package com.zanaaustralis.feature_auth.data

import com.zanaaustralis.feature_auth.AuthFactor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthFactorsCodecTest {

    private val now = 1_700_000_000_000L
    private val ttl = 5 * 60 * 1000L

    @Test
    fun `decode returns the factors encoded within the ttl`() {
        val raw = AuthFactorsCodec.encode(setOf(AuthFactor.PIN, AuthFactor.BIOMETRIC), now)

        val decoded = AuthFactorsCodec.decode(raw, now + ttl - 1, ttl)

        assertEquals(setOf(AuthFactor.PIN, AuthFactor.BIOMETRIC), decoded)
    }

    @Test
    fun `decode returns empty set once the ttl has expired`() {
        val raw = AuthFactorsCodec.encode(setOf(AuthFactor.PIN), now)

        assertTrue(AuthFactorsCodec.decode(raw, now + ttl + 1, ttl).isEmpty())
    }

    @Test
    fun `decode rejects timestamps in the future`() {
        val raw = AuthFactorsCodec.encode(setOf(AuthFactor.PIN), now + ttl)

        assertTrue(AuthFactorsCodec.decode(raw, now, ttl).isEmpty())
    }

    @Test
    fun `decode ignores unknown factor names`() {
        val decoded = AuthFactorsCodec.decode("$now|PIN,HACKED", now, ttl)

        assertEquals(setOf(AuthFactor.PIN), decoded)
    }

    @Test
    fun `decode returns empty set for null or malformed values`() {
        assertTrue(AuthFactorsCodec.decode(null, now, ttl).isEmpty())
        assertTrue(AuthFactorsCodec.decode("PIN,BIOMETRIC", now, ttl).isEmpty())
        assertTrue(AuthFactorsCodec.decode("abc|PIN", now, ttl).isEmpty())
    }

    @Test
    fun `encode with no factors decodes to empty set`() {
        val raw = AuthFactorsCodec.encode(emptySet(), now)

        assertTrue(AuthFactorsCodec.decode(raw, now, ttl).isEmpty())
    }
}
