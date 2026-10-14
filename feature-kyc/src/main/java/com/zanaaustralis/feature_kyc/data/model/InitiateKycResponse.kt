package com.zanaaustralis.feature_kyc.data.model

/**
 * @author esteban.rivera01
 */

data class InitiateKycResponse(
    val success: Boolean,
    val submission_id: String,
    val status: String,
    val verification_url: String?,
    val session_id: String,
    val message: String?,
    val error:String?
)
data class InitiateKycResponseError(
    val submission_id: String,
    val status: String,
    val verification_url: String?,
    val error:String?
)
