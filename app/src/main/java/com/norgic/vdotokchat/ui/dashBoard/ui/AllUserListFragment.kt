package com.norgic.vdotokchat.ui.dashBoard.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.ObservableField
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.google.android.material.snackbar.Snackbar
import com.norgic.vdotokchat.R
import com.norgic.vdotokchat.databinding.LayoutAllUserListBinding
import com.norgic.vdotokchat.dialogs.CreateGroupDialog
import com.norgic.vdotokchat.databinding.LayoutSelectContactBinding
import com.norgic.vdotokchat.extensions.*
import com.norgic.vdotokchat.models.AllGroupsResponse
import com.norgic.vdotokchat.models.CreateGroupModel
import com.norgic.vdotokchat.models.GetAllUsersResponseModel
import com.norgic.vdotokchat.models.UserModel
import com.norgic.vdotokchat.extensions.hide
import com.norgic.vdotokchat.extensions.show
import com.norgic.vdotokchat.extensions.toggleVisibility
import com.norgic.vdotokchat.models.*
import com.norgic.vdotokchat.network.ApiService
import com.norgic.vdotokchat.network.Result
import com.norgic.vdotokchat.network.RetrofitBuilder
import com.norgic.vdotokchat.prefs.Prefs
import com.norgic.vdotokchat.ui.dashBoard.adapter.AllUserListAdapter
import com.norgic.vdotokchat.ui.dashBoard.adapter.OnInboxItemClickCallbackListner
import com.norgic.vdotokchat.utils.ApplicationConstants
import com.norgic.vdotokchat.utils.safeApiCall
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class AllUserListFragment: Fragment(), OnInboxItemClickCallbackListner {

    lateinit var adapter: AllUserListAdapter
    private lateinit var binding: LayoutAllUserListBinding
    private lateinit var prefs: Prefs

    var title : String? = null

    var edtSearch = ObservableField<String>()
    private var userList = ArrayList<UserModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = LayoutAllUserListBinding.inflate(inflater, container, false)
        prefs = Prefs(activity)

        init()
        textListenerForSearch()
        getAllUsers()

        return binding.root
    }

    private fun init() {
        initUserListAdapter()

        binding.search = edtSearch

        binding.customToolbar.title.text = getString(R.string.createGroupText)

        binding.customToolbar.createGroupBtn.setOnClickListener {
            activity?.hideKeyboard()
            if (adapter.getSelectedUsers().isNotEmpty()) {
                onCreateGroupClick()
            } else {
                binding.root.showSnackBar(R.string.no_user_select)
            }

        }

        binding.customToolbar.imgBack.setOnClickListener {
            activity?.hideKeyboard()
            activity?.onBackPressed()
        }
    }

    private fun initUserListAdapter() {
        adapter = AllUserListAdapter(ArrayList(),this)
        binding.rcvUserList.adapter = adapter
    }

    private fun onCreateGroupClick() {
        val selectedUsersList: List<UserModel> = adapter.getSelectedUsers()

        if (selectedUsersList.isNotEmpty() && selectedUsersList.size == 1){
            getGroupTitle(selectedUsersList).let {
                if (it != null) {
                    createGroup(it)
                }
            }
        } else{
            activity?.supportFragmentManager?.let { CreateGroupDialog(this::createGroup).show(it, CreateGroupDialog.TAG) }
        }

    }


    /**
     * Function for creating a group
     * */
    fun createGroup(title: String) {
        val selectedUsersList: List<UserModel> = adapter.getSelectedUsers()

        if(selectedUsersList.isNotEmpty()){

            val model = CreateGroupModel()
            model.groupTitle = title
            //model.auto_created -> set auto created group, set 1 for only single user, 0 for multiple users
            model.pariticpants = getParticipantsIds(selectedUsersList)

            when (selectedUsersList.size) {
                1 -> model.autoCreated = 1
                else -> model.autoCreated = 0
            }

            createGroupApiCall(model)
        }
    }

    private fun getGroupTitle(selectedUsersList: List<UserModel>): String? {

        var title = prefs.loginInfo?.fullName.plus("-")
        //In this case, we have only one item in list
        selectedUsersList.forEach {
            title = title.plus(it.userName.toString())
        }
        return title
    }

    private fun createGroupApiCall(model: CreateGroupModel) {
        binding.progressBar.toggleVisibility()
        val apiService: ApiService =
                RetrofitBuilder.makeRetrofitService(activity?.applicationContext!!)
        prefs.loginInfo?.authToken.let {
            CoroutineScope(Dispatchers.IO).launch {
                val response = safeApiCall { apiService.createGroup (auth_token = "Bearer $it", model) }
                withContext(Dispatchers.Main) {
                    try {
                        when (response) {
                            is Result.Success -> {
                                Snackbar.make(
                                        binding.root,
                                        R.string.group_created,
                                        Snackbar.LENGTH_LONG
                                ).show()
                                handleCreateGroupSuccess(response.data)
                            }
                            is Result.Error -> {
                                if (response.error.responseCode == ApplicationConstants.HTTP_CODE_NO_NETWORK) {
                                    binding.root.showSnackBar(getString(R.string.no_network_available))
                                } else {
                                    binding.root.showSnackBar(response.error.message)
                                }

                            }
                        }
                    } catch (e: HttpException) {
                        Log.e(API_ERROR, "createGroup: ${e.printStackTrace()}")
                    } catch (e: Throwable) {
                        Log.e(API_ERROR, "createGroup: ${e.printStackTrace()}")
                    }
                    binding.progressBar.toggleVisibility()
                }
            }
        }
    }


    private fun handleCreateGroupSuccess(response: CreateGroupResponse) {
        activity?.hideKeyboard()
        response.groupModel?.let {
            (activity as DashboardActivity).subscribe(it)
            openChatFragment(response.groupModel)
        }

    }

    /**
     * Function for setting participants ids
     * @param selectedUsersList list of selected users to form a group with
     * @return Returns an ArrayList<Int> of selected user ids
     * */
    private fun getParticipantsIds(selectedUsersList: List<UserModel>): ArrayList<Int> {
        val list: ArrayList<Int> = ArrayList()
        selectedUsersList.forEach { userModel ->
            userModel.id?.let { list.add(it.toInt()) }
        }
        return list
    }

    private fun getAllUsers() {
        activity?.let {
            binding.progressBar.toggleVisibility()
            val apiService: ApiService = RetrofitBuilder.makeRetrofitService(it)
            prefs.loginInfo?.authToken.let {
                CoroutineScope(Dispatchers.IO).launch {
                    val response = safeApiCall { apiService.getAllUsers (auth_token = "Bearer $it") }
                    withContext(Dispatchers.Main) {
                        try {
                            when (response) {
                                is Result.Success -> {
                                    populateDataToList(response.data)
                                }
                                is Result.Error -> {
                                    if (response.error.responseCode == ApplicationConstants.HTTP_CODE_NO_NETWORK) {
                                        binding.root.showSnackBar(getString(R.string.no_network_available))
                                    } else {
                                        binding.root.showSnackBar(response.error.message)
                                    }
                                }
                            }
                        } catch (e: HttpException) {
                            Log.e(API_ERROR, "allUser: ${e.printStackTrace()}")
                        } catch (e: Throwable) {
                            Log.e(API_ERROR, "allUser: ${e.printStackTrace()}")
                        }
                        binding.progressBar.toggleVisibility()
                    }
                }
            }
        }

    }

    private fun textListenerForSearch() {
            binding.searchEditText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable) {
                   adapter.filter?.filter(s)
                }
            })
        }

    private fun openChatFragment(groupModel: GroupModel?) {
        val bundle = Bundle()
        bundle.putParcelable(GroupModel.TAG, groupModel)
        Navigation.findNavController(binding.root).navigate(R.id.action_open_chat_fragment,bundle)
    }


    private fun populateDataToList(response: GetAllUsersResponseModel) {
        adapter.updateData(response.users)
    }

    override fun onItemClick(position: Int) {
        val item = adapter.dataList[position]
        item.isSelected = item.isSelected.not()
        adapter.notifyItemChanged(position)
    }

    override fun searchResult(position: Int) {
        edtSearch.get()?.isNotEmpty()?.let {
            if (position == 0 && it){
                binding.check.show()
                binding.rcvUserList.hide()
            }else{
                binding.check.hide()
                binding.rcvUserList.show()
            }
        }
    }

    companion object {

        const val TAG_FRAGMENT_USERLIST = "TAG_FRAGMENT_USERLIST"
        const val API_ERROR = "API_ERROR"

        @JvmStatic
        fun newInstance() = AllUserListFragment()

    }
}



