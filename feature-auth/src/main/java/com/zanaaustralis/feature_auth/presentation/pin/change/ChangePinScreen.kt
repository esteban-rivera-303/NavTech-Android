package com.zanaaustralis.feature_auth.presentation.pin.change

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.zanaaustralis.feature_auth.presentation.ChangePinStep
import com.zanaaustralis.feature_auth.presentation.pin.CreatePinStep
import com.zanaaustralis.feature_auth.presentation.pin.auth.PinScreen
import com.zanaaustralis.feature_auth.presentation.pin.create.CreatePinContent

/**
 * @author esteban.rivera01
 */

@Composable
fun ChangePinScreen(
    navController: NavHostController,
    viewModel: ChangePinViewModel = hiltViewModel()
) {
    val step by viewModel.step.collectAsState()
    val error by viewModel.error.collectAsState()

    when (step) {

        ChangePinStep.VERIFY_CURRENT -> {
            PinScreen(
                onPinValidated = {
                    viewModel.onCurrentPinValidated()
                },
                onCancel = { navController.popBackStack() }
            )
        }

        ChangePinStep.ENTER_NEW -> {
            CreatePinContent(
                step = CreatePinStep.ENTER,
                error = error,
                onPinComplete = { pin ->
                    viewModel.onNewPinEntered(pin)
                },
                onCancel = { navController.popBackStack() }
            )
        }

        ChangePinStep.CONFIRM_NEW -> {
            CreatePinContent(
                step = CreatePinStep.CONFIRM,
                error = error,
                onPinComplete = { pin ->
                    viewModel.onNewPinConfirmed(pin) {
                        navController.popBackStack()
                    }
                },
                onCancel = { navController.popBackStack() }
            )
        }

        ChangePinStep.DONE -> {
            navController.popBackStack()
        }
    }
}
