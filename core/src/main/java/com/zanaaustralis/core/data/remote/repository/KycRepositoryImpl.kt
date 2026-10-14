package com.zanaaustralis.core.data.remote.repository

import com.zanaaustralis.core.common.Resource
import com.zanaaustralis.core.domain.repository.KycRepository

/**
 * @author esteban.rivera01
 */

class KycRepositoryImpl : KycRepository {
    override suspend fun startKyc(userId: String): Resource<Boolean> {

        return try {

            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Error starting KYC")
        }
    }

    override suspend fun getKycStatus(userId: String): Resource<String> {
        return try {

            Resource.Success("verified")
        } catch (e: Exception) {
            Resource.Error("Error retrieving KYC status")
        }
    }
}