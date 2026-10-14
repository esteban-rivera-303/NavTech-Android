package com.zanaaustralis.feature_challanges.data.model

/**
 * @author esteban.rivera01
 */

data class ValidateTotpRequest(
    val device_uuid: String,
    val totp_code: String
)

data class TotpValidateResponse(
    val success: Boolean,
    val valid: Boolean
)
