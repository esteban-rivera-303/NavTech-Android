package com.zanaaustralis.feature_auth.presentation.recovery

import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.zanaaustralis.feature_auth.presentation.common.NavStepScreen
import com.zanaaustralis.feature_auth.presentation.recovery.verification.email.RecoveryEmailVerificationScreen
import com.zanaaustralis.feature_auth.presentation.recovery.verification.phone.RecoveryPhoneVerificationScreen
import com.zanaaustralis.feature_kyc.presentation.KyCScreen

/**
 * @author esteban.rivera01
 */

@Composable
fun RecoveryVerificationFlowScreen(
    rut: String,
    email: String,
    phone: String,
    viewModel: RecoveryVerificationFlowViewModel = hiltViewModel(),
    onRecoveryComplete: () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.init()
    }

    LaunchedEffect(state) {
        if (state is RecoveryVerificationState.Completed) {
            viewModel.clearRecovery()
            onRecoveryComplete()
        }
    }

    AnimatedContent(
        targetState = state,
        label = "recovery_verification_flow"
    ) { currentState ->

        when (currentState) {
            is RecoveryVerificationState.Loading -> {
                NavStepScreen(
                    title = "Validando...",
                    step = 0,
                    totalSteps = 0,
                    bottomContent = {}
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }


            is RecoveryVerificationState.Email -> {
                RecoveryEmailVerificationScreen(
                    email = email,
                    step = 1,
                    totalSteps = 3,
                    onSuccess = {
                        viewModel.refreshStatus()
                    }
                )
            }

            is RecoveryVerificationState.Phone -> {
                RecoveryPhoneVerificationScreen(
                    phone = phone,
                    step = 2,
                    totalSteps = 3,
                    onSuccess = {
                        viewModel.refreshStatus()
                    }
                )
            }

            is RecoveryVerificationState.Kyc -> {
                KyCScreen(
                    rut = rut,
                    onSuccess = {
                        viewModel.onKycCompleted()
                    }
                )
            }


            is RecoveryVerificationState.Error -> {
                NavStepScreen(
                    title = "Error",
                    step = 0,
                    totalSteps = 0,
                    bottomContent = {}
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(currentState.message)
                    }
                }
            }

            is RecoveryVerificationState.LastStep -> {
                Log.e("Main Recovery", "LastStep")
                viewModel.completeRecovery()
            }
            is RecoveryVerificationState.Completed -> {
                onRecoveryComplete()
            }
        }
    }
}