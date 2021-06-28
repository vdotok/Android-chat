package com.norgic.vdotokchat.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AuthenticationRequest(

    @SerializedName("auth_token")
    var apiKey: String,

    @SerializedName("project_id")
    var tenantId: String
): Parcelable