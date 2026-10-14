package com.zanaaustralis.feature_auth.data

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * @author esteban.rivera01
 */

/**
 * Hash de PIN con PBKDF2-HMAC-SHA256.
 * Formato almacenado: `pbkdf2_sha256:<iteraciones>:<salt base64>:<hash base64>`.
 */
object PinHasher {

    const val DEFAULT_ITERATIONS = 60_000

    private const val SCHEME = "pbkdf2_sha256"
    private const val ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val SEPARATOR = ":"
    private const val SALT_BYTES = 16
    private const val KEY_LENGTH_BITS = 256
    private const val ENCODED_PARTS = 4

    private val encoder = Base64.getEncoder()
    private val decoder = Base64.getDecoder()

    fun hash(
        pin: String,
        iterations: Int = DEFAULT_ITERATIONS,
        salt: ByteArray = randomSalt()
    ): String {
        val derived = derive(pin, salt, iterations)
        return listOf(SCHEME, iterations.toString(), encoder.encodeToString(salt), encoder.encodeToString(derived))
            .joinToString(SEPARATOR)
    }

    fun verify(pin: String, encoded: String): Boolean {
        val parts = encoded.split(SEPARATOR)
        if (parts.size != ENCODED_PARTS || parts[0] != SCHEME) return false

        val iterations = parts[1].toIntOrNull()?.takeIf { it > 0 } ?: return false
        val salt = decodeOrNull(parts[2]) ?: return false
        val expected = decodeOrNull(parts[3]) ?: return false

        return MessageDigest.isEqual(derive(pin, salt, iterations), expected)
    }

    /** Verifica el esquema anterior `SHA-256(pin + salt)` para migrar PINs existentes. */
    fun verifyLegacySha256(pin: String, legacyHash: String, legacySalt: String): Boolean {
        val expected = decodeOrNull(legacyHash) ?: return false
        val actual = MessageDigest.getInstance("SHA-256")
            .digest((pin + legacySalt).toByteArray(Charsets.UTF_8))
        return MessageDigest.isEqual(actual, expected)
    }

    private fun derive(pin: String, salt: ByteArray, iterations: Int): ByteArray {
        val spec = PBEKeySpec(pin.toCharArray(), salt, iterations, KEY_LENGTH_BITS)
        return try {
            SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }

    private fun randomSalt(): ByteArray =
        ByteArray(SALT_BYTES).also { SecureRandom().nextBytes(it) }

    private fun decodeOrNull(value: String): ByteArray? =
        runCatching { decoder.decode(value) }.getOrNull()
}
