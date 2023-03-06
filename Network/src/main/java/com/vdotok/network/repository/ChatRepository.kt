package com.vdotok.network.repository

import com.vdotok.network.models.UploadFileResponse
import com.vdotok.network.network.Result
import com.vdotok.network.network.api.ApiService
import com.vdotok.network.network.safeApiCall
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

class ChatRepository @Inject constructor(
    private val apiService: ApiService,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    suspend fun uploadFile(
        type: RequestBody,
        file: MultipartBody.Part?,
        auth_token: RequestBody
    ): Result<UploadFileResponse> {
        return safeApiCall(dispatcher) {
            apiService.uploadImage(type, file, auth_token)
        }
    }

}