package com.zanaaustralis.feature_challanges.domain.model

import com.zanaaustralis.core.secure.SecurityFlags
import com.zanaaustralis.feature_auth.AuthFactor
import com.zanaaustralis.feature_challanges.data.mapper.toDomain
import com.zanaaustralis.feature_challanges.data.model.ApplicationDto
import com.zanaaustralis.feature_challanges.data.model.ChallengeDto
import com.zanaaustralis.feature_challanges.data.model.ChallengeTypeDto
import com.zanaaustralis.feature_challanges.data.model.ContextData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeFalse
import org.junit.Test
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

class ChallengeModelTest {

    private fun challenge(
        biometric: Boolean = false,
        location: Boolean = false,
        pin: Boolean = false,
        totp: Boolean = false,
        authorized: Set<AuthFactor> = emptySet(),
        expiresAt: String = "2026-01-01T10:00:00Z"
    ) = ChallengeItem(
        id = "c1", title = "t", description = null, date = "", appName = "app", amount = null,
        requiresBiometric = biometric, requiresTotp = totp, requiresLocation = location, requiresPin = pin,
        createdAt = "2026-01-01T09:00:00Z", expiresAt = expiresAt, risk_level = "low",
        authorizedFactors = authorized
    )

    private fun dto(status: String = "pending", risk: String = "high", createdAt: String = "2026-01-01T09:00:00Z") =
        ChallengeDto(
            id = "c1", user_id = "u1", application_id = "a1", challenge_type_id = "t1",
            description = "Transferencia", challenge_amount = 1500.0, status = status,
            created_at = createdAt, expires_at = "2026-01-01T10:00:00Z",
            challenge_types = ChallengeTypeDto(
                name = "Pago", description = "d", risk_level = risk,
                requires_pin = true, requires_geolocation = false, requires_biometric = true, requires_totp = true
            ),
            applications = ApplicationDto("Banco"),
            risk_level = risk,
            context_data = ContextData("Pago a terceros")
        )

    // ---------- Factores ----------

    @Test
    fun `required factors follow the challenge flags in fixed order and ignore totp`() {
        val all = challenge(biometric = true, location = true, pin = true, totp = true)

        assertEquals(listOf(AuthFactor.BIOMETRIC, AuthFactor.LOCATION, AuthFactor.PIN), all.requiredFactors())
        assertEquals(emptyList<AuthFactor>(), challenge().requiredFactors())
    }

    @Test
    fun `marking a factor keeps the challenge pending until all required factors are authorized`() {
        val start = challenge(pin = true, location = true)

        val afterPin = start.markFactorAsAuthorized(AuthFactor.PIN)
        assertEquals(setOf(AuthFactor.PIN), afterPin.authorizedFactors)
        assertEquals("pendiente", afterPin.status)

        val afterLocation = afterPin.markFactorAsAuthorized(AuthFactor.LOCATION)
        assertEquals("enviado", afterLocation.status)
        assertEquals(emptySet<AuthFactor>(), start.authorizedFactors)
    }

    @Test
    fun `missing factors exclude authorized ones and readiness follows`() {
        assumeFalse(SecurityFlags.signatureV4Enabled)
        val ch = challenge(biometric = true, pin = true, authorized = setOf(AuthFactor.PIN))

        assertEquals(listOf(AuthFactor.BIOMETRIC), ch.missingFactors())
        assertFalse(ch.isReadyToAuthorize())
        assertTrue(ch.markFactorAsAuthorized(AuthFactor.BIOMETRIC).isReadyToAuthorize())
    }

    @Test
    fun `without signature v4 biometric is a regular factor`() {
        assumeFalse(SecurityFlags.signatureV4Enabled)

        assertFalse(challenge(biometric = true).needsBiometricSignature())
    }

    // ---------- Expiración ----------

    @Test
    fun `expires in seconds is positive before expiry and zero after or when invalid`() {
        val now = Instant.parse("2026-01-01T09:59:00Z")

        assertEquals(60L, challenge().expiresInSeconds(now))
        assertEquals(0L, challenge().expiresInSeconds(Instant.parse("2026-01-01T11:00:00Z")))
        assertEquals(0L, challenge(expiresAt = "no-date").expiresInSeconds(now))
    }

    @Test
    fun `expires at millis parses iso dates and falls back to zero`() {
        assertEquals(Instant.parse("2026-01-01T10:00:00Z").toEpochMilli(), challenge().expiresAtMillis)
        assertEquals(0L, challenge(expiresAt = "bad").expiresAtMillis)
    }

    // ---------- Mapeo ----------

    @Test
    fun `dto maps to challenge item with translated status and required factors`() {
        val item = dto().toChallengeItem()

        assertEquals("Banco", item.title)
        assertEquals("Pago a terceros", item.description)
        assertEquals("pendiente", item.status)
        assertTrue(item.isCritical)
        assertTrue(item.requiresPin && item.requiresBiometric && item.requiresTotp)
        assertFalse(item.requiresLocation)
        assertEquals(1500.0, item.amount)
    }

    @Test
    fun `dto status approved maps to enviado and unknown statuses pass through`() {
        assertEquals("enviado", dto(status = "APPROVED").toChallengeItem().status)
        assertEquals("expired", dto(status = "expired").toChallengeItem().status)
        assertFalse(dto(risk = "low").toChallengeItem().isCritical)
    }

    @Test
    fun `legacy mapper uses challenge type name and keeps raw status`() {
        val item = dto(status = "pending").toDomain()

        assertEquals("Pago", item.title)
        assertEquals("Transferencia", item.description)
        assertEquals("pending", item.status)
        assertTrue(item.isCritical)
    }

    // ---------- Fechas relativas ----------

    @Test
    fun `format time ago uses minutes hours and days`() {
        val now = OffsetDateTime.now(ZoneOffset.UTC)

        assertEquals("Hace un momento", formatTimeAgo(now.toString()))
        assertEquals("Hace 5 minutos", formatTimeAgo(now.minusMinutes(5).toString()))
        assertEquals("Hace 3 horas", formatTimeAgo(now.minusHours(3).toString()))
        assertEquals("Hace 2 días", formatTimeAgo(now.minusDays(2).toString()))
        assertEquals("Fecha desconocida", formatTimeAgo("ayer"))
    }

    @Test
    fun `format created at uses short relative labels`() {
        val now = Instant.now()

        assertEquals("Ahora", now.toString().formatCreatedAt())
        assertEquals("Hace 10 min", now.minusSeconds(10 * 60).toString().formatCreatedAt())
        assertEquals("Hace 2 h", now.minusSeconds(2 * 3600).toString().formatCreatedAt())
        assertEquals("Ayer", now.minusSeconds(26 * 3600).toString().formatCreatedAt())
        assertEquals("Hace 3 días", now.minusSeconds(3 * 86400).toString().formatCreatedAt())
        assertEquals("", "bad".formatCreatedAt())
    }
}
