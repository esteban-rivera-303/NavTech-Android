package com.zanaaustralis.core.session

/**
 * @author esteban.rivera01
 */

import com.zanaaustralis.core.data.remote.model.AuthSessionResponse
import com.zanaaustralis.core.data.remote.model.UserDataResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManagerImpl @Inject constructor(
    private val storage: TokenStorage
) {

    private val _sessionState = MutableStateFlow<SessionState>(SessionState.Active)
    val sessionState: StateFlow<SessionState> = _sessionState

    private val _sessionNotificationState = MutableStateFlow(SessionStateNotification())
    val sessionNotificationState: StateFlow<SessionStateNotification> = _sessionNotificationState

    fun updateUnreadNotifications(hasUnread: Boolean) {
        _sessionNotificationState.value = _sessionNotificationState.value.copy(
            hasUnreadNotifications = hasUnread
        )
    }

    private var cachedSession: AuthSessionResponse? = null
    private var cachedUserData: UserDataResponse? = null
    private val mutex = Mutex()
    private var lastActivityAt: Long = System.currentTimeMillis()

    suspend fun getSession(): AuthSessionResponse? = mutex.withLock {
        try {
            cachedSession ?: storage.getSession()?.also { cachedSession = it }
        } catch (e: IllegalStateException) {
            clearSession()
            null
        }
    }

    suspend fun isSessionActive(): Boolean {
        val session = getSession() ?: return false
        return session.expiresAt > System.currentTimeMillis()
    }

    suspend fun updateSession(session: AuthSessionResponse) = mutex.withLock {
        cachedSession = session
        storage.saveSession(session.accessToken, session.refreshToken, session.expiresAt)
    }

    // USER DATA
    private val _userData = MutableStateFlow<UserDataResponse?>(null)
    val userData: StateFlow<UserDataResponse?> = _userData

    suspend fun getUserData(): UserDataResponse? = mutex.withLock {
        try {
            val data = cachedUserData ?: storage.getDataUser()?.also {
                cachedUserData = it
            }
            _userData.value = data
            return data
        } catch (e: IllegalStateException) {
            clearSession()
            null
        }
    }

    suspend fun updateUserData(user: UserDataResponse) = mutex.withLock {
        cachedUserData = user
        _userData.value = user
        storage.saveUserData(user.full_name, user.email, user.rut)
    }
    fun updateLastActivity() {
        lastActivityAt = System.currentTimeMillis()
    }

    suspend fun clearSession() {
        mutex.withLock {
            cachedSession = null
            storage.clear()
            _sessionState.value = SessionState.Expired
        }
    }

    suspend fun onInvalidSessionDetected() {
        clearSession()
        /*mutex.withLock {
            cachedSession = null
            cachedUserData = null
            storage.clear()
            _userData.value = null
            _sessionState.value = SessionState.InvalidToken
        }*/
    }
}


