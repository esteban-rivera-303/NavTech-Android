package com.zanaaustralis.core.data.remote

import com.zanaaustralis.core.data.remote.model.BaseResponse
import com.zanaaustralis.core.data.remote.model.LoginRequest
import com.zanaaustralis.core.data.remote.model.LoginResponse
import com.zanaaustralis.core.data.remote.model.LogoutResponse
import com.zanaaustralis.core.data.remote.model.MarkReadRequest
import com.zanaaustralis.core.data.remote.model.MarkReadResponse
import com.zanaaustralis.core.data.remote.model.NotificationHistoryResponse
import com.zanaaustralis.core.data.remote.model.OnboardingRequest
import com.zanaaustralis.core.data.remote.model.OnboardingResponse
import com.zanaaustralis.core.data.remote.model.RefreshResponse
import com.zanaaustralis.core.data.remote.model.RefreshTokenRequest
import com.zanaaustralis.core.data.remote.model.RegisterRequest
import com.zanaaustralis.core.data.remote.model.RegisterResponse
import com.zanaaustralis.core.data.remote.model.RequestCodeRequest
import com.zanaaustralis.core.data.remote.model.UserStatusRequest
import com.zanaaustralis.core.data.remote.model.UserStatusResponse
import com.zanaaustralis.core.data.remote.model.VerifyCodeRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * @author esteban.rivera01
 */

interface AuthApiService {

    @POST("user-status")
    suspend fun getUserStatus(
        @Body request: UserStatusRequest
    ): Response<UserStatusResponse>

    @POST("pre-register")
    suspend fun registerUser(
        @Body request: RegisterRequest
    ): Response<RegisterResponse>

    @POST("verify-code")
    suspend fun verifyCode(
        @Body request: VerifyCodeRequest
    ): Response<BaseResponse>

    @POST("request")
    suspend fun requestCode(
        @Body request: RequestCodeRequest
    ): Response<BaseResponse>

    @POST("onboarding")
    suspend fun onboarding(
        @Body request: OnboardingRequest,
        @Header("X-Platform") platform: String = "android",
        @Header("X-Device-UUID") uuid: String
    ): Response<OnboardingResponse>

    @POST("login")
    suspend fun login(
        @Body body: LoginRequest,
        @Header("X-Platform") platform: String = "android",
    ): Response<LoginResponse>

    @POST("logout")
    suspend fun logout(): Response<LogoutResponse>


    @POST("refresh")
    suspend fun refresh(
        @Body body: RefreshTokenRequest,
        @Header("X-Platform") platform: String = "android",
    ): Response<RefreshResponse>


    // Esto no deberia ir aca o.o

    @GET("notifications/history")
    suspend fun getNotificationHistory(
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): Response<NotificationHistoryResponse>

    @POST("notifications/mark-read")
    suspend fun markNotificationsAsRead(
        @Body body: MarkReadRequest
    ): Response<MarkReadResponse>

    @POST("notifications/mark-all-read")
    suspend fun markAllNotificationsAsRead(): Response<MarkReadResponse>


}
