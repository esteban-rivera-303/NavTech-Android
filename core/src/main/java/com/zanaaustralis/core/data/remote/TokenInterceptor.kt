package com.zanaaustralis.core.data.remote

import com.zanaaustralis.core.session.SessionManagerCached
import com.zanaaustralis.core.session.SessionManagerImpl
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * @author esteban.rivera01
 */

class TokenInterceptor @Inject constructor(
    private val sessionManagerCached: SessionManagerCached,
    private val sessionManager: SessionManagerImpl,

) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val session = sessionManagerCached.getSession()
        val session2 = runBlocking {
            sessionManager.getSession()
        }

        val request = if (session != null) {
            chain.request().newBuilder()
                .header("Authorization", "Bearer ${session.accessToken}")
                .header("X-Platform", "android")
                .build()
        } else {
            chain.request()
        }

        return chain.proceed(request)
    }
}