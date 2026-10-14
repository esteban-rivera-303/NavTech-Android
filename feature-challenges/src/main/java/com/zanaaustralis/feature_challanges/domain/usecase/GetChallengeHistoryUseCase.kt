package com.zanaaustralis.feature_challanges.domain.usecase

import com.zanaaustralis.core.common.Resource
import com.zanaaustralis.feature_challanges.domain.model.ChallengeItem
import com.zanaaustralis.feature_challanges.domain.repository.ChallengeRepository
import javax.inject.Inject

/**
 * @author esteban.rivera01
 */

class GetChallengeHistoryUseCase @Inject constructor(
    private val repository: ChallengeRepository
) {
    suspend operator fun invoke(
        status: String? = null,
        limit: Int = 20,
        offset: Int = 0
    ): Resource<List<ChallengeItem>> {
        return repository.getChallengesHistory(status, limit, offset)
    }
}