package com.vdotok.network.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

/**
 * Created By: Vdotok
 * Date & Time: On 1/21/21 At 1:17 PM in 2021
 *
 * Response model class for mapping group information
 */
@Parcelize
data class GroupModel (

    @SerializedName("channel_name")
    var channelName: String = "",

    @SerializedName("admin_id")
    var adminId: Int? = null,

    @SerializedName("id")
    var id: Int? = null,

    @SerializedName("group_title")
    var groupTitle: String? = null,

    @SerializedName("participants")
    var participants: ArrayList<Participants> = ArrayList(),

    @SerializedName("auto_created")
    var autoCreated: Int? = null,

    @SerializedName("channel_key")
    var channelKey: String = "",

    @SerializedName("created_datetime")
    var createdDateTime: String = "",
    var isUnreadMessage: Boolean = false


): Parcelable{
    companion object{
        const val TAG = "GROUP_MODEL"
    }
}