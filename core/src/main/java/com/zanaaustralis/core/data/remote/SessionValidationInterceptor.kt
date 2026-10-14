package com.zanaaustralis.core.data.remote

import com.zanaaustralis.core.session.SessionInvalidator
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * @author esteban.rivera01
 */

class SessionValidationInterceptor @Inject constructor(
    private val sessionInvalidator: SessionInvalidator
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        if (response.code == 401) {
            runBlocking {
                sessionInvalidator.updateLastActivity()
                sessionInvalidator.invalidateSession()
            }
        }
        return response
    }
}