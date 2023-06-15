package com.vdotok.chat.ui.dashBoard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.vdotok.network.di.module.RetrofitModule
import com.vdotok.network.network.Result
import com.vdotok.network.repository.ChatRepository
import okhttp3.MultipartBody
import okhttp3.RequestBody

class ChatViewModel : ViewModel() {

    fun uploadFile(type: RequestBody, file: MultipartBody.Part, auth_token: RequestBody, extension: RequestBody) =
        liveData {
            val service = RetrofitModule.provideRetrofitService()
            val chatRepo = ChatRepository(service)
            emit(Result.Loading)
            emit(chatRepo.uploadFile(type, file, auth_token, extension))
        }
}