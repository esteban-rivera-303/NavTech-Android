package com.zanaaustralis.feature_kyc.data

import com.zanaaustralis.core.common.Resource
import com.zanaaustralis.feature_kyc.data.model.InitiateKycResponse
import com.zanaaustralis.feature_kyc.data.model.KycInitiateRequest
import com.zanaaustralis.feature_kyc.data.model.KycMetadata
import com.zanaaustralis.feature_kyc.data.model.KycStatusResponse
import com.zanaaustralis.feature_kyc.data.remote.KycApi
import retrofit2.HttpException

/**
 * @author esteban.rivera01
 */

class KycRepository(
    private val api: KycApi
) {

    suspend fun initiate(
        rut: String,
    ): InitiateKycResponse {
        val request = KycInitiateRequest(
            rut = rut,
            metadata = KycMetadata(source = "")
        )
        return api.initiate(request = request)
    }

    suspend fun checkStatus(rut: String): Resource<KycStatusResponse> {
        return try {
            Resource.Success(api.getStatus(rut))
        } catch (e: HttpException) {
            Resource.Error("HTTP ${e.code()}")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error de red")
        }
    }
}
