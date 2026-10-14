package com.zanaaustralis.feature_auth.presentation.verification

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zanaaustralis.core.common.ui.component.NavTechButton
import com.zanaaustralis.core.common.ui.theme.NavTechTheme
import com.zanaaustralis.feature_auth.presentation.common.CodeInputRow
import com.zanaaustralis.feature_auth.presentation.common.NavStepScreen
import com.zanaaustralis.feature_auth.presentation.verify.VerificationType

/**
 * @author esteban.rivera01
 */
@Composable
fun EmailVerificationScreen(
    email: String,
    onSuccess: () -> Unit,
    step: Int,
    totalSteps: Int,
    verifyViewModel: VerifyViewModel = hiltViewModel()
) {

    val uiState by verifyViewModel.uiState.collectAsState()

    var code by remember { mutableStateOf(List(6) { "" }) }
    val fullCode = code.joinToString("")

    // 🔥 Reset al entrar
    LaunchedEffect(Unit) {
        verifyViewModel.reset()
    }

    // 🔥 Success
    LaunchedEffect(uiState.verified) {
        if (uiState.verified) onSuccess()
    }

    NavStepScreen(
        title = "Verificación correo",
        step = step,
        totalSteps = totalSteps,
        bottomContent = {

            NavTechButton(
                text = "Validar código",
                enabled = fullCode.length == 6 &&
                        !uiState.isLoading &&
                        !uiState.blocked,
                isLoading = uiState.isLoading,
                onClick = {
                    verifyViewModel.verifyCode(
                        target = email,
                        code = fullCode,
                        verificationType = VerificationType.EMAIL
                    )
                },
                modifier = Modifier.fillMaxWidth().padding(bottom = 64.dp)
            )
        }
    ) {

        Column(
            modifier = Modifier
                .background(NavTechTheme.colors.background)
                .align(Alignment.Center)
                .statusBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Ingresa el código enviado a",
                style = NavTechTheme.typography.bodyMedium,
                color = NavTechTheme.colors.textSecondary
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = email,
                style = NavTechTheme.typography.bodyLarge,
                color = NavTechTheme.colors.textPrimary
            )

            Spacer(Modifier.height(32.dp))

            CodeInputRow(
                code = code,
                onValueChange = { index, value ->
                    code = code.toMutableList().also {
                        it[index] = value
                    }
                }
            )

            Spacer(Modifier.height(20.dp))

            // 🔴 ERROR
            if (uiState.error != null && !uiState.verified) {
                Text(
                    text = uiState.error ?: "",
                    color = NavTechTheme.colors.error,
                    style = NavTechTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(12.dp))
            }

            if (uiState.message != null && !uiState.verified) {
                Text(
                    text = uiState.message ?: "",
                    color = NavTechTheme.colors.success,
                    style = NavTechTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(12.dp))
            }

            // 🔁 RESEND
            if (!uiState.canResend) {
                Text(
                    text = "Reenviar código en ${uiState.secondsRemaining}s",
                    style = NavTechTheme.typography.bodySmall,
                    color = NavTechTheme.colors.textSecondary
                )
            } else {
                Text(
                    text = "Reenviar código",
                    color = NavTechTheme.colors.primary,
                    modifier = Modifier.clickable {
                        verifyViewModel.resendCode(email, VerificationType.EMAIL)
                    }
                )
            }
        }
    }
}

/**
* PREVIEW
 */

@Preview(
    name = "Email Verification - Default",
    showBackground = true,
    device = "spec:width=411dp,height=891dp"
)
@Composable
fun PreviewEmailVerificationScreen() {
    NavTechTheme {

        EmailVerificationScreenPreviewContent(
            email = "usuario@mail.com"
        )
    }
}

@Composable
private fun EmailVerificationScreenPreviewContent(email: String) {

    var code by remember { mutableStateOf(List(6) { "" }) }

    NavStepScreen(
        title = "Verificación correo",
        step = 1,
        totalSteps = 3,
        onBack = {},
        bottomContent = {
            NavTechButton(
                text = "Validar código",
                enabled = false,
                onClick = {},
                modifier = Modifier.fillMaxWidth()
            )
        }
    ) {

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text("Ingresa el código enviado a")

            Spacer(Modifier.height(4.dp))

            Text(email, fontWeight = FontWeight.Bold)

            Spacer(Modifier.height(32.dp))

            CodeInputRow(
                code = code,
                onValueChange = { index, value ->
                    code = code.toMutableList().also {
                        it[index] = value
                    }
                }
            )

            Spacer(Modifier.height(20.dp))

            Text(
                text = "Reenviar código en 30s",
                color = NavTechTheme.colors.textSecondary
            )
        }
    }
}