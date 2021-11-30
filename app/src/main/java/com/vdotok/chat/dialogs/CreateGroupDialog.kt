package com.vdotok.chat.dialogs

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.vdotok.chat.R
import com.vdotok.chat.databinding.CustomDialogueBinding
import com.vdotok.chat.extensions.showSnackBar

class CreateGroupDialog(private val createGroup : (title: String) -> Unit) : DialogFragment(){

    private lateinit var binding: CustomDialogueBinding

    init {
        isCancelable = false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        if (dialog != null && dialog?.window != null) {
            dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        }

        binding = CustomDialogueBinding.inflate(inflater, container, false)

        binding.imgClose.setOnClickListener {
            dismiss()
        }

        binding.btnDone.setOnClickListener {
            if (binding.edtGroupName.text.isNotEmpty()) {
                createGroup.invoke(binding.edtGroupName.text.toString())
                dismiss()
            } else {
                binding.root.showSnackBar(R.string.group_name_empty)
            }
        }

        return binding.root
    }

    companion object{
        const val TAG = "CREATE_GROUP_DIALOG"
    }

}
