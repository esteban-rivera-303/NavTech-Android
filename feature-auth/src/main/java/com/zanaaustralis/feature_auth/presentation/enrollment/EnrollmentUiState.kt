package com.zanaaustralis.feature_auth.presentation.enrollment

import com.zanaaustralis.core.domain.model.User

/**
 * @author esteban.rivera01
 */

data class EnrollmentUiState(
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val message: String? = null,
    val enrolledUser: User? = null,
    val recoveryTrustedDevice : Boolean = false
)
