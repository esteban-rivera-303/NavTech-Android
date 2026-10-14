package com.zanaaustralis.core.domain.repository

import com.zanaaustralis.core.common.Resource
import com.zanaaustralis.core.data.remote.model.AccountRecoveryCompleteResponse
import com.zanaaustralis.core.data.remote.model.AccountRecoveryInitiateResponse
import com.zanaaustralis.core.data.remote.model.AccountRecoveryVerifyResponse
import com.zanaaustralis.core.data.remote.model.RecoveryEnrollResponse
import com.zanaaustralis.core.data.remote.model.RecoveryStatusRequestCodeResponse
import com.zanaaustralis.core.data.remote.model.RecoveryStatusResponse
import com.zanaaustralis.core.data.remote.model.TrustedDeviceReactivateResponse
import java.security.PublicKey

/**
 * @author esteban.rivera01
 */


interface AccountRecoveryRepository {
    suspend fun initiateRecovery(rut: String, deviceUuid: String): Resource<AccountRecoveryInitiateResponse>
    suspend fun verifyEmail(token: String, code: String): Resource<AccountRecoveryVerifyResponse>
    suspend fun verifyPhone(token: String, code: String): Resource<AccountRecoveryVerifyResponse>
    suspend fun completeRecovery(token: String): Resource<AccountRecoveryCompleteResponse>
    suspend fun reactivateTrustedDevice(rut: String, publicKey: String, userAuthPublicKey: String? = null): Resource<TrustedDeviceReactivateResponse>
    suspend fun enrollDevice(rut: String, publicKey: String, userAuthPublicKey: String? = null): Resource<RecoveryEnrollResponse>
    suspend fun getRecoveryStatus(token: String): Resource<RecoveryStatusResponse>
    suspend fun requestNewCode(token: String, type: String): Resource<RecoveryStatusRequestCodeResponse>
}