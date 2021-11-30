package com.vdotok.network.models

import com.google.gson.annotations.SerializedName

/**
 * Created By: Vdotok
 * Date & Time: On 1/21/21 At 1:17 PM in 2021
 *
 * Response model map class for fetching all groups user is connected to
 */
class AllGroupsResponse {

    @SerializedName("groups")
    var groups: ArrayList<GroupModel>? = ArrayList()

    @SerializedName("message")
    var message: String? = null

    @SerializedName("status")
    var status: String? = null
}