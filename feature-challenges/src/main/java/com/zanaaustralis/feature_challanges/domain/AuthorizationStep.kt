package com.zanaaustralis.feature_challanges.domain

import java.security.Signature

/**
 * @author esteban.rivera01
 */

sealed class AuthorizationStep {
    data class RequireBiometric(val challengeId: String) : AuthorizationStep()

    /**
     * Firma v4: la biometría desbloquea la clave ligada a biometría y la aprobación
     * se envía en cuanto el prompt termina con éxito.
     */
    class RequireBiometricSignature(
        val challengeId: String,
        val signature: Signature
    ) : AuthorizationStep()
    data class RequirePin(val challengeId: String) : AuthorizationStep()
    data class RequireTotp(val challengeId: String) : AuthorizationStep()
    data class RequireLocation(val challengeId: String) : AuthorizationStep()
    data class ReadyToAuthorize(val challengeId: String) : AuthorizationStep()
    data class Completed(val challengeId: String) : AuthorizationStep()
    data class Failed(val message: String) : AuthorizationStep()
}

