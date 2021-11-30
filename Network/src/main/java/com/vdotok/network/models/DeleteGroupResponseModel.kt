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
data class DeleteGroupResponseModel (

    @SerializedName("message")
    var message: String,

    @SerializedName("process_time")
    var processTime: Int,

    @SerializedName("status")
    var status: Int

): Parcelable