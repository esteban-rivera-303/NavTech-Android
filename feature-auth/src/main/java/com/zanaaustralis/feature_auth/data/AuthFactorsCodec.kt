package com.zanaaustralis.feature_auth.data

import com.zanaaustralis.feature_auth.AuthFactor

/**
 * @author esteban.rivera01
 */

/**
 * Serializa los factores validados localmente junto con el momento de validación,
 * para que expiren y no puedan reutilizarse indefinidamente.
 * Formato: `<epochMillis>|PIN,BIOMETRIC`.
 */
object AuthFactorsCodec {

    private const val FIELD_SEPARATOR = "|"
    private const val FACTOR_SEPARATOR = ","

    fun encode(factors: Set<AuthFactor>, validatedAtMillis: Long): String =
        validatedAtMillis.toString() + FIELD_SEPARATOR +
            factors.joinToString(FACTOR_SEPARATOR) { it.name }

    fun decode(raw: String?, nowMillis: Long, ttlMillis: Long): Set<AuthFactor> {
        if (raw == null) return emptySet()

        val parts = raw.split(FIELD_SEPARATOR, limit = 2)
        if (parts.size != 2) return emptySet()

        val validatedAt = parts[0].toLongOrNull() ?: return emptySet()
        val age = nowMillis - validatedAt
        if (age < 0 || age > ttlMillis) return emptySet()

        return parts[1].split(FACTOR_SEPARATOR)
            .mapNotNull { name -> AuthFactor.entries.firstOrNull { it.name == name } }
            .toSet()
    }
}
