package com.zanaaustralis.feature_auth.presentation.register

/**
 * @author esteban.rivera01
 */

data class RegisterUiState(
    val rut: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val password: String? = null,
    val isLoading: Boolean = false,
    val alreadyPreregister: Boolean = false,
    val success: Boolean = false,
    val message: String? = null,
)
