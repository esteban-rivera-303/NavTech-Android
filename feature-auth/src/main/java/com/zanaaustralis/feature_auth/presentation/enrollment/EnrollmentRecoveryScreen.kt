package com.zanaaustralis.feature_auth.presentation.enrollment

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zanaaustralis.core.common.ui.component.NavTechButton
import com.zanaaustralis.core.common.ui.theme.NavTechTheme
import com.zanaaustralis.feature_auth.R
import com.zanaaustralis.feature_auth.presentation.common.NavStepScreen

/**
 * @author esteban.rivera01
 */


@Composable
fun EnrollmentRecoveryScreen(
    rut: String,
    onDone: () -> Unit,
    viewModel: EnrollmentViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var showMessage by remember { mutableStateOf<String?>(null) }
    val deviceInfo = viewModel.getDeviceInfo()

    EnrollmentRecoveryContent(
        rut = rut,
        device = deviceInfo.manufacturer,
        model = deviceInfo.model,
        state = state,
        messageExtra = showMessage,
        onEnroll = { rutV ->
            viewModel.recoveryEnrollDevice(rutV) { success, msg ->
                showMessage = msg
                if (success) onDone()
            }
        },
        onDone = onDone
    )
}

@Composable
fun EnrollmentRecoveryContent(
    rut: String,
    device: String,
    model: String,
    state: EnrollmentUiState,
    messageExtra: String?,
    onEnroll: (String) -> Unit,
    onDone: () -> Unit
) {

    NavStepScreen(
        title = "Enrolamiento dispositivo",
        step = 0,
        totalSteps = 0,
        bottomContent = {
            NavTechButton(
                text = "Finalizar Enrolamiento",
                onClick = { onEnroll(rut) },
                enabled = !state.isLoading,
                isLoading = state.isLoading,
                modifier = Modifier.fillMaxWidth().padding(bottom = 64.dp)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(8.dp))
            Icon(
                painter = painterResource(com.zanaaustralis.core.R.drawable.logo),
                contentDescription = null,
                tint = NavTechTheme.colors.primary,
                modifier = Modifier.size(120.dp)
            )
            Spacer(Modifier.height(8.dp))
            EnrollmentRecoveryDataSummary(rut, device, model)

            Spacer(Modifier.height(16.dp))

            // Mensajes
            when {
                state.isLoading ->
                    CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))

                state.message != null ||
                        messageExtra != null -> {
                    val msg = state.message ?: messageExtra ?: ""
                    Text(
                        text = msg,
                        color = if (state.success) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
    if (state.success) {
        LaunchedEffect(Unit) { onDone() }
    }
}

@Composable
fun EnrollmentRecoveryDataSummary(
    rut: String,
    device: String,
    model: String
) {

    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        DataLineRecovery(label = "RUT:", value = rut)
        Spacer(Modifier.height(4.dp))

        DataLineRecovery(label = "Dispositivo:", value = device)
        Spacer(Modifier.height(4.dp))

        DataLineRecovery(label = "Model:", value = model)
    }
}

@Composable
fun DataLineRecovery(label: String, value: String) {
    Row {
        Text(label, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(6.dp))
        Text(value, fontWeight = FontWeight.Normal)
        Spacer(Modifier.width(6.dp))
        if (label != "RUT:") {
            Icon(
                painter = painterResource(id = R.drawable.baseline_verified_user_24),
                contentDescription = null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

/**
 * Preview
 * */

@Preview(showBackground = true)
@Composable
fun Preview_Enrollment_Recovery_Default() {
    NavTechTheme {
        EnrollmentRecoveryContent(
            rut = "12.345.678-9",
            device = "usuario@mail.com",
            model = "+56999887766",
            state = EnrollmentUiState(isLoading = false),
            messageExtra = null,
            onEnroll = { _ -> },
            onDone = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun Preview_Enrollment_Recovery_Loading() {
    NavTechTheme {
        EnrollmentRecoveryContent(
            rut = "12.345.678-9",
            device = "usuario@mail.com",
            model = "+56999887766",
            state = EnrollmentUiState(isLoading = true),
            messageExtra = null,
            onEnroll = { _ -> },
            onDone = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun Preview_Enrollment_Recovery_Success() {
    NavTechTheme {
        EnrollmentRecoveryContent(
            rut = "12.345.678-9",
            device = "usuario@mail.com",
            model = "+56999887766",
            state = EnrollmentUiState(
                success = true,
                message = "Dispositivo enrolado exitosamente"
            ),
            messageExtra = null,
            onEnroll = { _ -> },
            onDone = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun Preview_Enrollment_Recovery_Error() {
    NavTechTheme {
        EnrollmentRecoveryContent(
            rut = "12.345.678-9",
            device = "usuario@mail.com",
            model = "+56999887766",
            state = EnrollmentUiState(
                success = false,
                message = "Ocurrió un problema con el enrolamiento"
            ),
            messageExtra = null,
            onEnroll = { _ -> },
            onDone = {}
        )
    }
}