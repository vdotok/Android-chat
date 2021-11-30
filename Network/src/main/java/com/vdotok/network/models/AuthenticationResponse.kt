package com.vdotok.network.models

import com.google.gson.annotations.SerializedName

data class AuthenticationResponse(

        @SerializedName("message")
        val message: String,

        @SerializedName("media_server_map")
        val mediaServer: MessagingServerMap,

        @SerializedName("messaging_server_map")
        val messagingServer: MessagingServerMap,

        @SerializedName("process_time")
        val processTime: Int,

        @SerializedName("status")
        val status: Int
)

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
