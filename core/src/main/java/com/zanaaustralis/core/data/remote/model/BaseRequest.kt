package com.zanaaustralis.core.data.remote.model

/**
 * @author esteban.rivera01
 */

data class UserStatusRequest(
    val rut: String,
    val device_uuid: String?,
    val device_info: DeviceInfo
)

data class DeviceInfo(
    val hardware_id: String,
    val manufacturer: String,
    val model: String,
    val os: String,
    val os_version: String
)

data class RegisterRequest(
    val rut: String,
    val email: String,
    val phone: String
)

data class VerifyCodeRequest(
    val type: String, // "email" o "phone"
    val email: String? = null,
    val phone: String? = null,
    val code: String
)

data class RequestCodeRequest(
    val type: String, // "email" o "phone"
    val email: String? = null,
    val phone: String? = null
)

data class OnboardingRequest(
    val rut: String,
    val device_uuid: String,
    val os: String,
    val model: String,
    val fingerprint: String,
    val platform: String,
    val public_key: String,
    val device_info: DeviceInfo,
    val fcm_token: String,
    val user_auth_public_key: String? = null,
)

// Login
data class LoginRequest(
    val device_uuid: String,
    val timestamp: Long,
    val signature: String
)

data class RefreshTokenRequest(
    val refresh_token: String
)

// Recovery
data class AccountRecoveryInitiateRequest(
    val rut: String,
    val device_uuid: String
)

data class AccountRecoveryVerifyRequest(
    val recovery_token: String,
    val code: String
)

data class AccountRecoveryVerifyRequestCodeRequest(
    val type: String, // "email" o "phone"
    val recovery_token: String,
)

data class AccountRecoveryCompleteRequest(
    val recovery_token: String
)

data class TrustedDeviceReactivateRequest(
    val rut: String,
    val device_uuid: String,
    val public_key: String,
    val biometric_verified: Boolean,
    val device_info: DeviceInfo,
    val fcm_token: String,
    val user_auth_public_key: String? = null,
)

data class RecoveryEnrollRequest(
    val rut: String,
    val device_uuid: String,
    val public_key: String,
    val device_info: DeviceInfo,
    val fcm_token: String,
    val user_auth_public_key: String? = null,
)
data class AccountStatusRequest(
    val recovery_token: String,
)
// Notification

data class MarkReadRequest(
    val notification_ids: List<String>
)



