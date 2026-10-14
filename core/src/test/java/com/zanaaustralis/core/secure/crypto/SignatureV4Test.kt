package com.zanaaustralis.core.secure.crypto

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class SignatureV4Test {

    private val deviceUuid = "4f1c2a9e-7d3b-4c55-9a10-2b6e8f0d1c3a"
    private val challengeId = "9b2d7e41-0c6f-4a8e-b3d5-6f1a2c4e8b90"
    private val nonce = "c7e0b5a2-3f19-4d6c-8e27-a41b9d5f0e63"

    @Test
    fun `dataHash is base64url sha256 without padding`() {
        assertEquals("ungWv48Bz-pBQUDeXa4iI7ADYaOWF3qctBD_YfIAFa0", SignatureV4.dataHash("abc"))
    }

    @Test
    fun `canonical joins all fields in the documented order`() {
        val canonical = SignatureV4.canonical(
            deviceUuid = deviceUuid,
            challengeId = challengeId,
            response = "approved",
            timestamp = 1_700_000_000_000L,
            nonce = nonce,
            data = "abc"
        )

        assertEquals(
            "v4:$deviceUuid:$challengeId:approved:1700000000000:$nonce:" +
                "ungWv48Bz-pBQUDeXa4iI7ADYaOWF3qctBD_YfIAFa0",
            canonical
        )
    }

    @Test
    fun `canonical changes when the signed data changes`() {
        val base = SignatureV4.canonical(deviceUuid, challengeId, "approved", 1L, nonce, "data-1")
        val tampered = SignatureV4.canonical(deviceUuid, challengeId, "approved", 1L, nonce, "data-2")

        assertNotEquals(base, tampered)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `canonical rejects fields containing the separator`() {
        SignatureV4.canonical(deviceUuid, "id:injected", "approved", 1L, nonce, "abc")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `canonical rejects blank nonce`() {
        SignatureV4.canonical(deviceUuid, challengeId, "approved", 1L, " ", "abc")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `canonical rejects unknown response values`() {
        SignatureV4.canonical(deviceUuid, challengeId, "maybe", 1L, nonce, "abc")
    }
}
