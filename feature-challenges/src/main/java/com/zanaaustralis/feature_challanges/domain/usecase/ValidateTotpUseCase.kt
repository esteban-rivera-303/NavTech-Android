package com.zanaaustralis.feature_challanges.domain.usecase

import com.zanaaustralis.core.common.Resource
import com.zanaaustralis.feature_challanges.domain.repository.ChallengeRepository
import javax.inject.Inject

/**
 * @author esteban.rivera01
 */

class ValidateTotpUseCase  @Inject constructor(
    private val repository: ChallengeRepository
) {
    suspend operator fun invoke(deviceUuid: String, code: String): Resource<Boolean> {
        return repository.validateTotp(deviceUuid, code)
    }
}