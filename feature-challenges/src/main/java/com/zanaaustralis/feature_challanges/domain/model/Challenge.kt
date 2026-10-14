package com.zanaaustralis.feature_challanges.domain.model

import android.os.Build
import androidx.annotation.RequiresApi
import com.zanaaustralis.core.common.parseIsoToMillis
import com.zanaaustralis.core.secure.SecurityFlags
import com.zanaaustralis.feature_auth.AuthFactor
import com.zanaaustralis.feature_challanges.data.model.ChallengeDto
import java.time.Duration
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * @author esteban.rivera01
 */

data class ChallengeItem(
    val id: String,
    val title: String,
    val description: String?,
    val date: String,
    val isCritical: Boolean = false,
    val status: String = "pendiente",
    val appName: String,
    val amount: Double?,
    val requiresBiometric: Boolean,
    val requiresTotp: Boolean,
    val requiresLocation: Boolean,
    val requiresPin: Boolean = true,
    val createdAt: String,
    val expiresAt: String,
    val risk_level: String,
    val is_current_device: Boolean = false,
    val authorizedFactors: Set<AuthFactor> = emptySet()

){
    val expiresAtMillis: Long
        get() = parseIsoToMillis(expiresAt)
}

fun ChallengeItem.requiredFactors(): List<AuthFactor> {
    val factors = mutableListOf<AuthFactor>()

    if (requiresBiometric) factors.add(AuthFactor.BIOMETRIC)

    // TODO cambiar aca si el PIN siempre se pedirá
    //factors.add(AuthFactor.PIN)

    //if (requiresTotp) factors.add(AuthFactor.TOTP)
    if (requiresLocation) factors.add(AuthFactor.LOCATION)
    if (requiresPin) factors.add(AuthFactor.PIN)
    // if (requiresPin) factors.add(AuthFactor.LOCATION)

    return factors
}




fun ChallengeItem.markFactorAsAuthorized(factor: AuthFactor): ChallengeItem {
    val updatedFactors = authorizedFactors + factor

    val allFactorsAuthorized =
        requiredFactors().all { it in updatedFactors }

    return copy(
        authorizedFactors = updatedFactors,
        status = if (allFactorsAuthorized) "enviado" else "pendiente"
    )
}


fun ChallengeItem.expiresInSeconds(
    now: Instant = Instant.now()
): Long {
    return try {
        val expiresInstant = Instant.parse(expiresAt)
        Duration.between(now, expiresInstant)
            .seconds
            .coerceAtLeast(0)
    } catch (e: Exception) {
        0L
    }
}


/**
 * Con la firma v4 la biometría no es un paso previo: se valida al firmar la aprobación
 * (BiometricPrompt + CryptoObject), por eso no cuenta como factor pendiente.
 */
fun ChallengeItem.needsBiometricSignature(): Boolean =
    SecurityFlags.signatureV4Enabled && requiresBiometric

fun ChallengeItem.missingFactors(): List<AuthFactor> {
    return requiredFactors()
        .filterNot { it in authorizedFactors }
        .filterNot { it == AuthFactor.BIOMETRIC && needsBiometricSignature() }
}


fun ChallengeItem.isReadyToAuthorize(): Boolean = missingFactors().isEmpty()




@RequiresApi(Build.VERSION_CODES.O)
fun ChallengeDto.toChallengeItem(): ChallengeItem {
    return ChallengeItem(
        id = id,
        title = applications.name,
        description = context_data.description,
        date = formatTimeAgo(created_at),
        isCritical = risk_level == "high",
        status = when (status.lowercase()) {
            "pending" -> "pendiente"
            "approved" -> "enviado"
            else -> status
        },
        createdAt = created_at,
        expiresAt = expires_at,
        requiresPin = challenge_types.requires_pin,
        requiresLocation = challenge_types.requires_geolocation,
        requiresBiometric = challenge_types.requires_biometric,
        requiresTotp = challenge_types.requires_totp,
        amount = challenge_amount,
        appName = applications.name,
        risk_level = risk_level,
    )
}


@RequiresApi(Build.VERSION_CODES.O)
fun formatTimeAgo(isoTime: String): String {
    return try {
        val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
        val time = OffsetDateTime.parse(isoTime, formatter)
        val minutes = ChronoUnit.MINUTES.between(time, OffsetDateTime.now())
        when {
            minutes < 1 -> "Hace un momento"
            minutes < 60 -> "Hace $minutes minutos"
            minutes < 1440 -> "Hace ${minutes / 60} horas"
            else -> "Hace ${minutes / 1440} días"
        }
    } catch (e: Exception) {
        "Fecha desconocida"
    }
}


fun String.formatCreatedAt(): String {
    return try {

        val instant = Instant.parse(this)
        val now = Instant.now()

        val minutes = ChronoUnit.MINUTES.between(instant, now)
        val hours = ChronoUnit.HOURS.between(instant, now)
        val days = ChronoUnit.DAYS.between(instant, now)

        when {
            minutes < 1 -> "Ahora"
            minutes < 60 -> "Hace $minutes min"
            hours < 24 -> "Hace $hours h"
            days == 1L -> "Ayer"
            days < 7 -> "Hace $days días"
            else -> {
                val date = ZonedDateTime.ofInstant(
                    instant,
                    ZoneId.systemDefault()
                )
                "${date.dayOfMonth} ${date.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }}"
            }
        }

    } catch (e: Exception) {
        ""
    }
}