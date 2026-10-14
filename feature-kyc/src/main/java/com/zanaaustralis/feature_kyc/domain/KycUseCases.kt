package com.zanaaustralis.feature_kyc.domain

import com.zanaaustralis.core.common.Resource
import com.zanaaustralis.feature_kyc.data.KycRepository
import com.zanaaustralis.feature_kyc.data.model.InitiateKycResponse
import com.zanaaustralis.feature_kyc.data.model.KycStatusResponse

/**
 * @author esteban.rivera01
 */

class KycUseCases(private val repo: KycRepository) {

    suspend fun initiateKyc(rut: String): InitiateKycResponse {
        return repo.initiate(rut)
    }

    suspend fun getKycStatus(rut: String): Resource<KycStatusResponse> {
        return repo.checkStatus(rut)
    }
}
