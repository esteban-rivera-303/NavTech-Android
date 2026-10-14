package com.zanaaustralis.core.data.remote.repository


import android.util.Log
import com.zanaaustralis.core.common.Resource
import com.zanaaustralis.core.common.parseError
import com.zanaaustralis.core.common.parseIsoDate
import com.zanaaustralis.core.data.local.TotpStorage
import com.zanaaustralis.core.data.remote.AuthApiService
import com.zanaaustralis.core.data.remote.model.AuthSessionResponse
import com.zanaaustralis.core.data.remote.model.BaseResponse
import com.zanaaustralis.core.data.remote.model.LoginRequest
import com.zanaaustralis.core.data.remote.model.LoginResponse
import com.zanaaustralis.core.data.remote.model.OnboardingRequest
import com.zanaaustralis.core.data.remote.model.OnboardingResponse
import com.zanaaustralis.core.data.remote.model.RefreshTokenRequest
import com.zanaaustralis.core.data.remote.model.RegisterRequest
import com.zanaaustralis.core.data.remote.model.RegisterResponse
import com.zanaaustralis.core.data.remote.model.RequestCodeRequest
import com.zanaaustralis.core.data.remote.model.UserDataResponse
import com.zanaaustralis.core.data.remote.model.UserStatusRequest
import com.zanaaustralis.core.data.remote.model.UserStatusResponse
import com.zanaaustralis.core.data.remote.model.VerifyCodeRequest
import com.zanaaustralis.core.di.AuthenticatedApi
import com.zanaaustralis.core.domain.repository.AuthRepository
import com.zanaaustralis.core.secure.crypto.DeviceCryptoSigner
import com.zanaaustralis.core.secure.device.DeviceIdentityManager
import com.zanaaustralis.core.session.SessionManagerImpl
import com.zanaaustralis.core.session.TokenStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

/**
 * @author esteban.rivera01
 */

class AuthRepositoryImpl @Inject constructor(
    @AuthenticatedApi private val api: AuthApiService,
    private val sessionManagerImpl: SessionManagerImpl,
    private val deviceIdentity: DeviceIdentityManager,
    private val tokenStorage: TokenStorage,
    private val signer: DeviceCryptoSigner,
    private val totpStorage: TotpStorage,
) : AuthRepository {

    override suspend fun registerUser(
        request: RegisterRequest
    ): Resource<RegisterResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.registerUser(request)
            if (response.isSuccessful) {
                response.body()?.let { body ->
                    Resource.Success(body)
                } ?: Resource.Error("Empty response body")
            } else {
                val errorMessage = parseError(response)
                Resource.Error(errorMessage, response.code())
            }
        } catch (e: IOException) {
            Resource.Error("Network error: ${e.message}")
        } catch (e: HttpException) {
            Resource.Error("Server error: ${e.message}")
        } catch (e: Exception) {
            Resource.Error("Unexpected error: ${e.localizedMessage}")
        }
    }

    override suspend fun verifyCode(
        request: VerifyCodeRequest
    ): Resource<BaseResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.verifyCode(request)
            if (response.isSuccessful) {
                response.body()?.let { body ->
                    Resource.Success(body)
                } ?: Resource.Error("Empty response body")
            } else {
                val errorMessage = parseError(response)
                Resource.Error(errorMessage, response.code())
            }
        } catch (e: IOException) {
            Resource.Error("Network error: ${e.message}")
        } catch (e: HttpException) {
            Resource.Error("Server error: ${e.message}")
        } catch (e: Exception) {
            Resource.Error("Unexpected error: ${e.localizedMessage}")
        }
    }

    override suspend fun requestCode(
        request: RequestCodeRequest
    ): Resource<BaseResponse> = withContext(Dispatchers.IO) {

        try {
            val response = api.requestCode(request)

            if (response.isSuccessful) {
                response.body()?.let { body ->
                    Resource.Success(body)
                } ?: Resource.Error("Empty response body")
            } else {
                val errorMessage = parseError(response)
                Resource.Error(errorMessage, response.code())
            }

        } catch (e: IOException) {
            Resource.Error("Network error: ${e.message}")
        } catch (e: HttpException) {
            Resource.Error("Server error: ${e.message}")
        } catch (e: Exception) {
            Resource.Error("Unexpected error: ${e.localizedMessage}")
        }
    }

    override suspend fun onboarding(
        request: OnboardingRequest
    ): Resource<OnboardingResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.onboarding(request = request, platform = "android", uuid = request.device_uuid)
            if (response.isSuccessful) {
                response.body()?.let { body ->
                    body.totp_secret?.let {
                        totpStorage.saveSecret(it)
                    }
                    Resource.Success(body)
                } ?: Resource.Error("Empty response body")
            } else {
                val errorMessage = parseError(response)
                Resource.Error(errorMessage, response.code())
            }
        } catch (e: IOException) {
            Resource.Error("Network error: ${e.message}")
        } catch (e: HttpException) {
            Resource.Error("Server error: ${e.message}")
        } catch (e: Exception) {
            Resource.Error("Unexpected error: ${e.localizedMessage}")
        }
    }

    override suspend fun userStatus(rut: String): Resource<UserStatusResponse> = withContext(Dispatchers.IO) {
        try {
            val request = UserStatusRequest(
                rut = rut,
                device_uuid = deviceIdentity.getOrCreateDeviceUUID(),
                device_info = deviceIdentity.getDeviceInfo()
            )
            val response = api.getUserStatus(request)
            if (response.isSuccessful) {
                response.body()?.let { body ->
                    Resource.Success(body)
                } ?: Resource.Error("Empty response body")
            } else {
                val errorMessage = parseError(response)
                Resource.Error(errorMessage, response.code())
            }
        } catch (e: IOException) {
            Resource.Error("Network error: ${e.message}")
        } catch (e: HttpException) {
            Resource.Error("Server error: ${e.message}")
        } catch (e: Exception) {
            Resource.Error("Unexpected error: ${e.localizedMessage}")
        }
    }

    // Login

    override suspend fun login(): Resource<LoginResponse> = withContext(Dispatchers.IO) {
        try {

            if (!deviceIdentity.isDeviceEnrolled()) return@withContext Resource.Error("No login")
            val deviceUuid = deviceIdentity.getOrCreateDeviceUUID()
            val signature = signer.signLogin(deviceUuid)

            val response = api.login(
                LoginRequest(
                    device_uuid = deviceUuid,
                    timestamp = signature.timestamp,
                    signature = signature.signatureBase64
                )
            )
            if (response.isSuccessful) {
                response.body()?.let { body ->
                    if (body.success) {
                        body.totp_secret?.let {
                            totpStorage.saveSecret(it)
                        }
                        val expiresAt = parseIsoDate(body.expires_at)

                        val session = AuthSessionResponse(
                            accessToken = body.session_token,
                            refreshToken = body.refresh_token,
                            expiresAt = expiresAt,
                        )

                        val userDataResponse = UserDataResponse(
                            full_name = body.user?.full_name ?: "",
                            rut =body.user?.rut ?: "",
                            email =body.user?.email?: ""
                        )
                        sessionManagerImpl.updateSession(session)
                        sessionManagerImpl.updateUserData(userDataResponse)
                        Resource.Success(body)
                    } else {
                        Resource.Error("No login")
                    }
                } ?: Resource.Error("Empty response body")
            } else {
                val errorMessage = parseError(response)
                Resource.Error(errorMessage, response.code())
            }
        } catch (e: IOException) {
            Resource.Error("Network error: ${e.message}")
        } catch (e: HttpException) {
            Resource.Error("Server error: ${e.message}")
        } catch (e: Exception) {
            Resource.Error("Unexpected error: ${e.localizedMessage}")
        }
    }

    override suspend fun refreshToken(request: RefreshTokenRequest): Resource<AuthSessionResponse> {
        return Resource.Error("Error refreshing token >> ")
        /*return try {
            val response = api.refreshToken(mapOf("refresh_token" to refreshToken))
            if (response.isSuccessful) {
                val body = response.body()!!
                val newSession = AuthSession(
                    accessToken = body.accessToken,
                    refreshToken = body.refreshToken,
                    expiresAt = System.currentTimeMillis() + (body.expiresIn * 1000)
                )
                sessionManager.updateSession(newSession)
                Resource.Success(newSession)
            } else {
                Resource.Error("Error refreshing token")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Unexpected error")
        }*/
    }

    override suspend fun logout(): Result<Unit> {
        return runCatching {
            api.logout()
            deviceIdentity.clear()
            sessionManagerImpl.clearSession()
            tokenStorage.clear()
        }
    }
}