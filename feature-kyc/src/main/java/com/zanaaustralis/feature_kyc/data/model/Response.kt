package com.zanaaustralis.feature_kyc.data.model

/**
 * @author esteban.rivera01
 */

data class StartValidationResponse(
    val success: Boolean,
    val validation: ValidationInfo,
    val instructionsMessage: String? = null,
    val instructions: ValidationInstructions
)

data class VerifyValidationResponse(
    val success: Boolean,
    val validation: ValidationResult,
    val next_step: NextStep? = null,
    val error: ValidationError? = null
)

data class ValidationResult(
    val id: String,
    val status: String,
    val purpose: String? = null,
    val completed_at: String? = null,
    val remaining_attempts: Int? = null,
    val failure_reason: String? = null
)

data class NextStep(
    val action: String,
    val message: String,
    val token: String
)

data class ValidationError(
    val code: String,
    val message: String
)

data class ValidationStatusResponse(
    val success: Boolean,
    val validation: ValidationStatus
)

data class ValidationInfo(
    val id: String,
    val purpose: String?,
    val status: String,
    val expires_at: String?,
    val max_attempts: Int?,
    val remaining_attempts: Int,
    val verification_url: String?,
)

data class ValidationStatus(
    val id: String,
    val purpose: String,
    val status: String,
    val validation_method: String,
    val attempt_count: Int,
    val remaining_attempts: Int,
    val expires_at: String,
    val created_at: String,
    val is_expired: Boolean
)

data class ValidationInstructions(
    val message: String,
    val timeout_seconds: Int
)




