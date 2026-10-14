package com.zanaaustralis.feature_challanges.domain.usecase

/**
 * @author esteban.rivera01
 */
data class ChallengeUseCases(
    val getPendingChallenges: GetPendingChallengesUseCase,
    val respondChallenge: RespondChallengeUseCase,
    val validateTotp: ValidateTotpUseCase
)
