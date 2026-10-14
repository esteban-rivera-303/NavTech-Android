package com.zanaaustralis.core.data.remote

import com.zanaaustralis.core.BuildConfig
import okhttp3.logging.HttpLoggingInterceptor

/**
 * @author esteban.rivera01
 */

/**
 * Logging HTTP seguro: solo registra en builds debug y nunca el header de autorización.
 * En release el nivel es NONE para no exponer tokens, RUT, TOTP ni ubicación en logcat.
 */
fun secureHttpLoggingInterceptor(): HttpLoggingInterceptor =
    HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
        redactHeader("Authorization")
    }
