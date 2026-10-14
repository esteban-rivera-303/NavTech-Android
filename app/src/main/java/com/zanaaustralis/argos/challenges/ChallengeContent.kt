package com.zanaaustralis.argos.challenges

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.zanaaustralis.core.common.parseIsoDate
import com.zanaaustralis.core.common.ui.theme.NavTechTheme
import com.zanaaustralis.core.secure.biometric.BiometricHelper
import com.zanaaustralis.feature_auth.AuthFactor
import com.zanaaustralis.feature_challanges.domain.AuthorizationStep
import com.zanaaustralis.feature_challanges.presentation.components.promptChallengeApproval
import com.zanaaustralis.feature_challanges.domain.model.ChallengeItem
import com.zanaaustralis.feature_challanges.presentation.challenge_list.ChallengeViewModel

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * @author esteban.rivera01
 */

@Composable
fun ChallengeScreen(
    viewModel: ChallengeViewModel,
    onValidateWithPIN: (String) -> Unit,
    onValidateWithTOTP: (String) -> Unit,
    onValidateLocation: (String) -> Unit,

) {
    val challenges by viewModel.challenges.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val context = LocalContext.current
    val activity = context as FragmentActivity
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.authorizationStep.collect { step ->
            when (step) {

                is AuthorizationStep.RequireBiometric -> {
                    val challenge = challenges.firstOrNull { it.id == step.challengeId } ?: return@collect

                    val biometricHelper = BiometricHelper(context)
                    if (biometricHelper.canAuthenticate()) {
                        biometricHelper.authenticate(
                            activity = activity,
                            title = "Autorización requerida",
                            subtitle = challenge.title,
                            onSuccess = {
                                viewModel.markFactorAsAuthorized(challenge.id, AuthFactor.BIOMETRIC)
                            },
                            onError = { /* toast */ }
                        )
                    }
                }

                is AuthorizationStep.RequireBiometricSignature -> {
                    BiometricHelper(context).promptChallengeApproval(
                        activity = activity,
                        step = step,
                        onSigned = viewModel::approveWithBiometricSignature
                    )
                }

                is AuthorizationStep.RequirePin -> onValidateWithPIN(step.challengeId)
                is AuthorizationStep.RequireTotp -> onValidateWithTOTP(step.challengeId)
                is AuthorizationStep.RequireLocation -> onValidateLocation(step.challengeId)

                is AuthorizationStep.ReadyToAuthorize -> {
                }

                is AuthorizationStep.Completed -> {
                    val id = step.challengeId
                    coroutineScope.launch {
                        delay(1200)
                        viewModel.removeChallenge(id)
                    }
                }
                else -> {}
            }
        }
    }

    ChallengeContent(
        challenges = challenges,
        isLoading = isLoading,
        errorMessage = errorMessage,
        onChallengeClick = { challenge ->
            viewModel.requestNextStep(challenge)
        },
        onBack = {

        }
    )
}
@Composable
fun ChallengesHeader(
    pendingCount: Int,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 48.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Volver"
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Desafíos",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "$pendingCount pendientes",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
    }
}
@Composable
fun ChallengeVerticalCard(
    challenge: ChallengeItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = challenge.appName,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.weight(1f))

                if (challenge.isCritical) {
                    Text(
                        text = "CRÍTICO",
                        color = Color.Red,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = challenge.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = challenge.description?:"",
                style = MaterialTheme.typography.bodySmall,
                color = Color.DarkGray,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Expira: ${parseIsoDate(challenge.expiresAt)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = challenge.risk_level.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun ChallengeContent(
    challenges: List<ChallengeItem>,
    isLoading: Boolean,
    errorMessage: String?,
    onChallengeClick: (ChallengeItem) -> Unit,
    onBack: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {

        // 🔹 Header fijo
        ChallengesHeader(
            pendingCount = challenges.size,
            onBack = onBack
        )

        Divider(color = Color(0xFFEAEAEA))

        // 🔹 Content
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {

            when {
                isLoading && challenges.isEmpty() -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = NavTechTheme.colors.primary
                    )
                }

                errorMessage != null -> {
                    Text(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }

                challenges.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No tienes desafíos pendientes",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Todo está bajo control 👍",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        items(challenges) { challenge ->
                            ChallengeVerticalCard(
                                challenge = challenge,
                                onClick = { onChallengeClick(challenge) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/*
@Preview(showBackground = true)
@Composable
fun PreviewChallengesScreen() {
    ChallengeContent(
        challenges = listOf(
            ChallengeItem(
                id = "1",
                title = "Transferencia bancaria",
                description = "Autorización de transferencia a cuenta externa",
                date = "Hace 14 minutos",
                isCritical = true,
                status = "pendiente",
                appName = "Banco Global",
                amount = 1250000.0,
                requiresBiometric = true,
                requiresTotp = true,
                requiresLocation = true,
                createdAt = "2025-11-27T10:00:00Z",
                expiresAt = "2025-11-27T10:05:00Z",
                risk_level = "LOW"
            ),
            ChallengeItem(
                id = "2",
                title = "Inicio de sesión en backoffice",
                description = "Intento de acceso al panel administrativo desde un nuevo dispositivo",
                date = "Hace 1 hora",
                isCritical = false,
                status = "pendiente",
                appName = "Sistema Corporativo",
                amount = null,
                requiresBiometric = false,
                requiresTotp = true,
                requiresLocation = false,
                createdAt = "2025-11-27T09:00:00Z",
                expiresAt = "2025-11-27T09:05:00Z",
                risk_level = "LOW"
            ),
            ChallengeItem(
                id = "3",
                title = "Autorización de pago recurrente",
                description = "Suscripción mensual a servicio Premium",
                date = "Hace 3 horas",
                isCritical = false,
                status = "pendiente",
                appName = "Streaming Plus",
                amount = 9990.0,
                requiresBiometric = false,
                requiresTotp = false,
                requiresLocation = false,
                createdAt = "2025-11-27T08:00:00Z",
                expiresAt = "2025-11-27T08:05:00Z",
                risk_level = "LOW"
            )
        ),
        onChallengeClick = {},
        isLoading = false,
        errorMessage = "",
        onBack = {  }
    )
}*/
