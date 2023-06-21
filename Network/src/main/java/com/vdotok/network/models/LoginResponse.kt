package com.vdotok.network.models

import com.google.gson.annotations.SerializedName

/**
 * Created By: Vdotok
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

    @SerializedName("media_server_map")
    val mediaServer: MediaServerMap? = null

    @SerializedName("messaging_server_map")
    val messagingServer: MessagingServerMap? = null

    data class MediaServerMap(

        @SerializedName("complete_address")
        val completeAddress: String,
        @SerializedName("end_point")
        val endPoint: String,
        @SerializedName("host")
        val host: String,
        @SerializedName("port")
        val port: String,
        @SerializedName("protocol")
        val protocol: String
    )

    data class MessagingServerMap(
        @SerializedName("complete_address")
        val completeAddress: String,
        @SerializedName("host")
        val host: String,
        @SerializedName("port")
        val port: String,
        @SerializedName("protocol")
        val protocol: String
    )

}