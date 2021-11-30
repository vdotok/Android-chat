package com.vdotok.network.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import java.util.*

/**
 * Created By: Vdotok
 * Date & Time: On 1/21/21 At 1:17 PM in 2021
 *
 * Response model map class after creating a group
 */
@Parcelize
data class CreateGroupModel(

    @SerializedName("group_title")
    var groupTitle: String? = "",

    @SerializedName("participants")
    var pariticpants: ArrayList<Int> = ArrayList(),

    @SerializedName("auto_created")
    var autoCreated: Int? = null

): Parcelable
