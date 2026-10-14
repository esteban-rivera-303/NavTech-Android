package com.zanaaustralis.feature_challanges.domain.usecase

/**
 * @author esteban.rivera01
 */
import com.zanaaustralis.core.common.Resource
import com.zanaaustralis.feature_challanges.domain.model.ChallengeItem
import com.zanaaustralis.feature_challanges.domain.repository.ChallengeRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetPendingChallengesUseCase  @Inject constructor(
    private val repository: ChallengeRepository
) {
    suspend operator fun invoke(token: String): Resource<List<ChallengeItem>> {
        return repository.getPendingChallenges(token)
    }
    val flow = repository.observeChallenges()
}
