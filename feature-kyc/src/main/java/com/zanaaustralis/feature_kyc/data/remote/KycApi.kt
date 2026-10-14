package com.zanaaustralis.feature_kyc.data.remote

import com.zanaaustralis.core.common.Resource
import com.zanaaustralis.feature_kyc.data.model.InitiateKycResponse
import com.zanaaustralis.feature_kyc.data.model.KycInitiateRequest
import com.zanaaustralis.feature_kyc.data.model.KycStatusResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * @author esteban.rivera01
 */

interface KycApi {

    @POST("kyc/initiate")
    suspend fun initiate(
        @Header("X-Platform") platform: String = "android",
        @Body request: KycInitiateRequest
    ): InitiateKycResponse

    @GET("kyc/status")
    suspend fun getStatus(
        @Query("rut") rut: String
    ): KycStatusResponse
}
