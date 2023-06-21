package com.vdotok.network.models

import com.google.gson.annotations.SerializedName

data class UploadFileResponse(

    @SerializedName("File_Name")
    val File_Name: String,

    @SerializedName("data")
    val data: String,

    @SerializedName("file_name")
    val file_name: String,

    @SerializedName("filetype")
    val filetype: String,

    @SerializedName("message")
    val message: String,

    @SerializedName("status")
    val status: Int,

    @SerializedName("uploadType")
    val uploadType: String
)