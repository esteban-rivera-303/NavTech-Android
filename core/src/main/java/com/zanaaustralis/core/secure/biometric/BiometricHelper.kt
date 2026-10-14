package com.zanaaustralis.core.secure.biometric

/**
 * @author esteban.rivera01
 */

import android.content.Context
import android.hardware.biometrics.BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE
import androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import java.security.Signature

class BiometricHelper(private val context: Context) {

    fun canAuthenticate(): Boolean {
        val manager = BiometricManager.from(context)
        return manager.canAuthenticate(BIOMETRIC_STRONG) ==
                BIOMETRIC_SUCCESS
    }

    fun authenticate(
        activity: FragmentActivity,
        title: String,
        subtitle: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(context)
        val builder = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setNegativeButtonText("Cancelar")
            .setAllowedAuthenticators(BIOMETRIC_STRONG)

        if (subtitle.isNotBlank()) {
            builder.setSubtitle(subtitle)
        }

        val promptInfo = builder.build()

        val prompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onError(errString.toString())
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onError("Autenticación fallida. Inténtalo nuevamente.")
            }
        })
        prompt.authenticate(promptInfo)
    }

    /**
     * Desbloquea [signature] (clave ligada a biometría) con un CryptoObject.
     * [onError] solo se invoca ante errores terminales (cancelación, bloqueo, etc.);
     * los intentos fallidos los muestra el propio prompt.
     */
    fun authenticateWithSignature(
        activity: FragmentActivity,
        title: String,
        subtitle: String,
        signature: Signature,
        onSuccess: (Signature) -> Unit,
        onError: (String) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(context)
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText("Cancelar")
            .setAllowedAuthenticators(BIOMETRIC_STRONG)
            .build()

        val prompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                val unlocked = result.cryptoObject?.signature
                if (unlocked != null) onSuccess(unlocked) else onError("No se pudo desbloquear la firma.")
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onError(errString.toString())
            }
        })
        prompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(signature))
    }

    fun getAuthStatus(): BiometricStatus {
        val manager = BiometricManager.from(context)

        return when (manager.canAuthenticate(BIOMETRIC_STRONG)) {
            BIOMETRIC_SUCCESS -> BiometricStatus.Available
            BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricStatus.NotEnrolled
            BIOMETRIC_ERROR_NO_HARDWARE -> BiometricStatus.NoHardware
            else -> BiometricStatus.Unavailable
        }
    }
}
sealed class BiometricStatus {
    object Available : BiometricStatus()
    object NotEnrolled : BiometricStatus()
    object NoHardware : BiometricStatus()
    object Unavailable : BiometricStatus()
}
