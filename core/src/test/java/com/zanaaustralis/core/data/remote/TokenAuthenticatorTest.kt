package com.zanaaustralis.core.data.remote

import android.util.Log
import com.zanaaustralis.core.data.remote.model.AuthSessionResponse
import com.zanaaustralis.core.data.remote.model.RefreshResponse
import com.zanaaustralis.core.data.remote.model.RefreshTokenRequest
import com.zanaaustralis.core.session.SessionInvalidator
import com.zanaaustralis.core.session.SessionManagerImpl
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.time.Instant
import okhttp3.Response as OkResponse
import retrofit2.Response as RetrofitResponse

class TokenAuthenticatorTest {

    private val sessionManager = mockk<SessionManagerImpl>(relaxed = true)
    private val invalidator = mockk<SessionInvalidator>(relaxed = true)
    private val api = mockk<AuthApiService>()
    private val authenticator = TokenAuthenticator(sessionManager, invalidator) { api }

    private val request = Request.Builder()
        .url("https://example.com/challenges")
        .header("Authorization", "Bearer old")
        .build()

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.w(any(), any<String>()) } returns 0
        coEvery { sessionManager.getSession() } returns AuthSessionResponse("old", "refresh-1", 0L)
    }

    @After
    fun tearDown() {
        unmockkStatic(Log::class)
    }

    private fun response(code: Int, prior: OkResponse? = null): OkResponse =
        OkResponse.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message("")
            .priorResponse(prior)
            .build()

    private fun refreshOk() = RefreshResponse(
        session_token = "new-token",
        refresh_token = "refresh-2",
        expires_at = "2026-01-01T10:00:00Z",
        sessionExpiresAt = null,
        sessionTimeoutMinutes = null
    )

    @Test
    fun `401 refreshes the session and retries with the new token`() {
        val sentBody = slot<RefreshTokenRequest>()
        val saved = slot<AuthSessionResponse>()
        coEvery { api.refresh(capture(sentBody), any()) } returns RetrofitResponse.success(refreshOk())
        coEvery { sessionManager.updateSession(capture(saved)) } returns Unit

        val retried = authenticator.authenticate(null, response(401))

        assertEquals("Bearer new-token", retried?.header("Authorization"))
        assertEquals("refresh-1", sentBody.captured.refresh_token)
        assertEquals("refresh-2", saved.captured.refreshToken)
        assertEquals(Instant.parse("2026-01-01T10:00:00Z").toEpochMilli(), saved.captured.expiresAt)
    }

    @Test
    fun `non 401 responses are not retried`() {
        assertNull(authenticator.authenticate(null, response(403)))
        coVerify(exactly = 0) { api.refresh(any(), any()) }
    }

    @Test
    fun `gives up after a retried request fails again`() {
        val second = response(401, prior = response(401))

        assertNull(authenticator.authenticate(null, second))
        coVerify(exactly = 0) { api.refresh(any(), any()) }
    }

    @Test
    fun `no stored refresh token means no retry`() {
        coEvery { sessionManager.getSession() } returns null

        assertNull(authenticator.authenticate(null, response(401)))
        coVerify(exactly = 0) { api.refresh(any(), any()) }
    }

    @Test
    fun `rejected refresh clears and invalidates the session`() {
        coEvery { api.refresh(any(), any()) } returns RetrofitResponse.error(401, "".toResponseBody())

        assertNull(authenticator.authenticate(null, response(401)))

        coVerify { sessionManager.clearSession() }
        coVerify { invalidator.invalidateSession() }
    }

    @Test
    fun `server error on refresh keeps the session`() {
        coEvery { api.refresh(any(), any()) } returns RetrofitResponse.error(500, "".toResponseBody())

        assertNull(authenticator.authenticate(null, response(401)))

        coVerify(exactly = 0) { sessionManager.clearSession() }
        coVerify(exactly = 0) { invalidator.invalidateSession() }
    }

    @Test
    fun `network failure on refresh returns null without clearing the session`() {
        coEvery { api.refresh(any(), any()) } throws IOException("offline")

        assertNull(authenticator.authenticate(null, response(401)))

        coVerify(exactly = 0) { sessionManager.clearSession() }
    }
}
