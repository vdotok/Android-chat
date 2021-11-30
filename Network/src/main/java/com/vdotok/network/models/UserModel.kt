package com.vdotok.network.models

import android.os.Parcelable
import android.text.TextUtils
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

/**
 * Created By: Vdotok
 * Date & Time: On 1/21/21 At 1:17 PM in 2021
 *
 * Request model map class to send that a user is selected to form a group
 */
@Parcelize
data class UserModel (
     @SerializedName("email")
     var email: String? = null,

    @SerializedName("user_id")
    var id: String? = null,

    @SerializedName("full_name")
    var fullName: String? = null,

    @SerializedName("ref_id")
    var refID: String? = null,

    var isSelected: Boolean = false


) : Parcelable{

    val userName: String?
        get() {
            if (!TextUtils.isEmpty(fullName)) {
                return fullName
            } else
                return email
        }

}
