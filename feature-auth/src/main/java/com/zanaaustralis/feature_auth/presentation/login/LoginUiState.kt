package com.zanaaustralis.feature_auth.presentation.login

/**
 * @author esteban.rivera01
 */



data class LoginUiState(
    val success: Boolean = false,
    val isLoading : Boolean = false,
    val rut: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val password: String? = null,
)