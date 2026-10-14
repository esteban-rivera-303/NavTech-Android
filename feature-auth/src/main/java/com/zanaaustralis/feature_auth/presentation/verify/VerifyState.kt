package com.zanaaustralis.feature_auth.presentation.verify

/**
 * @author esteban.rivera01
 */

enum class VerificationType { EMAIL, PHONE }

data class VerifyUiState(
    val isLoading: Boolean = false,
    val verified: Boolean = false,
    val message: String? = null,
    val error: String? = null,
    val secondsRemaining: Int = 30,
    val canResend: Boolean = false,
    val attempts: Int = 0,
    val blocked: Boolean = false
)