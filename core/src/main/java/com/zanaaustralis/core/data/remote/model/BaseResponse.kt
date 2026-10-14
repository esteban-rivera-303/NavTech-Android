package com.zanaaustralis.core.data.remote.model

import com.google.gson.annotations.SerializedName


/**
 * @author esteban.rivera01
 */

data class BaseResponse(
    val success: Boolean,
    val message: String?
)

data class UserStatusResponse(
    val registered: Boolean,
    val status: String?,
    val next_step: String?,
    val completed_steps: List<String> = emptyList(),
    val user: UserStatusDetail,
    val error: String?,
    val sessionExpiresAt: String?,
    val sessionTimeoutMinutes: Int?
)

data class UserStatusDetail(
    val id: String?,
    val email: String,
    val phone: String,
    val rut: String,
    val email_verified: Boolean,
    val phone_verified: Boolean,
    val kyc_status: String,
    val has_active_device: Boolean?
)

data class RegisterResponse(
    val success: Boolean,
    val user_id: String?,
    val message: String?,
)


data class OnboardingResponse(
    val success: Boolean,
    val message: String?,
    val user_id: String?,
    val device_id: String?,
    val session_token: String?,
    val refresh_token: String?,
    val totp_secret: String?,
    val expires_at: String?
)

// domain session
data class AuthSessionResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresAt: Long,

    //val expiredAtLong: Long?// epoch millis
    //val sessionExpiresAt: String?,
    //val sessionTimeoutMinutes: Long?
)

data class UserDataResponse(
    val full_name: String?,
    val email : String?,
    val rut : String ?
)

// Login

data class LoginResponse(
    val success: Boolean,
    val session_token: String,
    val refresh_token: String,
    val expires_at: String,
    val user_id: String,
    val device_id: String?,
    val totp_secret: String?,
    val has_unread_notifications : Boolean?,
    val un_read_notifications_count : Int?,
    val user :UserDataResponse?,
    val sessionExpiresAt: String?,
    val sessionTimeoutMinutes: Int?
)

// refresh
data class RefreshResponse(
    val session_token: String,
    val refresh_token: String,
    val expires_at: String,
    @SerializedName("session_expires_at")
    val sessionExpiresAt: String?,
    @SerializedName("session_timeout_minutes")
    val sessionTimeoutMinutes: Int?
)

data class LogoutResponse(
    val success: Boolean,
    val message: String
)


data class AccountRecoveryInitiateResponse(
    val success: Boolean,
    val recovery_token: String?,
    val message: String
)

data class AccountRecoveryVerifyResponse(
    val success: Boolean,
    val message: String
)

data class AccountRecoveryCompleteResponse(
    val success: Boolean,
    val message: String
)

data class TrustedDeviceReactivateResponse(
    val success: Boolean,
    val device_status: DeviceStatus,
    val installation_uuid: String,
    val access_token: String,
    val refresh_token: String,
    @SerializedName("expires_at")
    val expiresAt: String,
    val totp_secret: String,
    val sessionExpiresAt: String?,
    val sessionTimeoutMinutes: Int?,
    val user :UserDataResponse?,
)

data class RecoveryEnrollResponse(
    val success: Boolean,
    val device_id: String,
    val access_token: String,
    val refresh_token: String,
    val expires_at: String,
    val totp_secret: String,
    val sessionExpiresAt: String?,
    val sessionTimeoutMinutes: Int?,
    val user :UserDataResponse?,
)


data class RecoveryStatusResponse(
    @SerializedName("email_verified")
    val emailVerified: Boolean,
    @SerializedName("phone_verified")
    val phoneVerified: Boolean,
    @SerializedName("kyc_verified")
    val kycVerified: Boolean,
    val expired: Boolean,
    val completed: Boolean,
    val nextStep: String?
)

data class RecoveryStatusRequestCodeResponse(
    val success: Boolean,
    val message: String,
    val type: String,
)

data class DeviceStatus(
    val is_reinstall: String,
    val was_reactivated: Boolean,
    val first_enrolled_at: String,
    val reinstall_count: Int,
)

data class NotificationHistoryResponse(
    val success: Boolean,
    val notifications: List<NotificationDto>,
    val total: Int,
    val limit: Int,
    val offset: Int,
    val sessionExpiresAt: String?,
    val sessionTimeoutMinutes: Int?
)

data class NotificationDto(
    val id: String,
    val challenge_id: String?,
    val device_id: String?,
    val fcm_token: String?,
    val title: String,
    val body: String,
    val notification_data: NotificationDataDto?,
    val sent_at: String,
    val status: String,
    val error_message: String?,
    val created_at: String,
    val read_at: String,
    val is_read: Boolean,
)

data class NotificationDataDto(
    val challenge_id: String?,
    val application_name: String?,
    val challenge_type: String?,
    val risk_level: String?
)

data class MarkReadResponse(
    val success: Boolean,
    @SerializedName("marked_count")
    val markedCount: Int
)