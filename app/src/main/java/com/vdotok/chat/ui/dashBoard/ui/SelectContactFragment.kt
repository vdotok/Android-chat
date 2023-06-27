package com.vdotok.chat.ui.dashBoard.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ObservableField
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.vdotok.chat.R
import com.vdotok.chat.databinding.LayoutSelectContactBinding
import com.vdotok.chat.extensions.*
import com.vdotok.chat.models.Data
import com.vdotok.chat.models.NotificationEvent
import com.vdotok.chat.prefs.Prefs
import com.vdotok.chat.ui.dashBoard.adapter.OnChatItemClickCallbackListner
import com.vdotok.chat.ui.dashBoard.adapter.SelectUserContactAdapter
import com.vdotok.chat.ui.dashBoard.viewmodel.AllUserListFragmentViewModel
import com.vdotok.connect.manager.ChatManager
import com.vdotok.network.models.*
import com.vdotok.network.network.NetworkConnectivity
import com.vdotok.network.network.Result
import org.json.JSONArray

class SelectContactFragment: Fragment(), OnChatItemClickCallbackListner {

    lateinit var adapter: SelectUserContactAdapter
    private lateinit var binding: LayoutSelectContactBinding
    private lateinit var prefs: Prefs
    var title : String? = null
    private lateinit var cManger: ChatManager

    private val viewModel : AllUserListFragmentViewModel by viewModels()


    private var edtSearch = ObservableField<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = LayoutSelectContactBinding.inflate(inflater, container, false)
        prefs = Prefs(activity)

        init()
        textListenerForSearch()
        getAllUsers()
        addPullToRefresh()

        return binding.root
    }

    /**
     * Function for refreshing the updated group
     * */
    private fun addPullToRefresh() {
        binding.swipeRefreshLay.setOnRefreshListener {
            getAllUsers()
        }
    }

    private fun init() {
        initUserListAdapter()
        activity?.let {
            cManger = ChatManager.getInstance(it)
        }
        binding.search = edtSearch

        binding.customToolbar.title.text = getString(R.string.new_chat)
        binding.customToolbar.createGroupBtn.hide()

        binding.tvGroupChat.setOnClickListener {
            activity?.hideKeyboard()
            openAllUserListFragment()
            edtSearch.set("")
        }

        binding.customToolbar.imgBack.setOnClickListener {
            activity?.hideKeyboard()
            activity?.onBackPressed()
        }

    }

    private fun initUserListAdapter() {
        adapter = SelectUserContactAdapter(ArrayList(),this)
        binding.rcvUserList.adapter = adapter
    }

    private fun onCreateGroupClick() {
        val selectedUsersList: List<UserModel> = adapter.getSelectedUsers()
        createGroup(getGroupTitle(selectedUsersList))
    }

    /**
     * Function for creating a group
     * */
    private fun createGroup(title: String) {
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

    private fun getGroupTitle(selectedUsersList: List<UserModel>): String {

        var title = prefs.loginInfo?.fullName.plus("-")
        //In this case, we have only one item in list
        selectedUsersList.forEach {
            title = title.plus(it.userName.toString())
        }
        return title
    }


    private fun getAllUsers() {
        binding.swipeRefreshLay.isRefreshing = false
        binding.progressBar.toggleVisibility()
        activity?.let { activity ->
            viewModel.getAllUsers("Bearer ${prefs.loginInfo?.authToken}").observe(activity) {
                binding.progressBar.toggleVisibility()
                when (it) {
                    Result.Loading -> {
                        binding.progressBar.toggleVisibility()
                    }
                    is Result.Success ->  {
                        populateDataToList(it.data)
                    }
                    is Result.Failure -> {
                        binding.progressBar.toggleVisibility()
                        if(NetworkConnectivity.isNetworkAvailable(activity).not())
                            binding.root.showSnackBar(getString(R.string.no_internet))
                    }
                }
            }
        }
    }

    private fun createGroupApiCall(model: CreateGroupModel) {
        binding.progressBar.toggleVisibility()
        binding.viewDisableLayout.show()

        activity?.let { activity ->
            viewModel.createGroup("Bearer ${prefs.loginInfo?.authToken}", model).observe(activity) {
                binding.progressBar.toggleVisibility()
                binding.viewDisableLayout.hide()
                when (it) {
                    Result.Loading -> {
                        binding.progressBar.toggleVisibility()
                    }
                    is Result.Success ->  {
                        Snackbar.make(binding.root, R.string.group_created, Snackbar.LENGTH_LONG).show()
                        handleCreateGroupSuccess(it.data)
                    }
                    is Result.Failure -> {
                        binding.progressBar.toggleVisibility()
                        if(NetworkConnectivity.isNetworkAvailable(activity).not())
                            binding.root.showSnackBar(getString(R.string.no_internet))
                    }
                }
            }
        }
    }



    private fun handleCreateGroupSuccess(response: CreateGroupResponse) {
        activity?.hideKeyboard()
        response.let { model ->
            val dataModel = Data(
                action = NotificationEvent.NEW.value,
                groupModel = model
            )
            val toList: JSONArray = JSONArray().apply {
                model.groupModel.participants.forEach {
                    it.refID?.let { it1 -> this.put(it1) }
                }
            }
            cManger.publishNotification(
                from = prefs.loginInfo?.refId.toString(),
                to = toList,
                data = Gson().toJson(dataModel)
            )
            (activity as DashboardActivity).subscribe(model.groupModel)
        }
        openChatFragment(response.groupModel)
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

    private fun populateDataToList(response: GetAllUsersResponseModel) {
        adapter.updateData(response.users)
    }

    private fun openAllUserListFragment() {
        Navigation.findNavController(binding.root).navigate(R.id.action_open_all_users_list_fragment)
    }

    private fun openChatFragment(model: GroupModel?) {
        val bundle = Bundle()
        bundle.putParcelable(GroupModel.TAG, model)
        Navigation.findNavController(binding.root).navigate(R.id.action_open_chat_fragment, bundle)
    }

    override fun onItemClick(position: Int) {
        val item = adapter.dataList[position]
        item.isSelected = item.isSelected.not()
        adapter.notifyItemChanged(position)
        onCreateGroupClick()
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

}



