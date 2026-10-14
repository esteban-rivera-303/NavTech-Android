package com.zanaaustralis.core.data.remote.repository

import com.google.gson.JsonParser
import com.zanaaustralis.core.common.Resource
import com.zanaaustralis.core.data.local.TotpStorage
import com.zanaaustralis.core.data.remote.AccountRecoveryApi
import com.zanaaustralis.core.data.remote.model.AuthSessionResponse
import com.zanaaustralis.core.data.remote.model.DeviceInfo
import com.zanaaustralis.core.secure.device.DeviceIdentityManager
import com.zanaaustralis.core.session.SessionManagerImpl
import com.zanaaustralis.core.store.notification.PushTokenStore
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AccountRecoveryRepositoryImplTest {

    private val server = MockWebServer()
    private val deviceIdentity = mockk<DeviceIdentityManager>()
    private val pushTokenStore = mockk<PushTokenStore>()
    private val sessionManager = mockk<SessionManagerImpl>(relaxed = true)
    private val totpStorage = mockk<TotpStorage>(relaxed = true)
    private lateinit var repository: AccountRecoveryRepositoryImpl

    private val sessionJson = """
        "access_token": "access", "refresh_token": "refresh", "expires_at": "2026-01-01T10:00:00Z",
        "totp_secret": "SECRET", "user": { "full_name": "Ana", "email": "ana@example.com", "rut": "1-9" }
    """.trimIndent()

    @Before
    fun setUp() {
        server.start()
        val api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AccountRecoveryApi::class.java)
        repository = AccountRecoveryRepositoryImpl(api, deviceIdentity, pushTokenStore, sessionManager, totpStorage)
        every { deviceIdentity.getOrCreateDeviceUUID() } returns "d1"
        every { deviceIdentity.getDeviceInfo() } returns DeviceInfo("hw", "Google", "Pixel", "Android 14", "14")
        coEvery { pushTokenStore.get() } returns "fcm"
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun enqueue(code: Int, body: String = "{}") {
        server.enqueue(MockResponse().setResponseCode(code).setBody(body))
    }

    // ---------- Inicio y verificación ----------

    @Test
    fun `initiate sends rut and device uuid in body and header`() = runTest {
        enqueue(200, """{ "success": true, "recovery_token": "rt", "message": "ok" }""")

        val result = repository.initiateRecovery("1-9", "d1")

        assertEquals("rt", (result as Resource.Success).data.recovery_token)
        val recorded = server.takeRequest()
        assertEquals("/account-recovery/initiate", recorded.path)
        assertEquals("d1", recorded.getHeader("X-Device-UUID"))
        val body = JsonParser.parseString(recorded.body.readUtf8()).asJsonObject
        assertEquals("1-9", body["rut"].asString)
    }

    @Test
    fun `recovery http errors include the status code in the message`() = runTest {
        enqueue(429)

        val result = repository.verifyEmail("rt", "123456") as Resource.Error

        assertTrue(result.error.startsWith("Error 429"))
    }

    @Test
    fun `phone verification and completion hit their endpoints`() = runTest {
        enqueue(200, """{ "success": true, "message": "ok" }""")
        enqueue(200, """{ "success": true, "message": "ok" }""")

        assertTrue(repository.verifyPhone("rt", "123456") is Resource.Success)
        assertTrue(repository.completeRecovery("rt") is Resource.Success)

        assertEquals("/account-recovery/verify-phone", server.takeRequest().path)
        assertEquals("/account-recovery/complete", server.takeRequest().path)
    }

    @Test
    fun `status maps verification flags`() = runTest {
        enqueue(
            200,
            """{ "email_verified": true, "phone_verified": false, "kyc_verified": false,
                 "expired": false, "completed": false, "nextStep": "phone" }"""
        )

        val status = (repository.getRecoveryStatus("rt") as Resource.Success).data

        assertTrue(status.emailVerified)
        assertFalse(status.phoneVerified)
        assertEquals("phone", status.nextStep)
    }

    // ---------- Reactivación ----------

    @Test
    fun `reactivation sends keys and stores session and totp secret`() = runTest {
        enqueue(
            200,
            """{ "success": true, "installation_uuid": "i1",
                 "device_status": { "is_reinstall": "true", "was_reactivated": true,
                                    "first_enrolled_at": "2025-01-01T00:00:00Z", "reinstall_count": 1 },
                 $sessionJson }"""
        )
        val session = slot<AuthSessionResponse>()
        coEvery { sessionManager.updateSession(capture(session)) } returns Unit

        val result = repository.reactivateTrustedDevice("1-9", "PEM", "USER-PEM")

        assertTrue(result is Resource.Success)
        assertEquals("access", session.captured.accessToken)
        verify { totpStorage.saveSecret("SECRET") }
        coVerify { sessionManager.updateUserData(match { it.full_name == "Ana" }) }

        val body = JsonParser.parseString(server.takeRequest().body.readUtf8()).asJsonObject
        assertEquals("d1", body["device_uuid"].asString)
        assertEquals("PEM", body["public_key"].asString)
        assertEquals("USER-PEM", body["user_auth_public_key"].asString)
        assertEquals("fcm", body["fcm_token"].asString)
        // Valor declarado por la app; el servidor no puede verificarlo (ver B2)
        assertTrue(body["biometric_verified"].asBoolean)
    }

    @Test
    fun `reactivation with success false does not store a session`() = runTest {
        enqueue(
            200,
            """{ "success": false, "installation_uuid": "i1",
                 "device_status": { "is_reinstall": "false", "was_reactivated": false,
                                    "first_enrolled_at": "", "reinstall_count": 0 },
                 $sessionJson }"""
        )

        val result = repository.reactivateTrustedDevice("1-9", "PEM", null) as Resource.Error

        assertEquals("No login", result.error)
        coVerify(exactly = 0) { sessionManager.updateSession(any()) }
        verify(exactly = 0) { totpStorage.saveSecret(any()) }
    }

    @Test
    fun `missing push token is sent as empty string`() = runTest {
        coEvery { pushTokenStore.get() } returns null
        enqueue(403)

        repository.reactivateTrustedDevice("1-9", "PEM", null)

        val body = JsonParser.parseString(server.takeRequest().body.readUtf8()).asJsonObject
        assertEquals("", body["fcm_token"].asString)
    }

    // ---------- Enrolamiento por recuperación ----------

    @Test
    fun `recovery enrollment stores session and totp secret`() = runTest {
        enqueue(200, """{ "success": true, "device_id": "dev", $sessionJson }""")

        val result = repository.enrollDevice("1-9", "PEM", "USER-PEM")

        assertTrue(result is Resource.Success)
        verify { totpStorage.saveSecret("SECRET") }
        coVerify { sessionManager.updateSession(match { it.refreshToken == "refresh" }) }
        assertEquals("/recovery/enroll-device", server.takeRequest().path)
    }

    @Test
    fun `recovery enrollment network failure becomes an error`() = runTest {
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START))

        val result = repository.enrollDevice("1-9", "PEM", null)

        assertTrue((result as Resource.Error).error.startsWith("Network error"))
        coVerify(exactly = 0) { sessionManager.updateSession(any()) }
    }
}
