package com.vdotok.network.repository

import com.vdotok.network.models.CheckUserModel
import com.vdotok.network.models.LoginResponse
import com.vdotok.network.models.LoginUserModel
import com.vdotok.network.models.SignUpModel
import com.vdotok.network.network.Result
import com.vdotok.network.network.api.ApiService
import com.vdotok.network.network.safeApiCall
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

class AccountRepository @Inject constructor(
        private val apiService: ApiService,
        private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    suspend fun login(model: LoginUserModel): Result<LoginResponse> {
        return safeApiCall(dispatcher) {
            apiService.loginUser(model)
        }
    }

    suspend fun emailAlreadyExist(model: CheckUserModel): Result<LoginResponse> {
        return safeApiCall(dispatcher) {
            apiService.checkEmail(model)
        }
    }

    suspend fun signUp(model: SignUpModel): Result<LoginResponse> {
        return safeApiCall(dispatcher) {
            apiService.signUp(model)
        }
    }
}