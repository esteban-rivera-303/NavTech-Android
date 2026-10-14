package com.zanaaustralis.feature_auth.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.security.MessageDigest
import java.util.Base64

class PinHasherTest {

    private val fastIterations = 1_000

    @Test
    fun `verify returns true for the same pin`() {
        val encoded = PinHasher.hash("123456", iterations = fastIterations)

        assertTrue(PinHasher.verify("123456", encoded))
    }

    @Test
    fun `verify returns false for a different pin`() {
        val encoded = PinHasher.hash("123456", iterations = fastIterations)

        assertFalse(PinHasher.verify("654321", encoded))
    }

    @Test
    fun `hash uses a random salt so equal pins produce different values`() {
        val first = PinHasher.hash("123456", iterations = fastIterations)
        val second = PinHasher.hash("123456", iterations = fastIterations)

        assertNotEquals(first, second)
    }

    @Test
    fun `encoded value stores algorithm and iteration count`() {
        val encoded = PinHasher.hash("1234", iterations = fastIterations)

        val parts = encoded.split(":")
        assertEquals(4, parts.size)
        assertEquals("pbkdf2_sha256", parts[0])
        assertEquals(fastIterations.toString(), parts[1])
    }

    @Test
    fun `verify returns false for malformed stored values`() {
        assertFalse(PinHasher.verify("1234", ""))
        assertFalse(PinHasher.verify("1234", "pbkdf2_sha256:abc:salt:hash"))
        assertFalse(PinHasher.verify("1234", "md5:1000:c2FsdA==:aGFzaA=="))
        assertFalse(PinHasher.verify("1234", "pbkdf2_sha256:1000:%%%:aGFzaA=="))
    }

    @Test
    fun `verifyLegacySha256 accepts hashes from the previous scheme`() {
        val salt = Base64.getEncoder().encodeToString(ByteArray(16) { it.toByte() })
        val legacyHash = Base64.getEncoder().encodeToString(
            MessageDigest.getInstance("SHA-256").digest(("4321" + salt).toByteArray())
        )

        assertTrue(PinHasher.verifyLegacySha256("4321", legacyHash, salt))
        assertFalse(PinHasher.verifyLegacySha256("0000", legacyHash, salt))
    }
}
