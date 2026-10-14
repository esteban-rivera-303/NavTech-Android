package com.zanaaustralis.feature_challanges.presentation.challenge_detail

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.zanaaustralis.core.NavRoute
import com.zanaaustralis.core.common.formatTimeRemaining
import com.zanaaustralis.core.common.ui.CriticalBadge
import com.zanaaustralis.core.common.ui.component.NavTechButton
import com.zanaaustralis.core.common.ui.component.NavTechButtonSecondary
import com.zanaaustralis.core.common.ui.theme.NavTechTheme
import com.zanaaustralis.core.location.LocationPermissionHelper
import com.zanaaustralis.core.location.LocationProvider
import com.zanaaustralis.feature_auth.AuthFactor
import com.zanaaustralis.feature_auth.presentation.totp.TotpInputBottomSheet
import com.zanaaustralis.feature_challanges.domain.AuthorizationStep
import com.zanaaustralis.feature_challanges.domain.model.ChallengeItem
import com.zanaaustralis.feature_challanges.domain.model.isReadyToAuthorize
import com.zanaaustralis.feature_challanges.presentation.challenge_list.ChallengeViewModel

/**
 * @author esteban.rivera01
 */

@Composable
fun ChallengeDetailScreen(
    challenge: ChallengeItem,
    state: ChallengeDetailState,
    navController: NavController,
    viewModel: ChallengeViewModel = hiltViewModel(),
    onReject: () -> Unit,
    onResolveFactor: (ChallengeItem, AuthFactor) -> Unit,
    onBack: () -> Unit
) {

    val permissionState by viewModel.permissionState.collectAsState()
    val showLocationSheet = permissionState == ChallengePermissionState.RequireLocationPermission
    var showSheet by remember { mutableStateOf(false) }

    val errorMessage by viewModel.errorMessage.collectAsState()
    val context = LocalContext.current
    val locationPermissionHelper = remember {
        LocationPermissionHelper(context)
    }
    val locationProvider = remember {
        LocationProvider(context)
    }

    val requestLocationPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                viewModel.onLocationPermissionResult(true)
            }
        }

    LaunchedEffect(challenge.id) {
        viewModel.onEnterChallengeDetail(
            challengeId = challenge.id,
            hasLocationPermission =
                locationPermissionHelper.hasLocationPermission()
        )
    }

    LaunchedEffect(permissionState) {

        when (permissionState) {

            ChallengePermissionState.RequestLocation,
            ChallengePermissionState.Ready -> {

                if (challenge.requiresLocation &&
                    !challenge.authorizedFactors.contains(AuthFactor.LOCATION)
                ) {
                    viewModel.fetchLocation(
                        challengeId = challenge.id,
                        locationProvider = locationProvider
                    )
                }
            }

            else -> Unit
        }
    }
    LaunchedEffect(Unit) {
        viewModel.authorizationStep.collect { step ->
            if (step is AuthorizationStep.Completed) {
                navController.navigate(NavRoute.DialogSuccess.route) {
                    popUpTo(NavRoute.Home.route) { inclusive = true }
                }
            }
            if (step is AuthorizationStep.Failed) {
                navController.navigate("result_error") {
                    popUpTo(NavRoute.Home.route) { inclusive = true }
                }
            }
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavTechTheme.colors.background)
            .padding(16.dp)

    ) {

        ChallengeDetailHeader(
            onBack = onBack
        )

        Spacer(Modifier.height(32.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp
            ),
            colors = CardDefaults.cardColors(containerColor = NavTechTheme.colors.surface)
        ) {

            ChallengeDetailCardV2(challenge)

            Spacer(Modifier.height(16.dp))

            SecurityFactorsSection(
                challenge = challenge,
                onShowSheetChange = { showSheet = it },
                onResolveFactor = onResolveFactor,
            )

            Spacer(Modifier.height(16.dp))
        }

        // ChallengeStatusSection(state)

        errorMessage?.let {
            Text(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(24.dp),
                text = it,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.weight(1f))

        ChallengeActions(
            //challenge = challenge,
            enabled = challenge.isReadyToAuthorize(),
            loading = state is ChallengeDetailState.Sending,
            onApprove = { viewModel.authorizeChallenge(challenge.id) },
            onReject = onReject
        )
    }

    if (showLocationSheet) {
        LocationPermissionBottomSheet(
            onAllow = {
                requestLocationPermissionLauncher.launch(
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            },
            onReject = {
                viewModel.onLocationPermissionResult(false)
            }
        )
    }

    TotpInputBottomSheet(
        visible = showSheet,
        onDismiss = { showSheet = false },
        onConfirm = { code ->
            viewModel.onTotpEntered(challenge.id, code)
            showSheet = false
        }
    )
}


@Composable
fun ChallengeDetailCardV2(challenge: ChallengeItem) {

    Column(Modifier.padding(16.dp)) {

        // 🔹 App name + badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Text(
                text = challenge.appName,
                color = NavTechTheme.colors.primary,
                style = MaterialTheme.typography.bodySmall
            )

            if (challenge.isCritical) {
                CriticalBadge()
            }
        }

        Spacer(Modifier.height(6.dp))

        Text(
            text = challenge.title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(12.dp))

        Divider()

        Spacer(Modifier.height(12.dp))

        DetailRow("Estado", challenge.status)
        DetailRow("Riesgo", challenge.risk_level)
        DetailRow("Expira en", formatTimeRemaining(challenge.expiresAtMillis))

        Spacer(Modifier.height(12.dp))

        Divider()
    }
}

@Composable
fun ChallengeDetailHeader(
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .background(Color.White)
            .padding(top = 24.dp, bottom = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onBack() }
        ) {
            Icon(Icons.Outlined.ArrowBack, null, tint = NavTechTheme.colors.primary)
        /*    Spacer(Modifier.width(6.dp))
            Text(
                "Atrás", color = NavTechTheme.colors.primary,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center
            )*/
            Spacer(Modifier.weight(1f))

            Text(
                text = "Desafío",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.weight(1f))
            //Spacer(Modifier.width(72.dp))
            Spacer(Modifier.width(36.dp))

        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.labelMedium)
        Text(value, color = Color.Gray, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
fun SecurityFactorsSection(
    challenge: ChallengeItem,
    onShowSheetChange: (Boolean) -> Unit,
    onResolveFactor: (ChallengeItem, AuthFactor) -> Unit
) {

    Column(Modifier.padding(16.dp)) {

        Text(
            text = "Factores requeridos",
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.titleSmall
        )

        Spacer(Modifier.height(12.dp))

        // 🔹 LISTA (igual que ahora pero mejor visual)
        if (challenge.requiresLocation) {
            SecurityFactorRow(
                "Geolocalización",
                challenge.authorizedFactors.contains(AuthFactor.LOCATION)
            )
        }

        if (challenge.requiresPin) {
            SecurityFactorRow(
                "PIN",
                challenge.authorizedFactors.contains(AuthFactor.PIN)
            )
        }

        if (challenge.requiresBiometric) {
            SecurityFactorRow(
                "Biometría",
                challenge.authorizedFactors.contains(AuthFactor.BIOMETRIC)
            )
        }

        if (challenge.requiresTotp) {
            SecurityFactorRow(
                "Código TOTP",
                challenge.authorizedFactors.contains(AuthFactor.TOTP)
            )
        }

        Spacer(Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            if (challenge.requiresPin && !challenge.authorizedFactors.contains(AuthFactor.PIN)) {
                NavTechButtonSecondary(
                    text = "PIN",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onResolveFactor(challenge, AuthFactor.PIN)
                    }
                )
            }

            if (challenge.requiresBiometric && !challenge.authorizedFactors.contains(AuthFactor.BIOMETRIC)) {
                NavTechButtonSecondary(
                    text = "Biometría",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onResolveFactor(challenge, AuthFactor.BIOMETRIC)
                    }
                )
            }

            if (challenge.requiresTotp && !challenge.authorizedFactors.contains(AuthFactor.TOTP)) {
                NavTechButtonSecondary(
                    text = "TOTP",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onShowSheetChange(true)
                        //onResolveFactor(challenge, AuthFactor.TOTP)
                    }
                )
            }
        }
    }
}

@Composable
fun SecurityFactorRow(label: String, completed: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = if (completed) Icons.Outlined.CheckCircle else Icons.Outlined.CheckCircle,
            tint = if (completed) Color(0xFF4CAF50) else Color.Gray,
            contentDescription = null
        )

        Spacer(Modifier.width(8.dp))

        Text(label)
    }
}


sealed class ChallengeDetailState {
    object Idle : ChallengeDetailState()
    object Sending : ChallengeDetailState()
    data class Error(val message: String) : ChallengeDetailState()
}


// Botonera
@Composable
fun ChallengeActions(
    enabled: Boolean,
    loading: Boolean,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {

    Column(modifier = Modifier.padding(bottom = 36.dp)) {

        NavTechButton(
            text = "Autorizar",
            onClick = onApprove,
            enabled = enabled,
            isLoading = loading,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Rechazar",
            color = Color.Red,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .clickable { onReject() }
        )
        Spacer(Modifier.height(12.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPermissionBottomSheet(
    onAllow: () -> Unit,
    onReject: () -> Unit
) {

    ModalBottomSheet(
        onDismissRequest = onReject,
        containerColor = Color.White,
        tonalElevation = 2.dp
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // 🔵 INDICADOR (opcional pero pro)
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .background(Color.LightGray, RoundedCornerShape(50))
            )

            Spacer(Modifier.height(20.dp))

            // 🔵 ICONO
            Icon(
                imageVector = Icons.Outlined.LocationOn,
                contentDescription = null,
                tint = NavTechTheme.colors.primary,
                modifier = Modifier.size(36.dp)
            )

            Spacer(Modifier.height(16.dp))

            // 🔵 TÍTULO
            Text(
                text = "Permiso de ubicación",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            // 🔵 DESCRIPCIÓN
            Text(
                text = "Navtech necesita acceder a tu ubicación para validar este desafío.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = Color.Gray
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "*Requerido para aprobar la operación",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = NavTechTheme.colors.primary
            )

            Spacer(Modifier.height(24.dp))

            // 🔵 BOTONES
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // ❌ RECHAZAR
                TextButton(
                    onClick = onReject,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Rechazar")
                }

                // ✅ PERMITIR
                NavTechButton(
                    text = "Permitir",
                    onClick = onAllow,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}

// PREVIEW

fun previewChallenge(
    authorized: Set<AuthFactor> = emptySet()
): ChallengeItem {
    return ChallengeItem(
        id = "1",
        title = "Transferencia bancaria",
        description = "Transferencia a Juan Pérez",
        date = "2026-03-20T10:00:00Z",
        isCritical = true,
        status = "Pendiente",
        appName = "Banco Estado",
        amount = 250000.0,
        requiresBiometric = true,
        requiresTotp = true,
        requiresLocation = true,
        requiresPin = true,
        createdAt = "2026-03-20T09:00:00Z",
        expiresAt = "2026-03-20T11:00:00Z",
        risk_level = "Alto",
        authorizedFactors = authorized
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewChallengeDetail_Idle() {
    NavTechTheme {
        ChallengeDetailContentPreview(
            challenge = previewChallenge(),
            state = ChallengeDetailState.Idle
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewChallengeDetail_Sending() {
    NavTechTheme {
        ChallengeDetailContentPreview(
            challenge = previewChallenge(
                authorized = setOf(
                    AuthFactor.PIN,
                    AuthFactor.BIOMETRIC,
                    AuthFactor.LOCATION,
                    AuthFactor.TOTP
                )
            ),
            state = ChallengeDetailState.Sending
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewChallengeDetail_Error() {
    NavTechTheme {
        ChallengeDetailContentPreview(
            challenge = previewChallenge(),
            state = ChallengeDetailState.Error("Error al autorizar")
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewChallengeDetail_AllFactors() {
    NavTechTheme {
        ChallengeDetailContentPreview(
            challenge = previewChallenge(
                authorized = setOf(
                    AuthFactor.PIN,
                    AuthFactor.BIOMETRIC,
                    AuthFactor.LOCATION,
                    AuthFactor.TOTP
                )
            ),
            state = ChallengeDetailState.Idle
        )
    }
}

@Composable
private fun ChallengeDetailContentPreview(
    challenge: ChallengeItem,
    state: ChallengeDetailState
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F7FB))
            .padding(16.dp)
    ) {


        ChallengeDetailHeader(
            onBack = {}
        )

        Spacer(Modifier.height(16.dp))
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            ChallengeDetailCardV2(challenge)

            Spacer(Modifier.height(16.dp))

            SecurityFactorsSection(
                challenge = challenge,
                onShowSheetChange ={},
                onResolveFactor = { _, _ -> }
            )

            Spacer(Modifier.height(16.dp))
        }

        //ChallengeStatusSection(state)

        Spacer(Modifier.weight(1f))

        ChallengeActions(
            enabled = challenge.isReadyToAuthorize() && state !is ChallengeDetailState.Sending,
            loading = state is ChallengeDetailState.Sending,
            onApprove = {},
            onReject = {},
        )
    }
}
