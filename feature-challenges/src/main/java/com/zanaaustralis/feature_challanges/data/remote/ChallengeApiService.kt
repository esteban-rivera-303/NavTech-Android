package com.zanaaustralis.feature_challanges.data.remote

import com.zanaaustralis.core.data.remote.model.BaseResponse
import com.zanaaustralis.feature_challanges.data.model.ChallengeHistoryResponse
import com.zanaaustralis.feature_challanges.data.model.ChallengeResponse
import com.zanaaustralis.feature_challanges.data.model.EncryptedRespondRequest
import com.zanaaustralis.feature_challanges.data.model.NonceRequest
import com.zanaaustralis.feature_challanges.data.model.NonceResponse
import com.zanaaustralis.feature_challanges.data.model.TotpValidateResponse
import com.zanaaustralis.feature_challanges.data.model.ValidateTotpRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * @author esteban.rivera01
 */


interface ChallengeApiService {

    @GET("challenges")
    suspend fun getPendingChallenges(
    ): Response<ChallengeResponse>

    @POST("challenges/respond")
    suspend fun respondChallenge(
        @Header("x-argos-signature-version") signatureVersion: Int = 3,
        @Body request: EncryptedRespondRequest,
    ): Response<BaseResponse>

    @POST("request-nonce")
    suspend fun requestNonce(
        @Body request: NonceRequest
    ): Response<NonceResponse>

    @POST("totp/validate")
    suspend fun validateTotp(
        @Body request: ValidateTotpRequest
    ): Response<TotpValidateResponse>

    @GET("challenges/history")
    suspend fun getChallengesHistory(
        @Query("status") status: String? = null,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): Response<ChallengeHistoryResponse>
}
