package com.zanaaustralis.feature_auth.presentation.recovery.legacy

/**
 * @author esteban.rivera01
 */

data class RecoveryUiState(
    val step: Int = 1,
    val initializedSuccess: Boolean = false,
    val recoveryToken: String? = null,
    val emailVerified: Boolean = false,
    val phoneVerified: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)