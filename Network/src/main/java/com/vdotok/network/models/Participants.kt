package com.vdotok.network.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

/**
 * Created By: vdotok
 * Date & Time: On 1/21/21 At 1:17 PM in 2021
 *
 * Response model map class to get details of the participants involved in groups
 */
@Parcelize
data class Participants(

    @SerializedName("color_code")
    var color_code: String? = null,

    @SerializedName("color_id")
    var colorId: Int? = null,

    @SerializedName("email")
    var email: String? = null,

    @SerializedName("full_name")
    var fullname: String? = null,

    @SerializedName("user_id")
    var userId: Int? = null,

    @SerializedName("ref_id")
    var refID: String? = null,

    var isSelected: Boolean = false

) : Parcelable