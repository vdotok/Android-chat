package com.vdotok.network.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.vdotok.network.models.GroupModel
import kotlinx.android.parcel.Parcelize

/**
 * Created By: VdoTok
 * Date & Time: On 1/21/21 At 1:17 PM in 2021
 *
 * Response model map class for fetching all groups user is connected to
 */
@Parcelize
data class CreateGroupResponse(

    @SerializedName("message")
    var message: String,

    @SerializedName("process_time")
    var processTime: Int,

    @SerializedName("status")
    var status: Int,

    @SerializedName("group")
    var groupModel: GroupModel

) : Parcelable