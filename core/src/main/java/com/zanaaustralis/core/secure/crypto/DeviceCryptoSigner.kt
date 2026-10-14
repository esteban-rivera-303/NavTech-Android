package com.zanaaustralis.core.secure.crypto


import android.util.Base64
import android.util.Log
import java.security.Signature
import javax.inject.Inject

/**
 * @author esteban.rivera01
 */
class DeviceCryptoSigner @Inject constructor(
    private val keyStoreManager: AndroidKeyStoreManager
) {
/*    fun signLogin(deviceUuid: String): LoginSignature {
        val privateKey = keyStoreManager.getPrivateKey()
            ?: error("Device not enrolled")


        val timestamp = System.currentTimeMillis()
        val payload = "$deviceUuid:$timestamp"

        val signature = Signature.getInstance("SHA256withRSA").apply {
            initSign(privateKey)
            update(payload.toByteArray(Charsets.UTF_8))
        }.sign()

        return LoginSignature(
            timestamp,
            Base64.encodeToString(signature, Base64.NO_WRAP)
        )
    }

    fun signRespondChallenge(
        deviceUuid: String,
        challengeId: String,
        timestamp: Long,
    ): RespondSignature {
        val privateKey = keyStoreManager.getPrivateKey()

        val payload = "$deviceUuid:$challengeId:$timestamp"
        val signatureBytes = Signature.getInstance("SHA256withRSA").apply {
            initSign(privateKey)
            update(payload.toByteArray(Charsets.UTF_8))
        }.sign()

        return RespondSignature(
            signatureBase64 = Base64.encodeToString(signatureBytes, Base64.NO_WRAP)
        )
    }*/

    fun signLogin(deviceUuid: String): LoginSignature {
        val timestamp = System.currentTimeMillis()
        val payload = "$deviceUuid:$timestamp"

        return LoginSignature(
            timestamp = timestamp,
            signatureBase64 = signPayload(payload)
        )
    }

    fun signRespondChallenge(
        deviceUuid: String,
        challengeId: String,
        challengeResponse: String,
        timestamp: Long,
    ): RespondSignature {
        val payload = "$deviceUuid:$challengeId:$challengeResponse:$timestamp"

        return RespondSignature(
            signatureBase64 = signPayload(payload)
        )
    }

    /** Firma un canonical (p. ej. [SignatureV4]) con la clave de dispositivo. */
    fun signWithDeviceKey(canonical: String): String = signPayload(canonical)

    /**
     * Firma con un [Signature] ya inicializado y desbloqueado por BiometricPrompt
     * (clave de [UserAuthKeyStore]). Cada desbloqueo permite una sola firma.
     */
    fun signWith(signature: Signature, canonical: String): String {
        signature.update(canonical.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(signature.sign(), Base64.NO_WRAP)
    }

    private fun signPayload(payload: String): String {
        val privateKey = keyStoreManager.getPrivateKey()

        val signatureBytes = Signature.getInstance("SHA256withRSA").apply {
            initSign(privateKey)
            update(payload.toByteArray(Charsets.UTF_8))
        }.sign()

        return Base64.encodeToString(signatureBytes, Base64.NO_WRAP)
    }
}

data class LoginSignature(
    val timestamp: Long,
    val signatureBase64: String
)
data class RespondSignature(
    val signatureBase64: String
)

