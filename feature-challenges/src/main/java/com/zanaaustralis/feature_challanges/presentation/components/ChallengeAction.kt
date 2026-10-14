package com.zanaaustralis.feature_challanges.presentation.components

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.zanaaustralis.core.common.ui.PrimaryButton
import com.zanaaustralis.core.common.ui.SecondaryButton
import com.zanaaustralis.feature_auth.AuthFactor
import com.zanaaustralis.feature_challanges.presentation.challenge_list.ChallengeAction

/**
 * @author esteban.rivera01
 */

@Composable
fun ChallengeActionButton(
    action: ChallengeAction,
    isDetail: Boolean,
    modifier: Modifier,
    canAuthorize: Boolean,
    onAuthorize: () -> Unit,
    onResolveFactor: (AuthFactor) -> Unit
) {
    when (action) {
        is ChallengeAction.Authorize -> {
            PrimaryButton(
                modifier = modifier,
                enabled = canAuthorize,
                text = action.label(isDetail),
                onClick = onAuthorize
            )
        }

        is ChallengeAction.ResolveFactor -> {
            SecondaryButton(
                modifier = modifier,
                text = action.label(isDetail),
                onClick = {
                    onResolveFactor(action.factor) }
            )
        }

        is ChallengeAction.Sending -> {
            PrimaryButton(
                modifier = modifier,
                text = action.label(isDetail),
                enabled = false,
                loading = true,
                onClick = { Log.e("ChallengeAction", "Sending")}
            )
        }

        ChallengeAction.None -> Unit
    }
}

fun ChallengeAction.label(isDetail: Boolean): String {
    return when (this) {
        is ChallengeAction.Authorize ->
            if (isDetail) "¡Autorizar!" else "AUTORIZAR"

        is ChallengeAction.ResolveFactor -> {
            if (isDetail) {
                when (factor) {
                    AuthFactor.BIOMETRIC -> "Biometría"
                    AuthFactor.PIN -> "PIN"
                    AuthFactor.TOTP -> "Autorizar con Código"
                    AuthFactor.LOCATION -> "Autorizar con Ubicación"
                }
            } else {
                when (factor) {
                    AuthFactor.BIOMETRIC -> "Biometría"
                    AuthFactor.PIN -> "PIN"
                    AuthFactor.TOTP -> "TOTP"
                    AuthFactor.LOCATION -> "Ubicación"
                }
            }
        }

        is ChallengeAction.Sending -> "Enviando…"
        ChallengeAction.None -> ""
    }
}
