package com.zanaaustralis.core.domain.usescases.recovery

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.zanaaustralis.core.common.Resource
import com.zanaaustralis.core.common.parseIsoDate
import com.zanaaustralis.core.data.remote.model.AuthSessionResponse
import com.zanaaustralis.core.data.remote.model.TrustedDeviceReactivateResponse
import com.zanaaustralis.core.domain.repository.AccountRecoveryRepository
import com.zanaaustralis.core.secure.crypto.AndroidKeyStoreManager
import com.zanaaustralis.core.secure.crypto.UserAuthKeyStore
import com.zanaaustralis.core.session.SessionManagerImpl
import javax.inject.Inject

/**
 * @author esteban.rivera01
 */

class ReactivateTrustedDeviceUseCase @Inject constructor(
    private val repository: AccountRecoveryRepository,
    private val sessionManagerImpl: SessionManagerImpl,
    private val androidKeyStoreManager: AndroidKeyStoreManager,
    private val userAuthKeyStore: UserAuthKeyStore,
) {

    //@RequiresApi(Build.VERSION_CODES.O)
    suspend operator fun invoke(rut: String): Resource<TrustedDeviceReactivateResponse> {
        return try {
            androidKeyStoreManager.regenerateKeyPair()
            val publicKey = androidKeyStoreManager.publicKeyToPem()
            val response = repository.reactivateTrustedDevice(
                rut = rut,
                publicKey = publicKey,
                userAuthPublicKey = userAuthKeyStore.provisionForEnrollment()
            )
            when (response) {
                is Resource.Success<TrustedDeviceReactivateResponse> -> {
                    val expiresAt = parseIsoDate(response.data.expiresAt)

                    val session = AuthSessionResponse(
                        accessToken = response.data.access_token,
                        refreshToken = response.data.refresh_token,
                        expiresAt = expiresAt
                    )
                    sessionManagerImpl.updateSession(session)
                }

                else -> {
                    Resource.Error("Error: try again")
                }
            }
            response
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Device reactivation failed")
        }
    }
}