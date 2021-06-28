package com.norgic.vdotokchat.network

/**
 * A generic class that holds a value with its loading status.
 * @param <T>
 */

sealed class Result<out T : Any> {

    data class Success<out T : Any>(val data: T) : Result<T>()
    data class Error(val error: ParsedError) : Result<Nothing>()

    override fun toString(): String {
        return when (this) {
            is Success<*> -> "Success[data=$data]"
            is Error -> "Error[apiError=$error]"
        }
    }

    fun getDataOrNull() = if (this is Success) data else null
}


data class ParsedError (
    val message: String,
    val responseCode: Int
)
