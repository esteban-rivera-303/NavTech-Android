package com.zanaaustralis.feature_kyc.domain

import com.zanaaustralis.core.common.Resource
import com.zanaaustralis.feature_kyc.data.IdentityValidationRepository
import com.zanaaustralis.feature_kyc.data.model.ValidationStatusResponse
import javax.inject.Inject

/**
 * @author esteban.rivera01
 */

class GetValidationStatusUseCase @Inject constructor(
    private val repository: IdentityValidationRepository
) {
    suspend operator fun invoke(validationId: String): Resource<ValidationStatusResponse> {
        return repository.getStatus(validationId)
    }
}
