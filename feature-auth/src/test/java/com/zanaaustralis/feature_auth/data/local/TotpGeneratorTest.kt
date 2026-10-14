package com.zanaaustralis.feature_auth.data.local

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import java.time.Instant

/** Vectores de RFC 6238 (HMAC-SHA1), truncados a 6 dígitos. */
class TotpGeneratorTest {

    // Base32 de "12345678901234567890"
    private val rfcSecret = "GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ"
    private val generator = TotpGenerator()

    @Test
    fun `matches rfc 6238 test vectors`() {
        assertEquals("287082", generator.generate(rfcSecret, Instant.ofEpochSecond(59)))
        assertEquals("081804", generator.generate(rfcSecret, Instant.ofEpochSecond(1111111109)))
        assertEquals("050471", generator.generate(rfcSecret, Instant.ofEpochSecond(1111111111)))
        assertEquals("005924", generator.generate(rfcSecret, Instant.ofEpochSecond(1234567890)))
    }

    @Test
    fun `same 30 second window gives the same code and the next window changes it`() {
        val start = Instant.ofEpochSecond(1_700_000_010)

        assertEquals(generator.generate(rfcSecret, start), generator.generate(rfcSecret, start.plusSeconds(19)))
        assertNotEquals(generator.generate(rfcSecret, start), generator.generate(rfcSecret, start.plusSeconds(30)))
    }

    @Test
    fun `codes are always six digits with leading zeros`() {
        val code = generator.generate(rfcSecret, Instant.ofEpochSecond(1111111109))

        assertEquals(6, code.length)
        assertEquals('0', code.first())
    }
}
