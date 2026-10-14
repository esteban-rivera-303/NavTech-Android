package com.zanaaustralis.core.secure.crypto

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import android.util.Log
import com.zanaaustralis.core.secure.SecurityFlags
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.Signature
import javax.inject.Inject
import javax.inject.Singleton

/**
 * @author esteban.rivera01
 */

/**
 * Clave RSA del Android Keystore que exige autenticación biométrica fuerte en **cada** uso.
 * Una firma con esta clave solo es posible tras un BiometricPrompt con CryptoObject,
 * por lo que el servidor puede verificar que hubo biometría (docs/BACKEND_SECURITY_CHANGES.md, B2).
 */
@Singleton
class UserAuthKeyStore @Inject constructor() {

    companion object {
        private const val TAG = "UserAuthKeyStore"
        private const val KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "argos_user_auth_key"
        private const val KEY_SIZE = 2048
        private const val SIGNATURE_ALGORITHM = "SHA256withRSA"
        private const val AUTH_TIMEOUT_EVERY_USE = 0
    }

    /**
     * Regenera la clave durante el enrolamiento si la firma v4 está activa.
     * Devuelve el PEM público, o `null` si el flag está apagado o el dispositivo no tiene biometría enrolada.
     */
    fun provisionForEnrollment(): String? {
        if (!SecurityFlags.signatureV4Enabled) return null

        return runCatching {
            deleteKey()
            generateKeyPair()
            publicKeyPem()
        }.onFailure {
            Log.w(TAG, "User auth key unavailable: ${it.javaClass.simpleName}")
        }.getOrNull()
    }

    /** Prepara una firma lista para desbloquearse con BiometricPrompt + CryptoObject. */
    fun prepareSignature(): UserAuthSignature {
        val privateKey = loadKeyStore().getKey(KEY_ALIAS, null) as? PrivateKey
            ?: return UserAuthSignature.MissingKey

        return try {
            UserAuthSignature.Ready(
                Signature.getInstance(SIGNATURE_ALGORITHM).apply { initSign(privateKey) }
            )
        } catch (e: KeyPermanentlyInvalidatedException) {
            Log.w(TAG, "User auth key invalidated by biometric change")
            deleteKey()
            UserAuthSignature.Invalidated
        }
    }

    private fun generateKeyPair() {
        val builder = KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_SIGN)
            .setDigests(KeyProperties.DIGEST_SHA256)
            .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
            .setKeySize(KEY_SIZE)
            .setUserAuthenticationRequired(true)
            .setInvalidatedByBiometricEnrollment(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            builder.setUserAuthenticationParameters(
                AUTH_TIMEOUT_EVERY_USE,
                KeyProperties.AUTH_BIOMETRIC_STRONG
            )
        } else {
            @Suppress("DEPRECATION")
            builder.setUserAuthenticationValidityDurationSeconds(-1)
        }

        KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, KEYSTORE).apply {
            initialize(builder.build())
            generateKeyPair()
        }
    }

    private fun publicKeyPem(): String =
        loadKeyStore().getCertificate(KEY_ALIAS)?.publicKey?.toPem()
            ?: throw IllegalStateException("User auth key not found")

    private fun deleteKey() {
        val keyStore = loadKeyStore()
        if (keyStore.containsAlias(KEY_ALIAS)) keyStore.deleteEntry(KEY_ALIAS)
    }

    private fun loadKeyStore(): KeyStore =
        KeyStore.getInstance(KEYSTORE).apply { load(null) }
}

sealed interface UserAuthSignature {
    data class Ready(val signature: Signature) : UserAuthSignature
    data object MissingKey : UserAuthSignature
    data object Invalidated : UserAuthSignature
}
