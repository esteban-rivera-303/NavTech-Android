package com.zanaaustralis.core.data.remote

import com.zanaaustralis.core.data.remote.model.AccountRecoveryCompleteRequest
import com.zanaaustralis.core.data.remote.model.AccountRecoveryCompleteResponse
import com.zanaaustralis.core.data.remote.model.AccountRecoveryInitiateRequest
import com.zanaaustralis.core.data.remote.model.AccountRecoveryInitiateResponse
import com.zanaaustralis.core.data.remote.model.AccountRecoveryVerifyRequest
import com.zanaaustralis.core.data.remote.model.AccountRecoveryVerifyRequestCodeRequest
import com.zanaaustralis.core.data.remote.model.AccountRecoveryVerifyResponse
import com.zanaaustralis.core.data.remote.model.AccountStatusRequest
import com.zanaaustralis.core.data.remote.model.RecoveryEnrollRequest
import com.zanaaustralis.core.data.remote.model.RecoveryEnrollResponse
import com.zanaaustralis.core.data.remote.model.RecoveryStatusRequestCodeResponse
import com.zanaaustralis.core.data.remote.model.RecoveryStatusResponse
import com.zanaaustralis.core.data.remote.model.TrustedDeviceReactivateRequest
import com.zanaaustralis.core.data.remote.model.TrustedDeviceReactivateResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * @author esteban.rivera01
 */
interface AccountRecoveryApi {

    @POST("account-recovery/initiate")
    suspend fun initiateRecovery(
        @Body body: AccountRecoveryInitiateRequest,
        @Header("X-Platform") platform: String = "android",
        @Header("X-Device-UUID") deviceUuid: String
    ): Response<AccountRecoveryInitiateResponse>

    @POST("account-recovery/verify-email")
    suspend fun verifyEmailRecovery(
        @Body body: AccountRecoveryVerifyRequest,
        @Header("X-Platform") platform: String = "android"
    ): Response<AccountRecoveryVerifyResponse>

    @POST("account-recovery/verify-phone")
    suspend fun verifyPhoneRecovery(
        @Body body: AccountRecoveryVerifyRequest,
        @Header("X-Platform") platform: String = "android"
    ): Response<AccountRecoveryVerifyResponse>

    @POST("account-recovery/complete")
    suspend fun completeRecovery(
        @Body body: AccountRecoveryCompleteRequest,
        @Header("X-Platform") platform: String = "android"
    ): Response<AccountRecoveryCompleteResponse>


    @POST("trusted-device-reactivate")
    suspend fun reactivateTrustedDevice(
        @Body request: TrustedDeviceReactivateRequest,
        @Header("X-Platform") platform: String = "android"
    ): Response<TrustedDeviceReactivateResponse>

    @POST("recovery/enroll-device")
    suspend fun recoveryEnrollDevice(
        @Body request: RecoveryEnrollRequest,
        @Header("X-Platform") platform: String = "android"
    ): Response<RecoveryEnrollResponse>

    @POST("account-recovery/status")
    suspend fun recoveryStatus(
        @Body request: AccountStatusRequest,
        @Header("X-Platform") platform: String = "android"
    ): Response<RecoveryStatusResponse>

    @POST("account-recovery/resend-code")
    suspend fun resendCode(
        @Body request: AccountRecoveryVerifyRequestCodeRequest,
        @Header("X-Platform") platform: String = "android"
    ): Response<RecoveryStatusRequestCodeResponse>
}