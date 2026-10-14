package com.zanaaustralis.feature_auth.presentation.logout

import com.zanaaustralis.core.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * @author esteban.rivera01
 */

class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return authRepository.logout()
    }
}