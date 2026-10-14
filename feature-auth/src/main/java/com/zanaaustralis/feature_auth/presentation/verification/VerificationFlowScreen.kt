package com.zanaaustralis.feature_auth.presentation.verification

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
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
import com.zanaaustralis.core.common.ui.theme.NavTechTheme
import com.zanaaustralis.core.domain.model.User
import com.zanaaustralis.feature_kyc.presentation.KyCScreen

/**
 * @author esteban.rivera01
 */
@Composable
fun VerificationFlowScreen(
    user: User,
    rut: String,
    onComplete: () -> Unit,
    viewModel: VerificationFlowViewModel = hiltViewModel()
) {

    val state by viewModel.state.collectAsState()
    val colors = NavTechTheme.colors

    LaunchedEffect(rut) {
        viewModel.init(rut)
    }

    LaunchedEffect(state) {
        if (state is VerificationState.Completed) {
            onComplete()
        }
    }

    AnimatedContent(
        targetState = state,
        label = "verification_flow"
    ) { currentState ->

        when (currentState) {

            VerificationState.Loading -> {
                Box(
                    modifier = Modifier
                        .background(colors.background)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            VerificationState.Email -> {
                EmailVerificationScreen(
                    email = user.email,
                    step = 1,
                    totalSteps = 3,
                    onSuccess = {
                        viewModel.onEmailVerified()
                    },
                )
            }

            VerificationState.Phone -> {
                PhoneVerificationScreen(
                    phone = user.phone,
                    step = 2,
                    totalSteps = 3,
                    onSuccess = {
                        viewModel.onPhoneVerified()
                    }
                )
            }

            VerificationState.Kyc -> {
                KyCScreen(
                    rut = rut,
                    onSuccess = {
                        viewModel.onKycCompleted()
                    }
                )
            }

            is VerificationState.Error -> {
                Text("Error: ${currentState.message}")
            }

            VerificationState.Completed -> Unit
        }
    }
}