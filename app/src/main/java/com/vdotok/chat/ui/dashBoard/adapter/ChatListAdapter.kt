package com.vdotok.chat.ui.dashBoard.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableField
import androidx.recyclerview.widget.RecyclerView
import com.vdotok.connect.models.Message
import com.vdotok.connect.models.MessageType
import com.vdotok.connect.models.ReadReceiptModel
import com.vdotok.connect.models.ReceiptType
import com.vdotok.chat.R
import com.vdotok.chat.databinding.ItemMessageTypeTextBinding
import com.vdotok.chat.extensions.hide
import com.vdotok.chat.extensions.show
import com.vdotok.chat.ui.dashBoard.ui.ChatFragment
import com.vdotok.chat.utils.ImageUtils
import com.vdotok.chat.utils.timeCheck
import java.util.*
import kotlin.collections.ArrayList


class ChatListAdapter(
    private val context: Context,
    private val chatFragment: ChatFragment,
    private val userName: String,
    list: List<Message>,
    private val callBack: OnMediaItemClickCallbackListner,
    val groupItemClick: () -> Unit,
    val unreadMessage: (Message) -> Unit
):
    RecyclerView.Adapter<ChatViewHolder>() {

    var items: ArrayList<Message> = ArrayList()
    var isSend: Boolean = false
    var countRead = ObservableField<String>()
//    var progress : Float = 0.0f

    init {
        items.addAll(list)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ChatViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.binding?.isSend = isSend
//        holder.binding?.count = countRead

        val data = items[position]
        holder.binding?.model = data
//        if (holder.binding?.progress != null) {
//            holder.binding?.progress?.setProgress(data.progress, false )
//            holder.binding?.progress?.show()
//        }
        if (data.from == userName) {
            holder.binding?.isSender = true
            holder.binding?.seenMsg = data.status == ReceiptType.SEEN.value && data.readCount > 0
//            when (data.status) {
//                ReceiptType.SEEN.value -> {
//                    holder.binding?.seenMsg = true
//                }
//                else -> holder.binding?.seenMsg = false
//            }

        }else{
                if (data.status != ReceiptType.SEEN.value) {
                    data.status = ReceiptType.SEEN.value
                    unreadMessage.invoke(data)
                }
            holder.binding?.isSender = false
        }

        when (data.type) {
            MessageType.text -> {
                holder.binding?.imageCard?.hide()
                holder.binding?.tvTime?.text = timeCheck(data.date)
                holder.binding?.cardAttachment?.visibility = View.GONE
                holder.binding?.imageCard?.hide()
                holder.binding?.tvMessage?.show()
                holder.binding?.tvMessage?.text = data.content
            }
            MessageType.media -> {
                when (data.subType) {
                    0 -> {
                        holder.binding?.tvMessage?.hide()
                        holder.binding?.cardAttachment?.hide()
                        holder.binding?.tvTime?.text = timeCheck(data.date)
                        holder.binding?.imageCard?.show()
                        holder.binding?.image?.setImageBitmap(ImageUtils.decodeBase64(data.content))
                        holder.binding?.imageCard?.setOnClickListener {
                            callBack.onFileClick()
                        }
                    }

                    2 -> {
                        holder.binding?.tvMessage?.hide()
                        holder.binding?.imageCard?.hide()
                        holder.binding?.tvTime?.text = timeCheck(data.date)
                        holder.binding?.cardAttachment?.show()
                        holder.binding?.attachmentText?.setText(R.string.video_file)
                        holder.binding?.cardAttachment?.setOnClickListener {
                            callBack.onFileClick()
                        }
                    }
                    3 -> {
                        holder.binding?.tvMessage?.hide()
                        holder.binding?.imageCard?.hide()
                        holder.binding?.tvTime?.text = timeCheck(data.date)
                        holder.binding?.cardAttachment?.show()
                        holder.binding?.attachmentText?.setText(R.string.doc_file)
                        holder.binding?.cardAttachment?.setOnClickListener {
                            callBack.onFileClick()
                        }
                    }
                    1 -> {
                        holder.binding?.tvMessage?.hide()
                        holder.binding?.imageCard?.hide()
                        holder.binding?.tvTime?.text = timeCheck(data.date)
                        holder.binding?.cardAttachment?.show()
                        holder.binding?.attachmentText?.setText(R.string.audio_file)
                        holder.binding?.cardAttachment?.setOnClickListener {
                            callBack.onFileClick()
                        }
                    }
                }

            }

            else -> { //holder.binding?.root?.hide()
             }
        }


        holder.binding?.tvSenderName?.text = chatFragment.getUserName(data)

        holder.itemView.setOnClickListener {
            groupItemClick.invoke()
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

        if(model.receiptType == ReceiptType.SEEN.value){
            item?.status = model.receiptType
            item?.readCount = item?.readCount?.plus(1) ?: 0

            item?.let {
                items[position] = item
                callBack.onRecieptReceive(it)
            }
//            notifyDataSetChanged()
            notifyItemChanged(position)
        }

//        items.forEachIndexed { index, message ->
//            if(message.id == model.messageId){
//                message.status = model.receiptType
//                if (message.status ==  ReceiptType.SEEN.value) {
//                    message.readCount += 1
//                }
//                items[index] = message
//                callBack.onRecieptReceive(message)
//                notifyItemRangeChanged(index, items.size)
//            }
//        }


    }


    override fun getItemId(position: Int): Long {
        return position.toLong()

    }
    override fun getItemViewType(position: Int): Int {
        return position
    }

    fun addItem(item: Message) {
        item.date = System.currentTimeMillis()
        items.add(item)
        notifyItemInserted(itemCount - 1)
    }

}

class ChatViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.item_message_type_text, parent, false)) {
    var binding: ItemMessageTypeTextBinding? = null
    init {
        binding = DataBindingUtil.bind(itemView)
    }

}
interface OnMediaItemClickCallbackListner {
    fun onFileClick()
    fun onRecieptReceive(message: Message)
}

