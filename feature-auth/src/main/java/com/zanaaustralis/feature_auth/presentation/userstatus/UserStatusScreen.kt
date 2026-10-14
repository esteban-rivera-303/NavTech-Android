package com.zanaaustralis.feature_auth.presentation.userstatus

import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.gson.Gson
import com.zanaaustralis.core.NavRoute
import com.zanaaustralis.core.common.ui.component.NavTechButton
import com.zanaaustralis.core.common.ui.component.NavTechHeader
import com.zanaaustralis.core.common.ui.component.NavTechInput
import com.zanaaustralis.core.common.ui.theme.NavTechTheme
import com.zanaaustralis.core.data.remote.model.UserStatusDetail
import com.zanaaustralis.core.data.remote.model.UserStatusResponse
import com.zanaaustralis.feature_auth.presentation.components.BiometricRequestScreen

/**
 * @author esteban.rivera01
 */


@Composable
fun UserStatusScreen(
    navController: NavController,
    onNavigateToVerification:(userJson: String, rut: String) -> Unit,
    onNavigateToRecoveryVerification:(userJson: String) -> Unit,
    onComplete: () -> Unit,
    viewModel: UserStatusViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    val verifyResult by navController.currentBackStackEntry!!
        .savedStateHandle
        .getStateFlow<Pair<String, Boolean>?>("VERIFY_RESULT", null)
        .collectAsState()

    LaunchedEffect(verifyResult) {
        val res = verifyResult ?: return@LaunchedEffect
        val (type, ok) = res
        if (ok) {
            if (type == "email") viewModel.onEmailVerified()
            else if (type == "phone") viewModel.onPhoneVerified()

            navController.currentBackStackEntry
                ?.savedStateHandle
                ?.set("VERIFY_RESULT", null)
        }
    }
    when (state.phase) {

        UserStatusPhase.NOT_REGISTER -> {
            if (state.rut.isNotBlank()) {
                navController.navigate(NavRoute.Register.build(state.rut))
            } else {
                UserStatusContent(
                    state = state,
                    onCheckStatus = viewModel::checkUserStatus,
                )
            }
        }

        UserStatusPhase.INIT,
        UserStatusPhase.ERROR ->{
            // TODO
            UserStatusContent(
                state = state,
                onCheckStatus = viewModel::checkUserStatus,
            )
        }
        UserStatusPhase.PENDING_EMAIL,
        UserStatusPhase.PENDING_PHONE,
        UserStatusPhase.PENDING_KYC -> {
            val user = state.data?.user ?: return
            val userJson = Uri.encode(Gson().toJson(user))
            onNavigateToVerification(userJson, state.rut)
        }
        UserStatusPhase.DEVICE_MISMATCH -> {
            val user = state.data?.user ?: return
            val userJson = Uri.encode(Gson().toJson(user))
            onNavigateToRecoveryVerification(userJson)
        }

        UserStatusPhase.PENDING_DEVICE -> {
            BiometricRequestScreen {
                navController.navigate(
                    NavRoute.EnrollmentDevice.build(
                        rut = state.rut,
                        email = state.data?.user?.email ?: "",
                        phone = state.data?.user?.phone ?: ""
                    )
                )
            }
        }

        UserStatusPhase.DEVICE_TRUSTED -> {
            BiometricRequestScreen(title = "Dispositivo de confianza") {
                navController.navigate(NavRoute.EnrollmentTrustedDevice.build(state.rut)) {
                }
            }
        }

        UserStatusPhase.SESSION_ACTIVE,
        UserStatusPhase.READY_TO_LOGIN -> {
            BiometricRequestScreen(onComplete = onComplete)
        }

    }
}

@Composable
fun UserStatusContent(
    state: UserStatusState,
    onCheckStatus: (String) -> Unit,
) {

    val colors = NavTechTheme.colors
/*
    SetStatusBarColor(
        color = NavTechTheme.colors.primary,
        darkIcons = false
    )
*/

    var rut by remember { mutableStateOf("") }

    // NUEVO
    var stateUserAvailable by remember {
        mutableStateOf(UserStatusUiState())
    }

    val isRutValid = remember(rut) {
        isValidRut(rut)
    }


    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        NavTechHeader(
            title = "Inicio",
            subtitle = "Ingresa tu rut para comenzar."
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                .padding(horizontal = 20.dp, vertical = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {

            // 🔹 INPUT RUT
            Row() {
                Text(
                    text = "RUT",
                    style = NavTechTheme.typography.labelMedium,
                    color = colors.textPrimary
                )
                Text(
                    text = "12.345.678-9 o 12345678-9",
                    modifier = Modifier.padding(horizontal = 4.dp),
                    style = NavTechTheme.typography.labelSmall,
                    color = colors.textPrimary.copy(alpha = 0.5F )
                )
            }

            Spacer(Modifier.height(6.dp))

            NavTechInput(
                value = rut,
                onValueChange = { input ->
                    rut = (input)
                },
                placeholder = "Escribe tu rut"
            )

            Spacer(Modifier.height(6.dp))
            // 🔵 CHECKBOX + LINK
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Checkbox(
                    checked = stateUserAvailable.termsAccepted,
                    colors = CheckboxDefaults.colors(
                        checkedColor = colors.primary,
                        uncheckedColor = colors.textSecondary,
                        checkmarkColor = Color.White,
                        disabledCheckedColor = colors.primary.copy(alpha = 0.4f),
                        disabledUncheckedColor = colors.textDisabled
                    ),
                    onCheckedChange = {
                        stateUserAvailable = stateUserAvailable.copy(termsAccepted = it)
                    }
                )

                Text(
                    text = "Acepto los términos y condiciones",
                    modifier = Modifier.clickable {
                        stateUserAvailable = stateUserAvailable.copy(showTermsDialog = true)
                    },
                    style = NavTechTheme.typography.bodyMedium,
                    color = NavTechTheme.colors.primary.copy(alpha = 0.7F)
                )
            }
            Spacer(Modifier.height(24.dp))
            // 🔹 BOTÓN
            NavTechButton(
                text = "Continuar",
                enabled = isRutValid && !state.isLoading && stateUserAvailable.termsAccepted,
                isLoading = state.isLoading,
                onClick = { onCheckStatus(rut) },
                modifier = Modifier.fillMaxWidth()
            )

            AnimatedVisibility(visible = state.error != null) {

                Column {
                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = state.error ?: "",
                        color = colors.error,
                        style = NavTechTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
    if (stateUserAvailable.showTermsDialog) {
        TermsDialog(
            onDismiss = {
                stateUserAvailable = stateUserAvailable.copy(showTermsDialog = false)
            }
        )
    }
}


@Composable
fun TermsDialog(
    onDismiss: () -> Unit
) {

    Dialog(onDismissRequest = onDismiss) {

        Surface(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
        ) {

            Column {

                // HEADER
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Text(
                        text = "Términos y condiciones",
                        style = MaterialTheme.typography.titleMedium
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                // WEBVIEW
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { context ->
                        WebView(context).apply {

                            // La página de términos es una SPA: requiere JavaScript
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.loadWithOverviewMode = true
                            settings.useWideViewPort = true
                            settings.allowFileAccess = false
                            settings.allowContentAccess = false

                            // Bloquea la navegación fuera del origen de los términos
                            webViewClient = object : WebViewClient() {
                                override fun shouldOverrideUrlLoading(
                                    view: WebView?,
                                    request: WebResourceRequest?
                                ): Boolean = !TermsWebPolicy.isAllowedNavigation(
                                    URL_TC,
                                    request?.url?.toString()
                                )
                            }

                            loadUrl(URL_TC)
                        }
                    }
                )
            }
        }
    }
}

fun isValidRut(rut: String): Boolean {

    val cleanRut = rut
        .replace(".", "")
        .replace("-", "")
        .uppercase()

    if (cleanRut.length < 2) return false

    val body = cleanRut.dropLast(1)
    val dv = cleanRut.last()

    if (!body.all { it.isDigit() }) return false

    var sum = 0
    var multiplier = 2

    for (i in body.reversed()) {
        sum += (i.toString().toInt() * multiplier)
        multiplier = if (multiplier == 7) 2 else multiplier + 1
    }

    val expectedDv = 11 - (sum % 11)

    val dvCalculated = when (expectedDv) {
        11 -> '0'
        10 -> 'K'
        else -> expectedDv.toString()[0]
    }

    return dv == dvCalculated
}

//val URL_TC = "https://njhhvsmhhpdxmlmcsoao.supabase.co/functions/v1/terms-html"
val URL_TC = "https://argos2fa-production.up.railway.app/terms"


@Composable
fun ShimmerCard() {
    val shimmerAlpha by rememberInfiniteTransition().animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        )
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .alpha(shimmerAlpha),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFDADADA)
        )
    ) {}
}

@Composable
fun VerificationIndicator(verified: Boolean) {
    val color = if (verified) Color(0xFF4CAF50) else Color(0xFFFF9800)
    val text = if (verified) " Verificado" else "Pendiente"
    Text(text, color = color, fontWeight = FontWeight.SemiBold)
}

/**
 * Preview
 ***/

@Preview(showBackground = true)
@Composable
fun Preview_UserStatus_Initial() {
    NavTechTheme {
        UserStatusContent(
            state = UserStatusState(),
            onCheckStatus = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
fun Preview_UserStatus_no_registered() {
    NavTechTheme {
        val fakeUser = UserStatusDetail(
            id = "1234",
            email = "test@email.com",
            phone = "+56989374",
            rut = "1-2",
            email_verified = false,
            phone_verified = false,
            kyc_status = "pending",
            has_active_device = false
        )

        val response = UserStatusResponse(
            registered = false,
            status = "pre_registered",
            next_step = "verify_email",
            completed_steps = listOf("email_verified"),
            user = fakeUser,
            error = null,
            sessionExpiresAt = "",
            sessionTimeoutMinutes = 0
        )

        UserStatusContent(
            state = UserStatusState(
                rut = "12.345.678-9",
                data = response,
                isLoading = true,
            ),
            onCheckStatus = {},
        )
    }
}

// con valores pendientes
@Preview(showBackground = true)
@Composable
fun Preview_UserStatus_Pending() {
    NavTechTheme {
        val fakeUser = UserStatusDetail(
            id = "1234",
            email = "test@email.com",
            rut = "1-2",
            phone = "+56989374",
            email_verified = false,
            phone_verified = false,
            kyc_status = "pending",
            has_active_device = false
        )

        val response = UserStatusResponse(
            registered = true,
            status = "pre_registered",
            next_step = "verify_email",
            completed_steps = listOf("email_verified", "jsjs"),
            user = fakeUser,
            error = null,
            sessionExpiresAt = "",
            sessionTimeoutMinutes = 0
        )

        UserStatusContent(
            state = UserStatusState(
                rut = "12.345.678-9",
                data = response
            ),
            onCheckStatus = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
fun Preview_UserStatus_Loading() {
    NavTechTheme {
        val fakeUser = UserStatusDetail(
            id = "1234",
            email = "test@email.com",
            phone = "+56989374",
            rut = "1-2",
            email_verified = false,
            phone_verified = false,
            kyc_status = "pending",
            has_active_device = false
        )

        val response = UserStatusResponse(
            registered = true,
            status = "pre_registered",
            next_step = "verify_email",
            completed_steps = listOf("email_verified"),
            user = fakeUser,
            error = null,
            sessionExpiresAt = "",
            sessionTimeoutMinutes = 0
        )

        UserStatusContent(
            state = UserStatusState(
                rut = "12.345.678-9",
                data = response,
                isLoading = true,
            ),
            onCheckStatus = {},
        )
    }
}