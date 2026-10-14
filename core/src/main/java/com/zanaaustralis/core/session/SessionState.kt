package com.zanaaustralis.core.session

/**
 * @author esteban.rivera01
 */
sealed class SessionState {
    data object Active : SessionState()
    data object Expired : SessionState()
    data object InvalidToken : SessionState()
}
data class SessionStateNotification(
    val isLoggedIn: Boolean = false,
    val hasUnreadNotifications: Boolean = false
)