package com.zanaaustralis.core.domain.repository

import com.zanaaustralis.core.common.Resource

/**
 * @author esteban.rivera01
 */

interface KycRepository {
    suspend fun startKyc(userId: String): Resource<Boolean>
    suspend fun getKycStatus(userId: String): Resource<String>
}