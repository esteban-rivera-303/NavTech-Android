package com.zanaaustralis.core.data.remote.repository

import com.google.gson.JsonParser
import com.zanaaustralis.core.common.Resource
import com.zanaaustralis.core.data.local.TotpStorage
import com.zanaaustralis.core.data.remote.AuthApiService
import com.zanaaustralis.core.data.remote.model.AuthSessionResponse
import com.zanaaustralis.core.data.remote.model.DeviceInfo
import com.zanaaustralis.core.data.remote.model.OnboardingRequest
import com.zanaaustralis.core.data.remote.model.RegisterRequest
import com.zanaaustralis.core.data.remote.model.UserDataResponse
import com.zanaaustralis.core.secure.crypto.DeviceCryptoSigner
import com.zanaaustralis.core.secure.crypto.LoginSignature
import com.zanaaustralis.core.secure.device.DeviceIdentityManager
import com.zanaaustralis.core.session.SessionManagerImpl
import com.zanaaustralis.core.session.TokenStorage
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.Instant

class AuthRepositoryImplTest {

    private val server = MockWebServer()
    private val sessionManager = mockk<SessionManagerImpl>(relaxed = true)
    private val deviceIdentity = mockk<DeviceIdentityManager>(relaxed = true)
    private val tokenStorage = mockk<TokenStorage>(relaxed = true)
    private val signer = mockk<DeviceCryptoSigner>()
    private val totpStorage = mockk<TotpStorage>(relaxed = true)
    private lateinit var repository: AuthRepositoryImpl

    private val deviceInfo = DeviceInfo("hw", "Google", "Pixel", "Android 14", "14")

    @Before
    fun setUp() {
        server.start()
        val api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApiService::class.java)
        repository = AuthRepositoryImpl(api, sessionManager, deviceIdentity, tokenStorage, signer, totpStorage)
        every { deviceIdentity.getOrCreateDeviceUUID() } returns "d1"
        every { deviceIdentity.getDeviceInfo() } returns deviceInfo
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun enqueue(code: Int, body: String = "{}") {
        server.enqueue(MockResponse().setResponseCode(code).setBody(body))
    }

    private fun onboardingRequest() = OnboardingRequest(
        rut = "11111111-1", device_uuid = "d1", os = "Android 14", model = "Pixel", fingerprint = "fp",
        platform = "android", public_key = "PEM", device_info = deviceInfo, fcm_token = "fcm",
        user_auth_public_key = "USER-PEM"
    )

    // ---------- Onboarding ----------

    @Test
    fun `onboarding sends device headers and stores the totp secret`() = runTest {
        enqueue(200, """{ "success": true, "totp_secret": "SECRET" }""")

        val result = repository.onboarding(onboardingRequest())

        assertTrue(result is Resource.Success)
        verify { totpStorage.saveSecret("SECRET") }
        val recorded = server.takeRequest()
        assertEquals("android", recorded.getHeader("X-Platform"))
        assertEquals("d1", recorded.getHeader("X-Device-UUID"))
        val body = JsonParser.parseString(recorded.body.readUtf8()).asJsonObject
        assertEquals("USER-PEM", body["user_auth_public_key"].asString)
    }

    @Test
    fun `onboarding error exposes the server error message and code`() = runTest {
        enqueue(409, """{ "error": "User already has a device enrolled" }""")

        val result = repository.onboarding(onboardingRequest()) as Resource.Error

        assertEquals("User already has a device enrolled", result.error)
        assertEquals(409, result.code)
        verify(exactly = 0) { totpStorage.saveSecret(any()) }
    }

    @Test
    fun `error bodies without an error field use a generic message`() = runTest {
        enqueue(500, """{ "message": "boom" }""")

        val result = repository.onboarding(onboardingRequest()) as Resource.Error

        assertEquals("Unexpected error", result.error)
    }

    // ---------- Login ----------

    @Test
    fun `login is skipped when the device is not enrolled`() = runTest {
        every { deviceIdentity.isDeviceEnrolled() } returns false

        val result = repository.login() as Resource.Error

        assertEquals("No login", result.error)
        assertEquals(0, server.requestCount)
    }

    @Test
    fun `login signs the request and stores session user and totp secret`() = runTest {
        every { deviceIdentity.isDeviceEnrolled() } returns true
        every { signer.signLogin("d1") } returns LoginSignature(timestamp = 42L, signatureBase64 = "sig")
        enqueue(
            200,
            """
            {
              "success": true, "session_token": "access", "refresh_token": "refresh",
              "expires_at": "2026-01-01T10:00:00Z", "user_id": "u1", "totp_secret": "SECRET",
              "user": { "full_name": "Ana", "email": "ana@example.com", "rut": "1-9" }
            }
            """.trimIndent()
        )
        val session = slot<AuthSessionResponse>()
        val user = slot<UserDataResponse>()
        coEvery { sessionManager.updateSession(capture(session)) } returns Unit
        coEvery { sessionManager.updateUserData(capture(user)) } returns Unit

        val result = repository.login()

        assertTrue(result is Resource.Success)
        val body = JsonParser.parseString(server.takeRequest().body.readUtf8()).asJsonObject
        assertEquals("d1", body["device_uuid"].asString)
        assertEquals(42L, body["timestamp"].asLong)
        assertEquals("sig", body["signature"].asString)
        assertEquals("access", session.captured.accessToken)
        assertEquals(Instant.parse("2026-01-01T10:00:00Z").toEpochMilli(), session.captured.expiresAt)
        assertEquals("Ana", user.captured.full_name)
        verify { totpStorage.saveSecret("SECRET") }
    }

    @Test
    fun `login with success false does not store a session`() = runTest {
        every { deviceIdentity.isDeviceEnrolled() } returns true
        every { signer.signLogin("d1") } returns LoginSignature(1L, "sig")
        enqueue(
            200,
            """{ "success": false, "session_token": "", "refresh_token": "", "expires_at": "", "user_id": "" }"""
        )

        val result = repository.login() as Resource.Error

        assertEquals("No login", result.error)
        coVerify(exactly = 0) { sessionManager.updateSession(any()) }
    }

    @Test
    fun `login http error keeps the status code`() = runTest {
        every { deviceIdentity.isDeviceEnrolled() } returns true
        every { signer.signLogin("d1") } returns LoginSignature(1L, "sig")
        enqueue(401, """{ "error": "Invalid signature" }""")

        val result = repository.login() as Resource.Error

        assertEquals("Invalid signature", result.error)
        assertEquals(401, result.code)
    }

    // ---------- Logout ----------

    @Test
    fun `logout clears enrollment flag session and tokens`() = runTest {
        enqueue(200, """{ "success": true, "message": "ok" }""")

        val result = repository.logout()

        assertTrue(result.isSuccess)
        verify { deviceIdentity.clear() }
        coVerify { sessionManager.clearSession() }
        verify { tokenStorage.clear() }
    }

    @Test
    fun `logout network failure reports failure and keeps local state`() = runTest {
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START))

        val result = repository.logout()

        assertTrue(result.isFailure)
        verify(exactly = 0) { deviceIdentity.clear() }
        verify(exactly = 0) { tokenStorage.clear() }
    }

    // ---------- Otros ----------

    @Test
    fun `user status sends rut device uuid and device info`() = runTest {
        enqueue(200, """{ "success": true }""")

        repository.userStatus("11111111-1")

        val body = JsonParser.parseString(server.takeRequest().body.readUtf8()).asJsonObject
        assertEquals("11111111-1", body["rut"].asString)
        assertEquals("d1", body["device_uuid"].asString)
        assertEquals("Pixel", body["device_info"].asJsonObject["model"].asString)
    }

    @Test
    fun `network failures become errors`() = runTest {
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START))

        val result = repository.registerUser(RegisterRequest("1-9", "a@b.c", "+56900000000"))

        assertTrue((result as Resource.Error).error.startsWith("Network error"))
    }
}
