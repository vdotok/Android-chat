package com.vdotok.chat.dialogs

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.databinding.ObservableField
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.vdotok.chat.R
import com.vdotok.chat.databinding.UpdateGroupNameBinding
import com.vdotok.chat.dialogs.viewmodel.UpdateGroupNameDialogViewModel
import com.vdotok.chat.extensions.showSnackBar
import com.vdotok.chat.extensions.toggleVisibility
import com.vdotok.chat.prefs.Prefs
import com.vdotok.network.models.GroupModel
import com.vdotok.network.models.UpdateGroupNameModel
import com.vdotok.network.network.NetworkConnectivity
import com.vdotok.network.network.Result

class UpdateGroupNameDialog(private val groupModel: GroupModel, private val updateGroup : () -> Unit) : DialogFragment(){

    private lateinit var binding: UpdateGroupNameBinding
    private lateinit var prefs: Prefs
    private var edtGroupName = ObservableField<String>()

    private val viewModel : UpdateGroupNameDialogViewModel by viewModels()


    init {
        isCancelable = false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        prefs = Prefs(activity)

        if (dialog != null && dialog?.window != null) {
            dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        }

        binding = UpdateGroupNameBinding.inflate(inflater, container, false)
        binding.groupName = edtGroupName


        binding.imgClose.setOnClickListener {
            dismiss()
        }
        edtGroupName.set(groupModel.groupTitle)



        binding.btnDone.setOnClickListener {
            edtGroupName.get()?.isNotEmpty()?.let {groupName ->
                if (groupName) {
                    val model = UpdateGroupNameModel()
                    model.groupId = groupModel.id
                    model.groupTitle = edtGroupName.get()
                    editGroup(model)
                    dismiss()
                    updateGroup.invoke()
                } else {
                    binding.root.showSnackBar(activity?.applicationContext?.getString(R.string.group_name_empty))
                }
            }
        }

        return binding.root
    }

    private fun editGroup(model: UpdateGroupNameModel){

        activity?.let { activity ->
            viewModel.updateGroupName("Bearer ${prefs.loginInfo?.authToken}", model).observe(activity) {
                when (it) {
                    Result.Loading -> {
                        binding.progressBar.toggleVisibility()
                    }
                    is Result.Success ->  {
                        binding.root.showSnackBar(activity.applicationContext.getString(R.string.group_deleted))
                    }
                    is Result.Failure -> {
                        binding.progressBar.toggleVisibility()
                        if(NetworkConnectivity.isNetworkAvailable(activity).not())
                            binding.root.showSnackBar(activity.applicationContext.getString(R.string.no_internet))
                    }
                }
            }
        }
    }

    companion object{
        const val UPDATE_GROUP_TAG = "UPDATE_GROUP_DIALOG"
    }

}
