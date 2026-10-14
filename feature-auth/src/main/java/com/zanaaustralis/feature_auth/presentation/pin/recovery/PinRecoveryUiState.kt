package com.zanaaustralis.feature_auth.presentation.pin.recovery

/**
 * @author esteban.rivera01
 */

data class PinRecoveryUiState(
    val loading: Boolean = false,
    val validationId: String? = null,
    val expiresAt: String? = null, // ISO
    val remainingAttempts: Int = 3,
    val validationUrl: String? = null,
    val actionToken: String? = null,
    val success: Boolean = false,
    val error: String? = null
)