package com.zanaaustralis.feature_kyc.data.remote

import com.zanaaustralis.feature_kyc.data.model.StartValidationRequest
import com.zanaaustralis.feature_kyc.data.model.StartValidationResponse
import com.zanaaustralis.feature_kyc.data.model.ValidationStatusResponse
import com.zanaaustralis.feature_kyc.data.model.VerifyValidationRequest
import com.zanaaustralis.feature_kyc.data.model.VerifyValidationResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * @author esteban.rivera01
 */

interface IdentityValidationApi {

    @POST("identity-validation/start")
    suspend fun startValidation(
        @Body request: StartValidationRequest
    ): StartValidationResponse

    @POST("identity-validation/verify")
    suspend fun verifyValidation(
        @Body request: VerifyValidationRequest
    ): VerifyValidationResponse

    @GET("identity-validation/status/{id}")
    suspend fun getStatus(
        @Path("id") validationId: String
    ): ValidationStatusResponse
}
