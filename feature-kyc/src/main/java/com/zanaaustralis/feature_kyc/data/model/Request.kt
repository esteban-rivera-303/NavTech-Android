package com.zanaaustralis.feature_kyc.data.model

import com.zanaaustralis.feature_kyc.data.ValidationPurpose

/**
 * @author esteban.rivera01
 */

data class StartValidationRequest(
    val purpose: ValidationPurpose,
    val validation_method: String,
    val device_uuid: String
)

data class VerifyValidationRequest(
    val validation_id: String,
    val verification_data: VerificationData
)

data class VerificationData(
    val biometric_token: String,
    val device_uuid: String
)
