package com.zanaaustralis.core.secure.crypto

import java.security.MessageDigest
import java.util.Base64

/**
 * @author esteban.rivera01
 */

/**
 * Firma v4 de respuesta a desafíos (ver docs/BACKEND_SECURITY_CHANGES.md, B1):
 *
 * `v4:{device_uuid}:{challenge_id}:{response}:{timestamp}:{nonce}:{data_hash}`
 *
 * `data_hash` = base64url sin padding de SHA-256 sobre el string `data` exactamente como se envía,
 * de modo que el servidor no necesita canonicalizar JSON para verificar el payload completo.
 */
object SignatureV4 {

    const val VERSION = 4

    private const val PREFIX = "v4"
    private const val SEPARATOR = ":"
    private val ALLOWED_RESPONSES = setOf("approved", "rejected")

    fun dataHash(data: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(data.toByteArray(Charsets.UTF_8))
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest)
    }

    fun canonical(
        deviceUuid: String,
        challengeId: String,
        response: String,
        timestamp: Long,
        nonce: String,
        data: String
    ): String {
        require(response in ALLOWED_RESPONSES) { "Unsupported challenge response: $response" }
        listOf("deviceUuid" to deviceUuid, "challengeId" to challengeId, "nonce" to nonce)
            .forEach { (name, value) -> requireField(name, value) }

        return listOf(PREFIX, deviceUuid, challengeId, response, timestamp.toString(), nonce, dataHash(data))
            .joinToString(SEPARATOR)
    }

    private fun requireField(name: String, value: String) {
        require(value.isNotBlank()) { "$name must not be blank" }
        require(!value.contains(SEPARATOR)) { "$name must not contain '$SEPARATOR'" }
    }
}
