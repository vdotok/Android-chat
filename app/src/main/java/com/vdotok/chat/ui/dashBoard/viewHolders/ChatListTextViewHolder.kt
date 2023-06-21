package com.vdotok.chat.ui.dashBoard.viewHolders

import androidx.recyclerview.widget.RecyclerView
import com.vdotok.chat.databinding.ItemChatTextBinding
import com.vdotok.connect.models.Message
import com.vdotok.connect.models.ReceiptType
import com.vdotok.network.models.GroupModel

class ChatListTextViewHolder(private val  binding : ItemChatTextBinding):  RecyclerView.ViewHolder(binding.root){
    var sendStatus = false
    fun bind(unreadMessage: (Message) -> Unit, message: Message, ownUserName: String, otherUser: String, groupModel: GroupModel?) {
        binding.customMessageTypeText.messageDisplay.text = message.content
        binding.otherUserName = otherUser
        binding.model = message
        if (groupModel != null) {
            binding.isAutoCreated = groupModel.autoCreated == 1
        }
        binding.sendStatus = sendStatus
        if (binding.model?.from != ownUserName) {
            binding.sender = false
            if (message.status != ReceiptType.SEEN.value) {
                message.status = ReceiptType.SEEN.value
                unreadMessage.invoke(message)
            }
        }else{
            binding.sender = true
            binding.seenMsg = binding.model?.status == ReceiptType.SEEN.value && binding.model?.readCount!! > 0
        }
    }

}