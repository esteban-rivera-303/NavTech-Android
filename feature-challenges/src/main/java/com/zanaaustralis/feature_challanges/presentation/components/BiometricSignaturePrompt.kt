package com.zanaaustralis.feature_challanges.presentation.components

import androidx.fragment.app.FragmentActivity
import com.zanaaustralis.core.secure.biometric.BiometricHelper
import com.zanaaustralis.feature_challanges.domain.AuthorizationStep
import java.security.Signature

/**
 * @author esteban.rivera01
 */

/**
 * Muestra el prompt biométrico que desbloquea la firma de aprobación (firma v4).
 * Al terminar con éxito, [onSigned] recibe la firma lista para usar una sola vez.
 */
fun BiometricHelper.promptChallengeApproval(
    activity: FragmentActivity,
    step: AuthorizationStep.RequireBiometricSignature,
    onSigned: (challengeId: String, signature: Signature) -> Unit,
    onError: (String) -> Unit = {}
) {
    authenticateWithSignature(
        activity = activity,
        title = "Aprobar desafío",
        subtitle = "Confirma con tu huella para aprobar",
        signature = step.signature,
        onSuccess = { unlocked -> onSigned(step.challengeId, unlocked) },
        onError = onError
    )
}
