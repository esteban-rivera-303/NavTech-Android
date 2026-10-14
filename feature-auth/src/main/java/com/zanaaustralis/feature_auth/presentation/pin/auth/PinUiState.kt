package com.zanaaustralis.feature_auth.presentation.pin.auth

/**
 * @author esteban.rivera01
 */
data class PinUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val remainingAttempts: Int = 3,
    val blocked: Boolean = false
)
