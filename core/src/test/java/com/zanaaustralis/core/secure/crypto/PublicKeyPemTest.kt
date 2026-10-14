package com.zanaaustralis.core.secure.crypto

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import java.security.KeyPairGenerator
import java.util.Base64

class PublicKeyPemTest {

    @Test
    fun `toPem wraps the X509 encoding between PEM markers`() {
        val publicKey = KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }
            .generateKeyPair().public

        val lines = publicKey.toPem().lines()

        assertEquals(3, lines.size)
        assertEquals("-----BEGIN PUBLIC KEY-----", lines[0])
        assertEquals("-----END PUBLIC KEY-----", lines[2])
        assertArrayEquals(publicKey.encoded, Base64.getDecoder().decode(lines[1]))
    }
}
