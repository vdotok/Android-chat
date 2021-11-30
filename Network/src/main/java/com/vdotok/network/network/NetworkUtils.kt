package com.vdotok.network.network

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

suspend fun <T : Any> safeApiCall(
    dispatcher: CoroutineDispatcher,
    call: suspend () -> T
): Result<T> {
    return withContext(dispatcher) {
        try {
            Result.Success(call())
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }
}

sealed class Result<out T> {
    data class Success<out T : Any>(val data: T) : Result<T>()
    data class Failure(val exception: Exception) : Result<Nothing>()
    object Loading : Result<Nothing>()
}