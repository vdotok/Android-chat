package com.vdotok.chat.ui.dashBoard.viewHolders

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.vdotok.chat.databinding.ItemChatImageBinding
import com.vdotok.chat.databinding.ItemChatTextBinding
import com.vdotok.chat.utils.ApplicationConstants
import com.vdotok.connect.models.Message
import com.vdotok.connect.models.ReceiptType
import com.vdotok.connect.utils.ImageUtils.decodeBase64
import com.vdotok.network.models.GroupModel
import java.io.File

class ChatListImageViewHolder(private val  binding : ItemChatImageBinding):  RecyclerView.ViewHolder(binding.root){
    var sendStatus = false
    val directoryName: String = "Vdotok-chat"
    fun bind(message: Message, unreadMessage: (Message) -> Unit, ownUserName: String, otherUser: String,context: Context, groupModel: GroupModel?) {
        binding.customImageTypeText.imageTypeMessage.setImageBitmap(decodeBase64(message.content))
        binding.customImageTypeText.image.setOnClickListener { onImageClick(context) }
        binding.otherUserName = otherUser
        binding.model = message
        binding.sendStatus = sendStatus
        if (groupModel != null) {
            binding.isAutoCreated = groupModel.autoCreated == 1
        }
        if (binding.model?.from == ownUserName) {
            binding.sender = true
            binding.seenMsg =
                binding.model?.status == ReceiptType.SEEN.value && binding.model?.readCount!! > 0
        } else {
            if (binding.model?.status != ReceiptType.SEEN.value) {
                binding.model?.status = ReceiptType.SEEN.value
                unreadMessage.invoke(message)
            }
            binding.sender = false
        }
    }
    private fun onImageClick(context: Context) {
        val intent = Intent(Intent.ACTION_VIEW)
        val uri = Uri.parse(
            Environment.getExternalStorageDirectory().path
                    + File.separator + directoryName + File.separator
        )
        intent.setDataAndType(uri, DocumentsContract.Document.MIME_TYPE_DIR)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}