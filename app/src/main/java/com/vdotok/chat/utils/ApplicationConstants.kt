package com.vdotok.chat.utils


/**
 * Created By: Vdotok
 * Date & Time: On 5/5/21 At 5:06 PM in 2021
 */
object ApplicationConstants {

    //    SDK AUTH PARAMS
    var PROJECT_ID: String = "Enter your Project Id here"
    const val USER_PROJECT = "USER_PROJECT"
    const val USER_BASE = "USER_BASE"

//    Prefs constants
    const val SOCKET_CONNECTION = "SOCKET_CONNECTION"
    const val LOGIN_INFO = "LOGIN_INFO"
    const val GROUP_MODEL_KEY = "GROUP_MODEL_KEY"
    const val REQUEST_CODE_GALLERY = 100
    const val SDK_AUTH_RESPONSE = "SDK_AUTH_RESPONSE"
    const val PRESENCE_MODEL_KEY = "PRESENCE_MODEL_KEY"
    const val MY_PERMISSIONS_REQUEST_CAMERA = 100
    const val MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 101
    const val MY_PERMISSIONS_REQUEST = 102

    const val MAX_FILE_SIZE = 48000000

    const val FILE_SIZE_LIMIT = 6291456// 6MB

    const val CHUNK_SIZE = 4000// 12kb

    //    File paths
    const val IMAGES_DIRECTORY = "/Vdotok-chat/images"
    const val VIDEO_DIRECTORY = "/Vdotok-chat/videos"
    const val AUDIO_DIRECTORY = "/Vdotok-chat/audios"
    const val DOCS_DIRECTORY = "/Vdotok-chat/docs"

//    API ERROR LOG TAGS
    const val API_ERROR = "API_ERROR"
    const val HTTP_CODE_NO_NETWORK = 600
    const val SUCCESS_CODE = 200

    const val type = "chat_file_upload"
}