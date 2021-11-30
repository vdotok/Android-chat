package com.vdotok.network.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SignUpModel(

    @SerializedName("full_name")
    var fullName: String = "",

    @SerializedName("email")
    var email: String = "",

    @SerializedName("password")
    var password: String = "",

    @SerializedName("device_type")
    var deviceType: String = "android",

    @SerializedName("device_model")
    var deviceModel: String? = null,

    @SerializedName("device_os_ver")
    var deviceOsVer: String? = null,

    @SerializedName("app_version")
    var appVersion: String = "1.0.0",

    @SerializedName("project_id")
    var project_id: String
): Parcelable