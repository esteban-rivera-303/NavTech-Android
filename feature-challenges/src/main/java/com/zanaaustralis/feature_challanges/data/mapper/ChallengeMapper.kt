package com.zanaaustralis.feature_challanges.data.mapper

import com.zanaaustralis.feature_challanges.data.model.ChallengeDto
import com.zanaaustralis.feature_challanges.domain.model.ChallengeItem

/**
 * @author esteban.rivera01
 */

fun ChallengeDto.toDomain(): ChallengeItem {
    return ChallengeItem(
        id = id,
        title = challenge_types.name,
        description = description,
        date = created_at,
        isCritical = challenge_types.requires_biometric,
        status = status,
        appName = applications.name,
        amount = null,
        requiresBiometric = challenge_types.requires_biometric,
        requiresTotp = challenge_types.requires_totp,
        requiresLocation = challenge_types.requires_geolocation,
        createdAt = created_at,
        expiresAt = expires_at,
        risk_level = risk_level
    )
}
