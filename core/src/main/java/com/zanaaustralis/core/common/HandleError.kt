package com.zanaaustralis.core.common

import org.json.JSONObject
import retrofit2.HttpException
import retrofit2.Response

/**
 * @author esteban.rivera01
 */

 fun parseError(response: Response<*>): String {
    return try {
        val errorBody = response.errorBody()?.string()
        if (errorBody != null && errorBody.contains("error")) {
            val json = JSONObject(errorBody)
            json.optString("error", "Unknown error")
        } else {
            "Unexpected error"
        }
    } catch (e: Exception) {
        "Error parsing server response"
    }
}

fun parseHttpError(e: HttpException): String {
    return when (e.code()) {
        400 -> "Solicitud inválida"
        401 -> "Sesión expirada"
        403 -> "No autorizado"
        404 -> "Servicio no disponible o validación no encontrada"
        409 -> "Ya existe una validación activa"
        429 -> "Demasiados intentos. Intenta más tarde"
        else -> "Error del servidor (${e.code()})"
    }
}

