package com.norgic.vdotokchat.models

import android.os.Parcelable
import com.google.gson.Gson
import com.norgic.chatsdks.models.ReceiptType
import kotlinx.android.parcel.Parcelize

/**
 * Created By: Norgic
 * Date & Time: On 1/19/21 At 6:31 PM in 2021
 *
 * HeaderModel: used to map header data for the file type messages
 * @property id unique message id for a chat message
 * @property to consists of the group or chat we want to send message to
 * @property key unique key for the group or chat
 * @property from from where the message packet is from eg: username
 * @property type message type which defines text, media, typing etc
 * @property content value content inside the message object
 * @property size size of the message to be sent
 * @property isGroupMsg to verify if the message to be sent is in group
 * @property status status defines message in sent, received and seen
 */

@Parcelize
data class MessageModel constructor(
    var id: String? = null,
    var to: String? = null,
    var key: String? = null,
    var from: String? = null,
    var type: MessageType? = null,
    var content: String? = null,
    var size: Float? = null,
    var isGroupMsg: Boolean? = null,
    var status: Int = ReceiptType.SENT.value,
    var subType: Int = MediaType.IMAGE.value
): Parcelable {
    override fun toString(): String {
        val gson = Gson()
        return gson.toJson(this)
    }

    companion object{
        const val TAG = "message_model"
    }

}
