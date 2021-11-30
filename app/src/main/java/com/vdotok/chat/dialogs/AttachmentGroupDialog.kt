package com.vdotok.chat.dialogs

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.vdotok.chat.databinding.AttachmentDialogueBinding
import com.vdotok.chat.utils.performSingleClick

class AttachmentGroupDialog(private val videoAction : () -> Unit,
                            private val audioAction : () -> Unit,
                            private val docAction : () -> Unit,
                            private val cameraAction : () -> Unit,
                            private val locationAction : () -> Unit,
                            private val contactAction : () -> Unit) : DialogFragment(){

    private lateinit var binding: AttachmentDialogueBinding

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

        binding = AttachmentDialogueBinding.inflate(inflater, container, false)

        binding.imgClose.setOnClickListener {
            dismiss()
        }
        binding.imgAudio.performSingleClick {
            audioAction.invoke()
            dismiss()
        }

        binding.imgAlbum.performSingleClick {
            videoAction.invoke()
            dismiss()
        }

        binding.imgFile.performSingleClick {
            docAction.invoke()
            dismiss()
        }

        binding.imgCamera.performSingleClick {
            cameraAction.invoke()
            dismiss()
        }

        binding.imgLocation.performSingleClick {
            locationAction.invoke()
            dismiss()
        }

        binding.imgContact.performSingleClick {
            contactAction.invoke()
            dismiss()
        }
        return binding.root
    }

    companion object{
        const val TAG = "ATTACHMENT"
    }

}
