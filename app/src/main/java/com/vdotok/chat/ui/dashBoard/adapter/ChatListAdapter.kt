package com.vdotok.chat.ui.dashBoard.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vdotok.chat.databinding.ItemChatFileBinding
import com.vdotok.chat.databinding.ItemChatImageBinding
import com.vdotok.chat.databinding.ItemChatTextBinding
import com.vdotok.chat.ui.dashBoard.ui.ChatFragment
import com.vdotok.chat.ui.dashBoard.viewHolders.ChatListFileViewHolder
import com.vdotok.chat.ui.dashBoard.viewHolders.ChatListImageViewHolder
import com.vdotok.chat.ui.dashBoard.viewHolders.ChatListTextViewHolder
import com.vdotok.connect.models.Message
import com.vdotok.connect.models.MessageType
import com.vdotok.connect.models.ReadReceiptModel
import com.vdotok.connect.models.ReceiptType
import com.vdotok.network.models.GroupModel


class ChatListAdapter(
    private val context: Context,
    private val chatFragment: ChatFragment,
    private val userName: String,
    list: List<Message>,
    private val groupModel: GroupModel?,
    private val callBack: OnMediaItemClickCallbackListner
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_TEXT_MESSAGE = 1
    private val VIEW_TYPE_IMAGE_MESSAGE = 2
    private val VIEW_TYPE_FILE_MESSAGE = 3
    var items: ArrayList<Message> = ArrayList()
    var isSend: Boolean = false

    init {
        items.addAll(list)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_TEXT_MESSAGE -> {
                val view =
                    ItemChatTextBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                ChatListTextViewHolder(view)
            }
            VIEW_TYPE_IMAGE_MESSAGE -> {
                val view =
                    ItemChatImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                ChatListImageViewHolder(view)
            }
            else -> {
                val view =
                    ItemChatFileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                ChatListFileViewHolder(view)
            }
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == VIEW_TYPE_TEXT_MESSAGE) {
            (holder as ChatListTextViewHolder).bind(
                { chatFragment.sendAcknowledgeMsgToGroup(items[position]) },
                items[position],
                userName,
                chatFragment.getUserName(items[position]),
                groupModel
            )
        } else if (getItemViewType(position) == VIEW_TYPE_IMAGE_MESSAGE) {
            (holder as ChatListImageViewHolder).bind(
                items[position],
                { chatFragment.sendAcknowledgeMsgToGroup(items[position]) },
                userName,
                chatFragment.getUserName(items[position]),
                context,
                groupModel
            )
        } else {
            (holder as ChatListFileViewHolder).bind(
                items[position],
                { chatFragment.sendAcknowledgeMsgToGroup(items[position]) },
                userName,
                chatFragment.getUserName(items[position]),
                context,
                groupModel
            )
        }
    }

    override fun getItemCount(): Int = items.size


    fun updateData(progress: Int, position: Int) {
        val message = items[position]
        message.progress = progress.toFloat()

        notifyItemChanged(position)
    }

    fun updateMessageForReceipt(model: ReadReceiptModel) {

        val item = items.firstOrNull { it.id == model.messageId }
        val position = items.indexOf(item)

        if (model.receiptType == ReceiptType.SEEN.value) {
            item?.status = model.receiptType
            item?.readCount = item?.readCount?.plus(1) ?: 0

            item?.let {
                items[position] = item
                callBack.onRecieptReceive(it)
            }
            notifyItemChanged(position)
        }


    }


    override fun getItemId(position: Int): Long {
        return position.toLong()

    }

    override fun getItemViewType(position: Int): Int {
        var type = 0
        val model: Message = items[position]
        if (model.type == MessageType.text) {
            type = VIEW_TYPE_TEXT_MESSAGE
        } else if (model.type == MessageType.media) {
            type = if (model.subType == 0) {
                VIEW_TYPE_IMAGE_MESSAGE
            } else {
                VIEW_TYPE_FILE_MESSAGE
            }
        }
        return type
    }

    fun addItem(item: Message) {
        item.date = System.currentTimeMillis()
        items.add(item)
        notifyItemInserted(itemCount - 1)
    }

}

interface OnMediaItemClickCallbackListner {
    fun onRecieptReceive(message: Message)
}

