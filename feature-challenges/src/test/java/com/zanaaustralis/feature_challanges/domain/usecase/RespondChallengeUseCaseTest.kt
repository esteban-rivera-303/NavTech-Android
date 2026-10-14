package com.zanaaustralis.feature_challanges.domain.usecase

import com.zanaaustralis.core.common.Resource
import com.zanaaustralis.core.secure.crypto.DeviceCryptoSigner
import com.zanaaustralis.core.secure.crypto.RespondSignature
import com.zanaaustralis.core.secure.crypto.SignatureV4
import com.zanaaustralis.core.secure.device.DeviceIdentityManager
import com.zanaaustralis.core.session.SessionProvider
import com.zanaaustralis.feature_challanges.data.model.EncryptedRespondRequest
import com.zanaaustralis.feature_challanges.data.model.LocationData
import com.zanaaustralis.feature_challanges.data.model.RespondChallengePayload
import com.zanaaustralis.feature_challanges.data.model.RespondChallengePayloadV4
import com.zanaaustralis.feature_challanges.domain.repository.ChallengeRepository
import com.zanaaustralis.feature_challanges.domain.repository.LEGACY_SIGNATURE_VERSION
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.security.Signature
import java.util.Base64

class RespondChallengeUseCaseTest {

    private val deviceUuid = "device-1"
    private val challengeId = "challenge-1"
    private val token = "access-token"
    private val nonce = "nonce-1"

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = false
        explicitNulls = false
    }

    private val repository = mockk<ChallengeRepository>()
    private val signer = mockk<DeviceCryptoSigner>()
    private val deviceIdentity = mockk<DeviceIdentityManager>()
    private val sessionProvider = mockk<SessionProvider>()
    private val sentRequest = slot<EncryptedRespondRequest>()

    @Before
    fun setUp() {
        every { deviceIdentity.getOrCreateDeviceUUID() } returns deviceUuid
        coEvery { sessionProvider.getAccessToken() } returns token
        coEvery { repository.respondChallenge(any(), capture(sentRequest), any()) } returns Resource.Success(true)
    }

    private fun useCase(v4: Boolean) = RespondChallengeUseCase(
        repository, signer, deviceIdentity, sessionProvider, json, isSignatureV4Enabled = v4
    )

    private fun decode(data: String): String = String(Base64.getDecoder().decode(data), Charsets.UTF_8)

    // ---------- v3 (flag apagado) ----------

    @Test
    fun `v3 approve signs device challenge response and timestamp and sends legacy version`() = runTest {
        every { signer.signRespondChallenge(deviceUuid, challengeId, "approved", any()) } returns RespondSignature("sig-v3")

        val result = useCase(v4 = false).approve(challengeId, totpCode = "123456")

        assertTrue(result is Resource.Success)
        coVerify { repository.respondChallenge(token, any(), LEGACY_SIGNATURE_VERSION) }
        coVerify(exactly = 0) { repository.requestNonce(any()) }

        val request = sentRequest.captured
        assertEquals(deviceUuid, request.device_uuid)
        assertNull(request.signature)
        assertNull(request.signing_key)

        val payload = json.decodeFromString(RespondChallengePayload.serializer(), decode(request.data))
        assertEquals(challengeId, payload.challenge_id)
        assertEquals("approved", payload.response)
        assertEquals("sig-v3", payload.signature)
        assertEquals("123456", payload.totp_code)
        verify { signer.signRespondChallenge(deviceUuid, challengeId, "approved", requireNotNull(payload.timestamp)) }
    }

    @Test
    fun `v3 reject sends rejected response`() = runTest {
        every { signer.signRespondChallenge(any(), any(), "rejected", any()) } returns RespondSignature("sig")

        useCase(v4 = false).reject(challengeId, totpCode = null)

        val payload = json.decodeFromString(RespondChallengePayload.serializer(), decode(sentRequest.captured.data))
        assertEquals("rejected", payload.response)
    }

    @Test
    fun `missing session sends empty token instead of crashing`() = runTest {
        coEvery { sessionProvider.getAccessToken() } returns null
        every { signer.signRespondChallenge(any(), any(), any(), any()) } returns RespondSignature("sig")

        useCase(v4 = false).approve(challengeId, totpCode = null)

        coVerify { repository.respondChallenge("", any(), any()) }
    }

    // ---------- v4 (flag encendido) ----------

    @Test
    fun `v4 approve signs canonical with nonce and payload hash using device key`() = runTest {
        coEvery { repository.requestNonce(deviceUuid) } returns Resource.Success(nonce)
        val canonical = slot<String>()
        every { signer.signWithDeviceKey(capture(canonical)) } returns "sig-v4"
        val location = LocationData(latitude = -33.4, longitude = -70.6, accuracy = 5.0)

        val result = useCase(v4 = true).approve(
            challengeId, totpCode = "654321", location = location, metadata = mapOf("pin_used" to "true")
        )

        assertTrue(result is Resource.Success)
        coVerify { repository.respondChallenge(token, any(), SignatureV4.VERSION) }

        val request = sentRequest.captured
        assertEquals("sig-v4", request.signature)
        assertEquals("device", request.signing_key)

        val payload = json.decodeFromString(RespondChallengePayloadV4.serializer(), decode(request.data))
        assertEquals(nonce, payload.nonce)
        assertEquals("654321", payload.totp_code)
        assertEquals(location, payload.location)
        assertEquals(mapOf("pin_used" to "true"), payload.metadata)

        val expected = SignatureV4.canonical(deviceUuid, challengeId, "approved", payload.timestamp, nonce, request.data)
        assertEquals(expected, canonical.captured)
    }

    @Test
    fun `v4 approve with biometric signature uses user auth key`() = runTest {
        coEvery { repository.requestNonce(deviceUuid) } returns Resource.Success(nonce)
        val biometricSignature = mockk<Signature>()
        every { signer.signWith(biometricSignature, any()) } returns "sig-user-auth"

        useCase(v4 = true).approve(challengeId, totpCode = null, userAuthSignature = biometricSignature)

        assertEquals("sig-user-auth", sentRequest.captured.signature)
        assertEquals("user_auth", sentRequest.captured.signing_key)
        verify(exactly = 0) { signer.signWithDeviceKey(any()) }
    }

    @Test
    fun `v4 reject always signs with device key`() = runTest {
        coEvery { repository.requestNonce(deviceUuid) } returns Resource.Success(nonce)
        every { signer.signWithDeviceKey(any()) } returns "sig"

        useCase(v4 = true).reject(challengeId, totpCode = null)

        assertEquals("device", sentRequest.captured.signing_key)
        val payload = json.decodeFromString(RespondChallengePayloadV4.serializer(), decode(sentRequest.captured.data))
        assertEquals("rejected", payload.response)
    }

    @Test
    fun `v4 nonce failure returns the error without responding`() = runTest {
        coEvery { repository.requestNonce(deviceUuid) } returns Resource.Error("nonce down")

        val result = useCase(v4 = true).approve(challengeId, totpCode = null)

        assertEquals("nonce down", (result as Resource.Error).error)
        coVerify(exactly = 0) { repository.respondChallenge(any(), any(), any()) }
    }

    @Test
    fun `v4 encoded data has no line breaks`() = runTest {
        coEvery { repository.requestNonce(deviceUuid) } returns Resource.Success(nonce)
        every { signer.signWithDeviceKey(any()) } returns "sig"
        val longMetadata = (1..50).associate { "key$it" to "value$it" }

        useCase(v4 = true).approve(challengeId, totpCode = null, metadata = longMetadata)

        assertTrue(sentRequest.captured.data.none { it == '\n' || it == '\r' })
    }
}
