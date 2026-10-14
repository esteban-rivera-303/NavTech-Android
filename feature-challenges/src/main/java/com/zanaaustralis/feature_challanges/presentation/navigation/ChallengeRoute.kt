package com.zanaaustralis.feature_challanges.presentation.navigation

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavHostController
import com.zanaaustralis.core.NavResultKeys
import com.zanaaustralis.core.NavRoute
import com.zanaaustralis.core.common.ui.theme.NavTechTheme
import com.zanaaustralis.core.secure.biometric.BiometricHelper
import com.zanaaustralis.feature_auth.AuthFactor
import com.zanaaustralis.feature_challanges.domain.AuthorizationStep
import com.zanaaustralis.feature_challanges.presentation.components.promptChallengeApproval
import com.zanaaustralis.feature_challanges.presentation.challenge_detail.ChallengeDetailScreen
import com.zanaaustralis.feature_challanges.presentation.challenge_detail.ChallengeDetailState
import com.zanaaustralis.feature_challanges.presentation.challenge_list.ChallengeViewModel


/**
 * @author esteban.rivera01
 */

@Composable
fun ChallengeDetailRoute(
    navController: NavHostController,
    viewModel: ChallengeViewModel,
    challengeId: String
) {
    val context = LocalContext.current
    val activity = context as FragmentActivity
    val biometricHelper = remember { BiometricHelper(context) }

    val challenges by viewModel.challenges.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val challenge = challenges.firstOrNull { it.id == challengeId }
    val state by viewModel.detailState.collectAsState()

    val savedStateHandle =
        navController.currentBackStackEntry?.savedStateHandle

    LaunchedEffect(Unit) {
        viewModel.authorizationStep.collect { step ->
            when (step) {
                is AuthorizationStep.RequireBiometric -> {
                    if (biometricHelper.canAuthenticate()) {
                        biometricHelper.authenticate(
                            activity = activity,
                            title = "Autorización biométrica",
                            subtitle = "Confirma para continuar",
                            onSuccess = {
                                viewModel.markFactorAsAuthorized(
                                    step.challengeId,
                                    AuthFactor.BIOMETRIC
                                )
                            },
                            onError = { /* mostrar error */ }
                        )
                    }
                }

                is AuthorizationStep.RequireBiometricSignature -> {
                    biometricHelper.promptChallengeApproval(
                        activity = activity,
                        step = step,
                        onSigned = viewModel::approveWithBiometricSignature
                    )
                }

                is AuthorizationStep.RequirePin -> {
                    navController.navigate(NavRoute.PIN_SECTION.build(step.challengeId))
                }

                is AuthorizationStep.RequireTotp -> {
                    navController.navigate(NavRoute.Totp.build(step.challengeId))
                }

                is AuthorizationStep.Completed -> {
                    navController.popBackStack() // vuelve al Home o lista
                }

                else -> Unit
            }
        }
    }


    when {
        isLoading -> {
            // loading o fetch puntual
            LoadingChallengeState()
        }
        else -> {// ✅ Escuchar PIN validado
            val pinValidatedFlow =
                savedStateHandle?.getStateFlow<String?>(
                    NavResultKeys.PIN_VALIDATED_CHALLENGE_ID,
                    null
                )

            LaunchedEffect(pinValidatedFlow) {
                pinValidatedFlow?.collect { id ->
                    if (challenge != null && id == challenge.id) {
                        viewModel.markFactorAsAuthorized(id, AuthFactor.PIN)
                        savedStateHandle.remove<String>(NavResultKeys.PIN_VALIDATED_CHALLENGE_ID)
                    }
                }
            }
            // ✅ Escuchar TOTP validado
            val totpValidatedFlow =
                savedStateHandle?.getStateFlow<String?>(
                    NavResultKeys.TOTP_VALIDATED_CHALLENGE_ID,
                    null
                )

            LaunchedEffect(totpValidatedFlow) {
                totpValidatedFlow?.collect { id ->
                    if (challenge != null && id == challenge.id) {
                        viewModel.markFactorAsAuthorized(id, AuthFactor.TOTP)
                        savedStateHandle.remove<String>(NavResultKeys.TOTP_VALIDATED_CHALLENGE_ID)
                    }
                }
            }

            // ✅ Escuchar AuthorizationStep (navega a factores)
            challenge?.let {
                ChallengeDetailScreen(
                    challenge = challenge,
                    state = state,
                    onReject = {
                        if (biometricHelper.canAuthenticate()) {
                            biometricHelper.authenticate(
                                activity = activity,
                                title = "Autorización biométrica",
                                subtitle = "",
                                onSuccess = {
                                    viewModel.rejectChallenge(challenge.id)
                                },
                                onError = { /* mostrar error */ }
                            )
                        }

                    },
                    onResolveFactor = { challenge, factor ->
                        viewModel.requestNextStep(
                            challengeId = challenge.id,
                            forcedFactor = factor
                        )
                    },
                    onBack = { navController.popBackStack() },
                    navController = navController
                )
            }
        }
    }
}

@Composable
fun LoadingChallengeState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF6F7FB))
            //.windowInsetsPadding(WindowInsets.systemBars)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // 🔐 Icono de seguridad / proceso
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(
                    color = Color(0xFFEDEBFF),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Lock,
                contentDescription = null,
                tint = NavTechTheme.colors.primary,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ⏳ Indicador
        CircularProgressIndicator(
            color = NavTechTheme.colors.primary,
            strokeWidth = 3.dp,
            modifier = Modifier.size(36.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 📝 Texto principal
        Text(
            text = "Verificando desafío…",
            style = MaterialTheme.typography.titleMedium,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 🧠 Texto secundario
        Text(
            text = "Estamos validando la información de seguridad",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 🧩 Skeleton card (opcional pero muy bonito)
        LoadingChallengeSkeleton()
    }
}

@Composable
fun LoadingChallengeSkeleton() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            Box(
                modifier = Modifier
                    .height(18.dp)
                    .fillMaxWidth(0.6f)
                    .background(Color(0xFFEDEDED), RoundedCornerShape(8.dp))
            )

            Box(
                modifier = Modifier
                    .height(14.dp)
                    .fillMaxWidth()
                    .background(Color(0xFFEDEDED), RoundedCornerShape(8.dp))
            )

            Box(
                modifier = Modifier
                    .height(14.dp)
                    .fillMaxWidth(0.4f)
                    .background(Color(0xFFEDEDED), RoundedCornerShape(8.dp))
            )

            Box(
                modifier = Modifier
                    .height(36.dp)
                    .fillMaxWidth(0.35f)
                    .background(Color(0xFFEDEBFF), RoundedCornerShape(12.dp))
            )
        }
    }
}


