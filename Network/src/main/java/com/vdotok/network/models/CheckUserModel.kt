package com.vdotok.network.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

/**
 * Created By: Vdotok
 * Date & Time: On 1/21/21 At 1:17 PM in 2021
 *
 * Response model map class to check user details
 */
// Added ths class of only one variable because we are using raw post params for API calling mechanism
@Parcelize
data class CheckUserModel (

    @SerializedName("email")
    var email: String? = null
) : Parcelable