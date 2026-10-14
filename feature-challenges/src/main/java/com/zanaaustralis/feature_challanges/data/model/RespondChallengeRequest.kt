package com.zanaaustralis.feature_challanges.data.model

import kotlinx.serialization.Serializable

/**
 * @author esteban.rivera01
 */

@Serializable
data class RespondChallengePayload(
    val challenge_id: String,
    val response: String, // approved | rejected
    val signature: String,
    val totp_code: String? = null,
    val location: LocationData? = null,
    val metadata: Map<String, String>? = null,
    val timestamp: Long?,
)

/**
 * Payload de la firma v4: no incluye la firma, que viaja en el envoltorio y cubre
 * el hash exacto de este JSON codificado (ver SignatureV4).
 */
@Serializable
data class RespondChallengePayloadV4(
    val challenge_id: String,
    val response: String, // approved | rejected
    val timestamp: Long,
    val nonce: String,
    val totp_code: String? = null,
    val location: LocationData? = null,
    val metadata: Map<String, String>? = null,
)

@Serializable
data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Double
)

data class EncryptedRespondRequest(
    val encrypted: Boolean = true,
    val data: String,          // Base64(JSON(payload))
    val device_uuid: String,
    // Solo firma v4 (null → Gson los omite en v3)
    val signature: String? = null,
    val signing_key: String? = null // "device" | "user_auth"
)

data class NonceRequest(
    val device_uuid: String
)

data class NonceResponse(
    val success: Boolean,
    val nonce: String?,
    val timestamp: Long?,
    val expires_in: Long?
)

