package com.zanaaustralis.core.domain.usescases.recovery

import android.util.Log
import com.zanaaustralis.core.common.Resource
import com.zanaaustralis.core.common.parseIsoDate
import com.zanaaustralis.core.data.remote.model.AuthSessionResponse
import com.zanaaustralis.core.data.remote.model.RecoveryEnrollResponse
import com.zanaaustralis.core.domain.repository.AccountRecoveryRepository
import com.zanaaustralis.core.secure.crypto.AndroidKeyStoreManager
import com.zanaaustralis.core.secure.crypto.UserAuthKeyStore
import com.zanaaustralis.core.session.SessionManagerImpl
import javax.inject.Inject

/**
 * @author esteban.rivera01
 */
class RecoveryEnrollDeviceUsesCases @Inject constructor(
    private val repository: AccountRecoveryRepository,
    private val androidKeyStoreManager: AndroidKeyStoreManager,
    private val sessionManagerImpl: SessionManagerImpl,
    private val userAuthKeyStore: UserAuthKeyStore
) {

    suspend operator fun invoke(rut: String): Resource<RecoveryEnrollResponse> {
        return try {
            androidKeyStoreManager.regenerateKeyPair()
            val publicKey = androidKeyStoreManager.publicKeyToPem()
            val response = repository.enrollDevice(rut, publicKey, userAuthKeyStore.provisionForEnrollment())
            when(response){
                is Resource.Success<RecoveryEnrollResponse> -> {
                    val expiresAt = parseIsoDate(response.data.expires_at)
                    val session = AuthSessionResponse(
                        accessToken = response.data.access_token,
                        refreshToken = response.data.refresh_token,
                        expiresAt = expiresAt
                    )
                    sessionManagerImpl.updateSession(session)
                }
                else -> {}
            }
            response
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Device reactivation failed")
        }
    }
}