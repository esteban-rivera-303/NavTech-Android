package com.zanaaustralis.core.data.remote

import android.util.Log
import com.zanaaustralis.core.common.parseIsoDate
import com.zanaaustralis.core.data.remote.model.AuthSessionResponse
import com.zanaaustralis.core.data.remote.model.RefreshTokenRequest
import com.zanaaustralis.core.di.NoAuthApi
import com.zanaaustralis.core.session.SessionInvalidator
import com.zanaaustralis.core.session.SessionManagerImpl
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject

/**
 * @author esteban.rivera01
 */

class TokenAuthenticator @Inject constructor(
    private val sessionManager: SessionManagerImpl,
    private val sessionInvalidator: SessionInvalidator,
    @NoAuthApi private val authApi: dagger.Lazy<AuthApiService>

) : Authenticator {
    private val refreshMutex = Mutex()

    override fun authenticate(route: Route?, response: Response): Request? {
        Log.w("TokenAuthenticator", "Received ${response.code} response")
        if (response.code != 401) return null
        if (responseCount(response) >= 2) return null

        val newToken = try {
            runBlocking {
                refreshTokenSafe()
            }
        } catch (e: Exception) {
            null
        } ?: return null

        return response.request.newBuilder()
            .header("Authorization", "Bearer $newToken")
            .build()
    }

    private suspend fun refreshTokenSafe(): String? {
        return refreshMutex.withLock {
            refreshToken()
        }
    }

    private suspend fun refreshToken(): String? {
        val refresh = sessionManager.getSession()?.refreshToken ?: return null
        val body = RefreshTokenRequest(refresh)

        return try {
            val response = authApi.get().refresh(body)
            if (response.isSuccessful) {
                val body = response.body()!!

                val newSession = AuthSessionResponse(
                    accessToken = body.session_token,
                    refreshToken = body.refresh_token,
                    expiresAt = parseIsoDate(body.expires_at),
                )

                sessionManager.updateSession(newSession)

                newSession.accessToken

            } else {
                handleRefreshFailure(response.code())
                null
            }

        } catch (e: Exception) {
            null
        }
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }

    private suspend fun handleRefreshFailure(code: Int) {

        if (code == 401 || code == 403) {
            sessionManager.clearSession()
            sessionInvalidator.invalidateSession()
        }
    }
}