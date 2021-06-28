package com.norgic.vdotokchat.models

import com.google.gson.annotations.SerializedName

/**
 * Created By: Norgic
 * Date & Time: On 1/21/21 At 1:17 PM in 2021
 *
 * Response model map class getting the response after user has successfully logged in
 */
class LoginResponse {

    @SerializedName("message")
    var message: String? = null

    @SerializedName("process_time")
    var processTime: String? = null

    @SerializedName("full_name")
    var fullName: String? = null

    @SerializedName("auth_token")
    var authToken: String? = null

    @SerializedName("authorization_token")
    var authorizationToken: String? = null

    @SerializedName("ref_id")
    var refId: String? = null

    @SerializedName("status")
    var status: String? = null

    @SerializedName("userid")
    var userId: String? = null
}