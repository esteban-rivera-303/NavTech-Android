package com.zanaaustralis.feature_challanges.data.repository

import com.google.gson.JsonParser
import com.zanaaustralis.core.common.Resource
import com.zanaaustralis.feature_challanges.data.local.ChallengeLocalDataSource
import com.zanaaustralis.feature_challanges.data.model.EncryptedRespondRequest
import com.zanaaustralis.feature_challanges.data.remote.ChallengeApiService
import io.mockk.mockk
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

class ChallengeRepositoryImplTest {

    private val server = MockWebServer()
    private lateinit var repository: ChallengeRepositoryImpl

    private val challengeJson = """
        {
          "id": "c1", "user_id": "u1", "application_id": "a1", "challenge_type_id": "t1",
          "description": "Transferencia", "challenge_amount": 1500.0, "status": "pending",
          "created_at": "2026-01-01T09:00:00Z", "expires_at": "2026-01-01T10:00:00Z",
          "challenge_types": {
            "name": "Pago", "description": "d", "risk_level": "high", "requires_pin": true,
            "requires_geolocation": true, "requires_biometric": false, "requires_totp": false
          },
          "applications": { "name": "Banco" },
          "risk_level": "high",
          "context_data": { "description": "Pago a terceros" }
        }
    """.trimIndent()

    @Before
    fun setUp() {
        server.start()
        val api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ChallengeApiService::class.java)
        repository = ChallengeRepositoryImpl(api, mockk<ChallengeLocalDataSource>())
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun enqueue(code: Int, body: String = "{}") {
        server.enqueue(MockResponse().setResponseCode(code).setBody(body))
    }

    // ---------- Pendientes ----------

    @Test
    fun `pending challenges are mapped to domain items`() = runTest {
        enqueue(200, """{ "success": true, "challenges": [$challengeJson] }""")

        val result = repository.getPendingChallenges("token")

        val item = (result as Resource.Success).data.single()
        assertEquals("c1", item.id)
        assertEquals("Banco", item.title)
        assertEquals("pendiente", item.status)
        assertTrue(item.requiresPin && item.requiresLocation)
        assertEquals("GET", server.takeRequest().method)
    }

    @Test
    fun `pending challenges http error keeps the status code`() = runTest {
        enqueue(403)

        val result = repository.getPendingChallenges("token") as Resource.Error

        assertEquals(403, result.code)
    }

    @Test
    fun `network failures become errors instead of exceptions`() = runTest {
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START))

        val result = repository.getPendingChallenges("token")

        assertTrue((result as Resource.Error).error.startsWith("Network error"))
    }

    @Test
    fun `refresh publishes challenges and ignores failures`() = runTest {
        enqueue(200, """{ "success": true, "challenges": [$challengeJson] }""")
        repository.refreshChallenges("token")
        assertEquals(listOf("c1"), repository.observeChallenges().value.map { it.id })

        enqueue(500)
        repository.refreshChallenges("token")
        assertEquals(listOf("c1"), repository.observeChallenges().value.map { it.id })
    }

    // ---------- Respuesta ----------

    @Test
    fun `respond sends signature version header and v4 envelope fields`() = runTest {
        enqueue(200)
        val request = EncryptedRespondRequest(
            data = "ZGF0YQ==", device_uuid = "d1", signature = "sig", signing_key = "user_auth"
        )

        val result = repository.respondChallenge("token", request, signatureVersion = 4)

        assertTrue(result is Resource.Success)
        val recorded = server.takeRequest()
        assertEquals("/challenges/respond", recorded.path)
        assertEquals("4", recorded.getHeader("x-argos-signature-version"))
        val body = JsonParser.parseString(recorded.body.readUtf8()).asJsonObject
        assertEquals("ZGF0YQ==", body["data"].asString)
        assertEquals("sig", body["signature"].asString)
        assertEquals("user_auth", body["signing_key"].asString)
        assertTrue(body["encrypted"].asBoolean)
    }

    @Test
    fun `respond v3 omits v4 only fields`() = runTest {
        enqueue(200)

        repository.respondChallenge("token", EncryptedRespondRequest(data = "x", device_uuid = "d1"), 3)

        val recorded = server.takeRequest()
        assertEquals("3", recorded.getHeader("x-argos-signature-version"))
        val body = JsonParser.parseString(recorded.body.readUtf8()).asJsonObject
        assertFalse(body.has("signature"))
        assertFalse(body.has("signing_key"))
    }

    @Test
    fun `respond http error keeps the status code`() = runTest {
        enqueue(409)

        val result = repository.respondChallenge("token", EncryptedRespondRequest(data = "x", device_uuid = "d1"), 3)

        assertEquals(409, (result as Resource.Error).code)
    }

    // ---------- Nonce ----------

    @Test
    fun `nonce is returned and request carries the device uuid`() = runTest {
        enqueue(200, """{ "success": true, "nonce": "n-1", "timestamp": 1, "expires_in": 300000 }""")

        val result = repository.requestNonce("d1")

        assertEquals("n-1", (result as Resource.Success).data)
        val body = JsonParser.parseString(server.takeRequest().body.readUtf8()).asJsonObject
        assertEquals("d1", body["device_uuid"].asString)
    }

    @Test
    fun `blank or missing nonce is an error`() = runTest {
        enqueue(200, """{ "success": true, "nonce": "" }""")
        assertTrue(repository.requestNonce("d1") is Resource.Error)

        enqueue(200, """{ "success": true }""")
        assertTrue(repository.requestNonce("d1") is Resource.Error)

        enqueue(500)
        assertEquals(500, (repository.requestNonce("d1") as Resource.Error).code)
    }

    // ---------- TOTP ----------

    @Test
    fun `totp validation succeeds only when the server says valid`() = runTest {
        enqueue(200, """{ "success": true, "valid": true }""")
        assertTrue(repository.validateTotp("d1", "123456") is Resource.Success)

        val body = JsonParser.parseString(server.takeRequest().body.readUtf8()).asJsonObject
        assertEquals("d1", body["device_uuid"].asString)
        assertEquals("123456", body["totp_code"].asString)

        enqueue(200, """{ "success": true, "valid": false }""")
        assertTrue(repository.validateTotp("d1", "000000") is Resource.Error)
    }

    // ---------- Historial ----------

    @Test
    fun `history sends filters as query params and maps items`() = runTest {
        enqueue(
            200,
            """
            {
              "success": true,
              "challenges": [{
                "id": "h1", "application": { "name": "Banco" },
                "challenge_type": {
                  "name": "Pago", "description": "d", "risk_level": "High", "requires_pin": true,
                  "requires_geolocation": false, "requires_biometric": false, "requires_totp": false
                },
                "status": "approved", "created_at": "2026-01-01T09:00:00Z",
                "expires_at": "2026-01-01T10:00:00Z", "responded_at": null
              }],
              "pagination": { "total": 1, "limit": 10, "offset": 20, "has_more": false }
            }
            """.trimIndent()
        )

        val result = repository.getChallengesHistory(status = "approved", limit = 10, offset = 20)

        val item = (result as Resource.Success).data.single()
        assertEquals("h1", item.id)
        assertTrue(item.isCritical)
        val path = server.takeRequest().path.orEmpty()
        assertTrue(path.startsWith("/challenges/history"))
        assertTrue("status=approved" in path && "limit=10" in path && "offset=20" in path)
    }

    @Test
    fun `history http error keeps the status code`() = runTest {
        enqueue(401)

        assertEquals(401, (repository.getChallengesHistory(null, 20, 0) as Resource.Error).code)
    }
}
