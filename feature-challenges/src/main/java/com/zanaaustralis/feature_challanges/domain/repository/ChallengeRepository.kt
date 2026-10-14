package com.zanaaustralis.feature_challanges.domain.repository

import com.zanaaustralis.core.common.Resource
import com.zanaaustralis.feature_challanges.data.model.EncryptedRespondRequest
import com.zanaaustralis.feature_challanges.domain.model.ChallengeItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow


/**
 * @author esteban.rivera01
 */

interface ChallengeRepository {


    fun observeChallenges(): StateFlow<List<ChallengeItem>>
    suspend fun refreshChallenges(token: String)

    suspend fun getPendingChallenges(token: String): Resource<List<ChallengeItem>>
    suspend fun respondChallenge(
        token: String,
        request: EncryptedRespondRequest,
        signatureVersion: Int = LEGACY_SIGNATURE_VERSION
    ): Resource<Boolean>
    suspend fun requestNonce(deviceUuid: String): Resource<String>
    suspend fun validateTotp(token: String, code: String): Resource<Boolean>

    suspend fun getChallengesHistory(
        status: String?,
        limit: Int,
        offset: Int
    ): Resource<List<ChallengeItem>>

    // Local persistence
    suspend fun savePendingChallenge(challenge: ChallengeItem)
    fun observePendingChallenges(): Flow<List<ChallengeItem>>
}

/** Versión que el backend desplegado verifica hoy (`device:challenge:response:timestamp`). */
const val LEGACY_SIGNATURE_VERSION = 3
