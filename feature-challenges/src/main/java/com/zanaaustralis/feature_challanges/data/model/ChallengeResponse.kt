package com.zanaaustralis.feature_challanges.data.model

import com.zanaaustralis.feature_challanges.domain.model.ChallengeItem

/**
 * @author esteban.rivera01
 */
data class ChallengeResponse(
    val success: Boolean,
    val challenges: List<ChallengeDto>
)

data class ChallengeHistoryResponse(
    val success: Boolean,
    val challenges: List<ChallengeHistoryDto>,
    val pagination: PaginationDto
)

data class ChallengeHistoryDto(
    val id: String,
    val application: ApplicationDto,
    val challenge_type: ChallengeTypeDto,
    val status: String,
    val challenge_data: Map<String, Any>?,
    val response_data: Map<String, Any>?,
    val created_at: String,
    val expires_at: String,
    val responded_at: String?
)


data class PaginationDto(
    val total: Int,
    val limit: Int,
    val offset: Int,
    val has_more: Boolean
)

fun ChallengeHistoryDto.toChallengeItem(): ChallengeItem {
    return ChallengeItem(
        id = id,
        title = challenge_type.name,
        description = challenge_type.description,
        date = created_at,
        isCritical = challenge_type.risk_level == "High",
        status = status,
        appName = application.name,
        amount = null, // si no viene en challenge_data
        requiresBiometric = false,
        requiresTotp = false,
        requiresLocation = false,
        requiresPin = false,
        createdAt = created_at,
        expiresAt = expires_at,
        risk_level = challenge_type.risk_level,
        authorizedFactors = emptySet()
    )
}