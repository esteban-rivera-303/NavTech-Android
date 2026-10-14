package com.zanaaustralis.feature_kyc.data.remote

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.zanaaustralis.core.data.remote.model.ApiError

/**
 * @author esteban.rivera01
 */
 fun parseError(json: String?): ApiError {
    return try {
        val moshi = Moshi.Builder().build()
        val adapter = moshi.adapter(ApiError::class.java)
        adapter.fromJson(json ?: "") ?: ApiError("Error desconocido")
    } catch (_: Exception) {
        ApiError("Error desconocido")
    }
}

inline fun <reified T> parseError(json: String?): T? {
    return try {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        val adapter = moshi.adapter(T::class.java)
        adapter.fromJson(json ?: "")
    } catch (_: Exception) {
        null
    }
}