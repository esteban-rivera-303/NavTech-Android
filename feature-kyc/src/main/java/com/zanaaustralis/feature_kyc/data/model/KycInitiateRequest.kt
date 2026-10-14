package com.zanaaustralis.feature_kyc.data.model

/**
 * @author esteban.rivera01
 */

data class KycMetadata(
    val source: String
)

data class KycInitiateRequest(
    val rut: String,
    val metadata: KycMetadata
)