package com.zanaaustralis.core.domain.model

/**
 * @author esteban.rivera01
 */

data class User(
    val id: String = "",
    val rut: String = "",
    val email: String = "",
    val phone: String = "",
    val name: String? = null,
    val verifiedEmail: Boolean = false,
    val verifiedPhone: Boolean = false,
    val totpSecret: String? = null,
    val sessionToken: String? = null,
    val refreshToken: String? = null,
    val expiresAt: String? = null
)