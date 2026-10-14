package com.zanaaustralis.feature_challanges.data.repository


import android.util.Log
import com.zanaaustralis.core.common.Resource
import com.zanaaustralis.feature_challanges.data.local.ChallengeLocalDataSource
import com.zanaaustralis.feature_challanges.data.model.EncryptedRespondRequest
import com.zanaaustralis.feature_challanges.data.model.NonceRequest
import com.zanaaustralis.feature_challanges.data.model.ValidateTotpRequest
import com.zanaaustralis.feature_challanges.data.model.toChallengeItem
import com.zanaaustralis.feature_challanges.data.remote.ChallengeApiService
import com.zanaaustralis.feature_challanges.domain.model.ChallengeItem
import com.zanaaustralis.feature_challanges.domain.model.toChallengeItem
import com.zanaaustralis.feature_challanges.domain.repository.ChallengeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Singleton

/**
 * @author esteban.rivera01
 */

@Singleton
class ChallengeRepositoryImpl(
    private val api: ChallengeApiService,
    private val localDataSource: ChallengeLocalDataSource
) : ChallengeRepository {

    // TODO new
    private val _challenges = MutableStateFlow<List<ChallengeItem>>(emptyList())
    override fun observeChallenges(): StateFlow<List<ChallengeItem>> =
        _challenges.asStateFlow()

    override suspend fun refreshChallenges(token: String) {
        try {
            val response = api.getPendingChallenges()
            if (response.isSuccessful) {
                val list = response.body()
                    ?.challenges
                    ?.map { it.toChallengeItem() }
                    .orEmpty()

                _challenges.value = list
            }
        } catch (_: Exception) {}
    }

    private val challenges = MutableStateFlow<List<ChallengeItem>>(emptyList())
    override suspend fun getPendingChallenges(token: String): Resource<List<ChallengeItem>> {
        return try {
            val response = api.getPendingChallenges()//"Bearer $token")
            if (response.isSuccessful) {
                val body = response.body()
                val list = body?.challenges?.map { it.toChallengeItem() }.orEmpty()
                challenges.value = list
                Resource.Success(list)
            } else {
                Resource.Error("Error fetching challenges", response.code())
            }
        } catch (e: Exception) {
            Resource.Error("Network error: ${e.localizedMessage}")
        }
    }

    override suspend fun respondChallenge(
        token: String,
        request: EncryptedRespondRequest,
        signatureVersion: Int
    ): Resource<Boolean> {
        return try {
            val response = api.respondChallenge(signatureVersion = signatureVersion, request = request)
            if (response.isSuccessful) Resource.Success(true)
            else Resource.Error("Challenge response failed", response.code())
        } catch (e: Exception) {
            Resource.Error("Network error: ${e.localizedMessage}")
        }
    }

    override suspend fun requestNonce(deviceUuid: String): Resource<String> {
        return try {
            val response = api.requestNonce(NonceRequest(device_uuid = deviceUuid))
            val nonce = response.body()?.nonce
            if (response.isSuccessful && !nonce.isNullOrBlank()) Resource.Success(nonce)
            else Resource.Error("Nonce request failed", response.code())
        } catch (e: Exception) {
            Resource.Error("Network error: ${e.localizedMessage}")
        }
    }

    override suspend fun validateTotp(token: String, code: String): Resource<Boolean> {
        return try {
            val response = api.validateTotp( ValidateTotpRequest(token, code)) // token
            if (response.isSuccessful && response.body()?.valid == true) Resource.Success(true)
            else Resource.Error("Invalid TOTP code", response.code())
        } catch (e: Exception) {
            Resource.Error("Network error: ${e.localizedMessage}")
        }
    }

    override suspend fun getChallengesHistory(
        status: String?,
        limit: Int,
        offset: Int
    ): Resource<List<ChallengeItem>> {

        return try {

            val response = api.getChallengesHistory(
                status = status,
                limit = limit,
                offset = offset
            )

            if (response.isSuccessful) {

                val body = response.body()

                val list = body?.challenges
                    ?.map { it.toChallengeItem() }
                    .orEmpty()

                Resource.Success(list)

            } else {
                Resource.Error("Error fetching history", response.code())
            }

        } catch (e: Exception) {
            Resource.Error("Network error: ${e.localizedMessage}")
        }
    }

    override suspend fun savePendingChallenge(challenge: ChallengeItem) {
        localDataSource.savePendingChallenge(challenge)
    }

    override fun observePendingChallenges(): Flow<List<ChallengeItem>> {
        return localDataSource.observePendingChallenges()
    }
}
