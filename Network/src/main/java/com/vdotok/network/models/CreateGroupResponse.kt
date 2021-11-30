package com.vdotok.network.models

import com.google.gson.annotations.SerializedName

/**
 * Created By: Vdotok
 * Date & Time: On 1/21/21 At 1:17 PM in 2021
 *
 * Response model map class for fetching all groups user is connected to
 */
class CreateGroupResponse {

    @SerializedName("group")
    var groupModel: GroupModel? = null

    @SerializedName("message")
    var message: String? = null

    @SerializedName("status")
    var status: String? = null
}