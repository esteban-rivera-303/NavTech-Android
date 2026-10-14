package com.zanaaustralis.feature_challanges.data.model

/**
 * @author esteban.rivera01
 */
data class ChallengeDto(
    val id: String,
    val user_id: String,
    val application_id: String,
    val challenge_type_id: String,
    val description: String,
    val challenge_amount: Double?,
    val status: String,
    val created_at: String,
    val expires_at: String,
    val challenge_types: ChallengeTypeDto,
    val applications: ApplicationDto,
    val risk_level: String,
    val context_data : ContextData,
)

data class ChallengeTypeDto(
    val name: String,
    val description: String,
    val risk_level: String,
    val requires_pin: Boolean,
    val requires_geolocation: Boolean,
    val requires_biometric: Boolean,
    val requires_totp: Boolean
)

data class ContextData(
    val description: String
)
data class ApplicationDto(
    val name: String
)
