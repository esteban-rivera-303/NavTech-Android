package com.zanaaustralis.core.data.remote.repository


import android.util.Log
import com.zanaaustralis.core.common.Resource
import com.zanaaustralis.core.common.parseIsoDate
import com.zanaaustralis.core.data.local.TotpStorage
import com.zanaaustralis.core.data.remote.AccountRecoveryApi
import com.zanaaustralis.core.data.remote.model.AccountRecoveryCompleteRequest
import com.zanaaustralis.core.data.remote.model.AccountRecoveryCompleteResponse
import com.zanaaustralis.core.data.remote.model.AccountRecoveryInitiateRequest
import com.zanaaustralis.core.data.remote.model.AccountRecoveryInitiateResponse
import com.zanaaustralis.core.data.remote.model.AccountRecoveryVerifyRequest
import com.zanaaustralis.core.data.remote.model.AccountRecoveryVerifyRequestCodeRequest
import com.zanaaustralis.core.data.remote.model.AccountRecoveryVerifyResponse
import com.zanaaustralis.core.data.remote.model.AccountStatusRequest
import com.zanaaustralis.core.data.remote.model.AuthSessionResponse
import com.zanaaustralis.core.data.remote.model.RecoveryEnrollRequest
import com.zanaaustralis.core.data.remote.model.RecoveryEnrollResponse
import com.zanaaustralis.core.data.remote.model.RecoveryStatusRequestCodeResponse
import com.zanaaustralis.core.data.remote.model.RecoveryStatusResponse
import com.zanaaustralis.core.data.remote.model.TrustedDeviceReactivateRequest
import com.zanaaustralis.core.data.remote.model.TrustedDeviceReactivateResponse
import com.zanaaustralis.core.data.remote.model.UserDataResponse
import com.zanaaustralis.core.domain.repository.AccountRecoveryRepository
import com.zanaaustralis.core.secure.device.DeviceIdentityManager
import com.zanaaustralis.core.session.SessionManagerImpl
import com.zanaaustralis.core.store.notification.PushTokenStore

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

/**
 * @author esteban.rivera01
 */

class AccountRecoveryRepositoryImpl @Inject constructor(
    private val api: AccountRecoveryApi,
    private val deviceIdentity: DeviceIdentityManager,
    private val pushTokenStore: PushTokenStore,
    private val sessionManagerImpl: SessionManagerImpl,
    private val totpStorage: TotpStorage,
    ) : AccountRecoveryRepository {


    override suspend fun initiateRecovery(
        rut: String,
        deviceUuid: String
    ): Resource<AccountRecoveryInitiateResponse> = withContext(Dispatchers.IO) {
        try {
            val request = AccountRecoveryInitiateRequest(rut, deviceUuid)
            val response = api.initiateRecovery(request, deviceUuid = deviceUuid)
            if (response.isSuccessful) {
                response.body()?.let { Resource.Success(it) }
                    ?: Resource.Error("Empty response body")
            } else {
                Resource.Error("Error ${response.code()}: ${response.message()}")
            }
        } catch (e: IOException) {
            Resource.Error("Network error: ${e.message}")
        } catch (e: HttpException) {
            Resource.Error("Server error: ${e.message}")
        } catch (e: Exception) {
            Resource.Error("Unexpected error: ${e.localizedMessage}")
        }
    }

    override suspend fun verifyEmail(
        token: String,
        code: String
    ): Resource<AccountRecoveryVerifyResponse> = withContext(Dispatchers.IO) {
        try {
            val request = AccountRecoveryVerifyRequest(token, code)
            val response = api.verifyEmailRecovery(request)
            if (response.isSuccessful) {
                response.body()?.let { Resource.Success(it) }
                    ?: Resource.Error("Empty response body")
            } else {
                Resource.Error("Error ${response.code()}: ${response.message()}")
            }
        } catch (e: IOException) {
            Resource.Error("Network error: ${e.message}")
        } catch (e: HttpException) {
            Resource.Error("Server error: ${e.message}")
        } catch (e: Exception) {
            Resource.Error("Unexpected error: ${e.localizedMessage}")
        }
    }

    override suspend fun verifyPhone(
        token: String,
        code: String
    ): Resource<AccountRecoveryVerifyResponse> = withContext(Dispatchers.IO) {
        try {
            val request = AccountRecoveryVerifyRequest(token, code)
            val response = api.verifyPhoneRecovery(request)
            if (response.isSuccessful) {
                response.body()?.let { Resource.Success(it) }
                    ?: Resource.Error("Empty response body")
            } else {
                Resource.Error("Error ${response.code()}: ${response.message()}")
            }
        } catch (e: IOException) {
            Resource.Error("Network error: ${e.message}")
        } catch (e: HttpException) {
            Resource.Error("Server error: ${e.message}")
        } catch (e: Exception) {
            Resource.Error("Unexpected error: ${e.localizedMessage}")
        }
    }

    override suspend fun completeRecovery(
        token: String
    ): Resource<AccountRecoveryCompleteResponse> = withContext(Dispatchers.IO) {
        try {
            val request = AccountRecoveryCompleteRequest(token)
            val response = api.completeRecovery(request)
            if (response.isSuccessful) {
                response.body()?.let { Resource.Success(it) }
                    ?: Resource.Error("Empty response body")
            } else {
                Resource.Error("Error ${response.code()}: ${response.message()}")
            }
        } catch (e: IOException) {
            Resource.Error("Network error: ${e.message}")
        } catch (e: HttpException) {
            Resource.Error("Server error: ${e.message}")
        } catch (e: Exception) {
            Resource.Error("Unexpected error: ${e.localizedMessage}")
        }
    }

    override suspend fun reactivateTrustedDevice(
        rut: String,
        publicKey: String,
        userAuthPublicKey: String?
    ): Resource<TrustedDeviceReactivateResponse> = withContext(Dispatchers.IO) {
        try {
            val request = TrustedDeviceReactivateRequest(
                rut = rut,
                device_uuid = deviceIdentity.getOrCreateDeviceUUID(),
                public_key = publicKey,
                biometric_verified = true,
                device_info = deviceIdentity.getDeviceInfo(),
                fcm_token = pushTokenStore.get() ?: "",
                user_auth_public_key = userAuthPublicKey
            )

            val response = api.reactivateTrustedDevice(request)

            if (response.isSuccessful) {
                response.body()?.let { body ->
                    if (body.success) {
                        body.totp_secret.let {
                            totpStorage.saveSecret(it)
                        }
                        val expiresAt = parseIsoDate(body.expiresAt)

                        val session = AuthSessionResponse(
                            accessToken = body.access_token,
                            refreshToken = body.refresh_token,
                            expiresAt = expiresAt,
                        )

                        val userDataResponse = UserDataResponse(
                            full_name = body.user?.full_name ?: "",
                            rut = body.user?.rut ?: "",
                            email = body.user?.email ?: ""
                        )
                        sessionManagerImpl.updateSession(session)
                        sessionManagerImpl.updateUserData(userDataResponse)
                        Resource.Success(body)
                    } else {
                        Resource.Error("No login")
                    }
                } ?: Resource.Error("Empty response body")
            } else {
                Resource.Error("Error ${response.code()}: ${response.message()}")
            }

        } catch (e: IOException) {
            Resource.Error("Network error: ${e.message}")
        } catch (e: HttpException) {
            Resource.Error("Server error: ${e.message}")
        } catch (e: Exception) {
            Resource.Error("Unexpected error: ${e.localizedMessage}")
        }
    }

    override suspend fun enrollDevice(rut: String, publicKey: String, userAuthPublicKey: String?): Resource<RecoveryEnrollResponse> = withContext(Dispatchers.IO) {
        try {
            val request = RecoveryEnrollRequest(
                rut = rut,
                public_key = publicKey,
                device_info = deviceIdentity.getDeviceInfo(),
                device_uuid = deviceIdentity.getOrCreateDeviceUUID(),
                fcm_token = pushTokenStore.get() ?: "",
                user_auth_public_key = userAuthPublicKey
            )
            val response = api.recoveryEnrollDevice(request)

            if (response.isSuccessful) {
                response.body()?.let { body ->
                    body.totp_secret.let {
                        totpStorage.saveSecret(it)
                    }
                    val expiresAt = parseIsoDate(body.expires_at)

                    val session = AuthSessionResponse(
                        accessToken = body.access_token,
                        refreshToken = body.refresh_token,
                        expiresAt = expiresAt,
                    )

                    val userDataResponse = UserDataResponse(
                        full_name = body.user?.full_name ?: "",
                        rut = body.user?.rut ?: "",
                        email = body.user?.email ?: ""
                    )
                    sessionManagerImpl.updateSession(session)
                    sessionManagerImpl.updateUserData(userDataResponse)
                    Resource.Success(body)
                } ?: Resource.Error("Empty response body")
            } else {
                Resource.Error("Error ${response.code()}: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error("Network error: ${e.message}")
        }
    }

    override suspend fun getRecoveryStatus(
        token: String,
    ): Resource<RecoveryStatusResponse> = withContext(Dispatchers.IO) {
        try {
            val request = AccountStatusRequest(recovery_token = token)
            val response = api.recoveryStatus(request)
            if (response.isSuccessful) {
                response.body()?.let {
                    Resource.Success(it) }
                    ?: Resource.Error("Empty response body")
            } else {
                Resource.Error("Error ${response.code()}: ${response.message()}")
            }
        } catch (e: IOException) {
            Resource.Error("Network error: ${e.message}")
        } catch (e: HttpException) {
            Resource.Error("Server error: ${e.message}")
        } catch (e: Exception) {
            Resource.Error("Unexpected error: ${e.localizedMessage}")
        }
    }

    override suspend fun requestNewCode(token: String, type: String): Resource<RecoveryStatusRequestCodeResponse> = withContext(Dispatchers.IO) {
        try {
            val request = AccountRecoveryVerifyRequestCodeRequest(recovery_token = token, type = type)
            val response = api.resendCode(request)
            if (response.isSuccessful) {
                response.body()?.let { Resource.Success(it) }
                    ?: Resource.Error("Empty response body")
            } else {
                Resource.Error("Error ${response.code()}: ${response.message()}")
            }
        } catch (e: IOException) {
            Resource.Error("Network error: ${e.message}")
        } catch (e: HttpException) {
            Resource.Error("Server error: ${e.message}")
        } catch (e: Exception) {
            Resource.Error("Unexpected error: ${e.localizedMessage}")
        }
    }

}
