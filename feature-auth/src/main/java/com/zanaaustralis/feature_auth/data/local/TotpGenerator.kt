package com.zanaaustralis.feature_auth.data.local

/**
 * @author esteban.rivera01
 */

import java.time.Duration
import java.time.Instant
import javax.crypto.spec.SecretKeySpec
import android.util.Base64
import com.eatthepath.otp.TimeBasedOneTimePasswordGenerator
import org.apache.commons.codec.binary.Base32
import javax.inject.Inject

class TotpGenerator @Inject constructor() {

    private val totp = TimeBasedOneTimePasswordGenerator(
        Duration.ofSeconds(30), // periodo
        6 // dígitos
    )

    fun generate(secret: String, at: Instant = Instant.now()): String {

        val base32 = Base32()

        // 👇 decode Base32
        val keyBytes = base32.decode(secret)

        val key = SecretKeySpec(keyBytes, totp.algorithm)

        val code = totp.generateOneTimePassword(
            key,
            at
        )

        return String.format("%06d", code)

    }
}

fun decodeSecret(secret: String): ByteArray {
    return Base32().decode(secret)
}