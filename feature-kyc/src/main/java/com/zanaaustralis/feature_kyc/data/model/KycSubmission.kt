package com.zanaaustralis.feature_kyc.data.model

/**
 * @author esteban.rivera01
 */

data class KycSubmission(
    val id: String,
    val provider: String,
    val status: String,
    val provider_verification_url: String?,
    val verification_completed_at: String?,
    val rejection_reason: String?,
    val created_at: String,
    val reviewed_at: String?
)
