package com.zanaaustralis.core.domain.model

/**
 * @author esteban.rivera01
 */

data class OnboardingState(
    val rut: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val userId: String? = null,
    val isEmailVerified: Boolean = false,
    val isPhoneVerified: Boolean = false,
    val isIdentityVerified: Boolean = false,
    val isDeviceBound: Boolean = false
)