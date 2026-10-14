package com.zanaaustralis.feature_auth.presentation.pin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.zanaaustralis.feature_auth.presentation.pin.intro.CreatePinIntroScreen
import com.zanaaustralis.feature_auth.presentation.pin.intro.PinViewModel
import com.zanaaustralis.feature_auth.presentation.pin.recovery.PinOptionsScreen

/**
 * @author esteban.rivera01
 */

@Composable
fun PinEntryPointScreen(
    viewModel: PinViewModel = hiltViewModel(),
    onCreatePin: () -> Unit,
    onChangePin: () -> Unit,
    onForgotPin: () -> Unit,
    onBack: () -> Unit
) {
    val hasPin by viewModel.hasPin.collectAsState()

    when (hasPin) {
        null -> {
            //LoadingChallengeState()
        }

        false -> {
            CreatePinIntroScreen(
                onRecoverPin = onChangePin,
                onCreatePin = onCreatePin,
                onBack = onBack
            )
        }

        true -> {
            PinOptionsScreen(
                onChangePin = onChangePin,
                onForgotPin = onForgotPin,
                onBack = onBack
            )
        }
    }
}
