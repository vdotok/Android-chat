package com.vdotok.chat.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


/**
 * Created By: chat_call_121
 * Date & Time: On 20/12/2021 At 6:54 PM in 2021
 */
@Parcelize
data class QRCodeModel(
    var project_id: String? = null,
    var tenant_api_url: String? = null,
    var sandbox_api_url :String? = null,
    val api_key: String? = null,
    var tenant_name :String? = null
) : Parcelable