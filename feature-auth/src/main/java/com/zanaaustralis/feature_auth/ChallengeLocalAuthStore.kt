package com.zanaaustralis.feature_auth

/**
 * @author esteban.rivera01
 */
interface ChallengeLocalAuthStore {
    suspend fun locallyValidatedFactors(challengeId: String): Set<AuthFactor>
    suspend fun setAuthorizedFactors(challengeId: String, factors: Set<AuthFactor>)
    suspend fun clear(challengeId: String)

    suspend fun setTotpCode(challengeId: String, code: String)
    suspend fun getTotpCode(challengeId: String): String?
    suspend fun clearTotpCode(challengeId: String)
}
