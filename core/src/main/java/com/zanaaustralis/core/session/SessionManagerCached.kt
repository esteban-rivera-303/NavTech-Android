package com.zanaaustralis.core.session

import com.zanaaustralis.core.data.remote.model.AuthSessionResponse
import javax.inject.Inject
import javax.inject.Singleton

/**
 * @author esteban.rivera01
 */

@Singleton
class SessionManagerCached @Inject constructor(
    private val storage: TokenStorage
)  {

    @Volatile
    private var cachedSession: AuthSessionResponse? = null

    fun getSession(): AuthSessionResponse? {
        return try {
            cachedSession ?: storage.getSession()?.also {
                cachedSession = it
            }
        } catch (e: IllegalStateException) {
            clearSession()
            null
        }
    }

    fun getCachedSession(): AuthSessionResponse? {
        return cachedSession
    }

    fun clearSession() {
        cachedSession = null
        storage.clear()
    }
}
