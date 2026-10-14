package com.zanaaustralis.core.common

/**
 * @author esteban.rivera01
 */
sealed class Resource<out T> {
    data class Success<out T>(val data: T) : Resource<T>()
    data class Error(val error: String,  val code: Int? = null) : Resource<Nothing>()
    object Loading : Resource<Nothing>()
}

sealed class ApiError {
    data object InvalidToken : ApiError()
    data class Unknown(val message: String?) : ApiError()
}