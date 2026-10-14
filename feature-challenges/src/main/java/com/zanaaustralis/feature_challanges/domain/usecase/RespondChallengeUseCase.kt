package com.zanaaustralis.feature_challanges.domain.usecase

import com.zanaaustralis.core.common.Resource
import com.zanaaustralis.core.secure.SecurityFlags
import com.zanaaustralis.core.secure.crypto.DeviceCryptoSigner
import com.zanaaustralis.core.secure.crypto.SignatureV4
import com.zanaaustralis.core.secure.device.DeviceIdentityManager
import com.zanaaustralis.core.session.SessionProvider
import com.zanaaustralis.feature_challanges.data.model.EncryptedRespondRequest
import com.zanaaustralis.feature_challanges.data.model.LocationData
import com.zanaaustralis.feature_challanges.data.model.RespondChallengePayload
import com.zanaaustralis.feature_challanges.data.model.RespondChallengePayloadV4
import com.zanaaustralis.feature_challanges.domain.repository.ChallengeRepository
import kotlinx.serialization.json.Json
import java.security.Signature
import java.util.Base64
import javax.inject.Inject

/**
 * @author esteban.rivera01
 */
class RespondChallengeUseCase internal constructor(
    private val repository: ChallengeRepository,
    private val signer: DeviceCryptoSigner,
    private val deviceIdentityManager: DeviceIdentityManager,
    private val sessionProvider: SessionProvider,
    private val json: Json,
    // Parámetro solo para tests; Hilt usa el constructor @Inject con SecurityFlags
    private val isSignatureV4Enabled: Boolean
) {

    @Inject
    constructor(
        repository: ChallengeRepository,
        signer: DeviceCryptoSigner,
        deviceIdentityManager: DeviceIdentityManager,
        sessionProvider: SessionProvider,
        json: Json
    ) : this(repository, signer, deviceIdentityManager, sessionProvider, json, SecurityFlags.signatureV4Enabled)

    private companion object {
        const val APPROVED = "approved"
        const val REJECTED = "rejected"
        const val SIGNING_KEY_DEVICE = "device"
        const val SIGNING_KEY_USER_AUTH = "user_auth"
    }

    /**
     * @param userAuthSignature firma de la clave ligada a biometría, ya desbloqueada con BiometricPrompt.
     * Solo se usa con la firma v4; sin ella se firma con la clave de dispositivo.
     */
    suspend fun approve(
        challengeId: String,
        totpCode: String?,
        location: LocationData? = null,
        metadata: Map<String, String>? = null,
        userAuthSignature: Signature? = null
    ): Resource<Boolean> =
        respond(ChallengeAnswer(challengeId, APPROVED, totpCode, location, metadata), userAuthSignature)

    suspend fun reject(
        challengeId: String,
        totpCode: String?,
        location: LocationData? = null,
        metadata: Map<String, String>? = null
    ): Resource<Boolean> =
        respond(ChallengeAnswer(challengeId, REJECTED, totpCode, location, metadata), userAuthSignature = null)

    private suspend fun respond(answer: ChallengeAnswer, userAuthSignature: Signature?): Resource<Boolean> =
        if (isSignatureV4Enabled) {
            respondV4(answer, userAuthSignature)
        } else {
            respondLegacy(answer)
        }

    /** Firma v3 que verifica el backend desplegado: `device:challenge:response:timestamp`. */
    private suspend fun respondLegacy(answer: ChallengeAnswer): Resource<Boolean> {
        val session = sessionProvider.getAccessToken() ?: ""
        val deviceUuid = deviceIdentityManager.getOrCreateDeviceUUID()
        val timestamp = System.currentTimeMillis()
        val signature = signer.signRespondChallenge(
            challengeId = answer.challengeId,
            deviceUuid = deviceUuid,
            challengeResponse = answer.response,
            timestamp = timestamp
        )
        val payload = RespondChallengePayload(
            challenge_id = answer.challengeId,
            response = answer.response,
            signature = signature.signatureBase64,
            totp_code = answer.totpCode,
            location = answer.location,
            metadata = answer.metadata,
            timestamp = timestamp
        )
        val envelope = EncryptedRespondRequest(
            encrypted = true,
            data = encode(json.encodeToString(RespondChallengePayload.serializer(), payload)),
            device_uuid = deviceUuid
        )
        return repository.respondChallenge(session, envelope)
    }

    /** Firma v4: nonce de un solo uso + hash del payload exacto (docs/BACKEND_SECURITY_CHANGES.md, B1). */
    private suspend fun respondV4(answer: ChallengeAnswer, userAuthSignature: Signature?): Resource<Boolean> {
        val session = sessionProvider.getAccessToken() ?: ""
        val deviceUuid = deviceIdentityManager.getOrCreateDeviceUUID()

        val nonce = when (val result = repository.requestNonce(deviceUuid)) {
            is Resource.Success -> result.data
            is Resource.Error -> return result
            Resource.Loading -> return Resource.Error("Nonce request failed")
        }

        val timestamp = System.currentTimeMillis()
        val payload = RespondChallengePayloadV4(
            challenge_id = answer.challengeId,
            response = answer.response,
            timestamp = timestamp,
            nonce = nonce,
            totp_code = answer.totpCode,
            location = answer.location,
            metadata = answer.metadata
        )
        val data = encode(json.encodeToString(RespondChallengePayloadV4.serializer(), payload))
        val canonical = SignatureV4.canonical(
            deviceUuid = deviceUuid,
            challengeId = answer.challengeId,
            response = answer.response,
            timestamp = timestamp,
            nonce = nonce,
            data = data
        )

        val (signature, signingKey) = if (userAuthSignature != null) {
            signer.signWith(userAuthSignature, canonical) to SIGNING_KEY_USER_AUTH
        } else {
            signer.signWithDeviceKey(canonical) to SIGNING_KEY_DEVICE
        }

        val envelope = EncryptedRespondRequest(
            encrypted = true,
            data = data,
            device_uuid = deviceUuid,
            signature = signature,
            signing_key = signingKey
        )
        return repository.respondChallenge(session, envelope, SignatureV4.VERSION)
    }

    private fun encode(jsonPayload: String): String =
        Base64.getEncoder().encodeToString(jsonPayload.toByteArray(Charsets.UTF_8))

    private data class ChallengeAnswer(
        val challengeId: String,
        val response: String,
        val totpCode: String?,
        val location: LocationData?,
        val metadata: Map<String, String>?
    )
}
