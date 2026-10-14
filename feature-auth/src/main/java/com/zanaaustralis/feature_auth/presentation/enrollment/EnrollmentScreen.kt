package com.zanaaustralis.feature_auth.presentation.enrollment

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zanaaustralis.core.common.ui.component.NavTechButton
import com.zanaaustralis.core.common.ui.theme.NavTechTheme
import com.zanaaustralis.core.domain.model.OnboardingState
import com.zanaaustralis.feature_auth.R
import com.zanaaustralis.feature_auth.presentation.common.NavStepScreen

/**
 * @author esteban.rivera01
 */


@Composable
fun EnrollmentScreen(
    rut: String,
    email: String,
    phone: String,
    onDone: () -> Unit,
    viewModel: EnrollmentViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var showMessage by remember { mutableStateOf<String?>(null) }

    EnrollmentContent(
        rut = rut,
        email = email,
        phone = phone,
        state = state,
        messageExtra = showMessage,
        onEnroll = { rutV, emailV, phoneV ->
            val onboardingState = OnboardingState(
                rut = rutV,
                email = emailV,
                phone = phoneV
            )

            viewModel.enrollDevice(onboardingState) { success, msg ->
                showMessage = msg
                if (success) onDone()
            }
        },
        onDone = onDone
    )
}

@Composable
fun EnrollmentContent(
    rut: String,
    email: String,
    phone: String,
    state: EnrollmentUiState,
    messageExtra: String?,
    onEnroll: (String, String, String) -> Unit,
    onDone: () -> Unit
) {

    NavStepScreen(
        title = "Enrolamiento dispositivo",
        step = 1,
        totalSteps = 1,
        bottomContent = {
            NavTechButton(
                text = "Continuar",
                onClick = { onEnroll(rut, email, phone) },
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

            Icon(
                painter = painterResource(com.zanaaustralis.core.R.drawable.logo),
                contentDescription = null,
                tint = NavTechTheme.colors.primary,
                modifier = Modifier.size(120.dp)
            )

            Spacer(Modifier.height(8.dp))

            Text("Enrolamiento", style = NavTechTheme.typography.headlineMedium)

            Spacer(Modifier.height(16.dp))

            Text(
                "Registraremos este dispositivo para firmar desafíos.",
                textAlign = TextAlign.Center,
                        style = NavTechTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(24.dp))

            EnrollmentDataSummary(rut, email, phone)
        }
    }

    // 🔥 navegación fuera del layout
    if (state.success) {
        LaunchedEffect(Unit) { onDone() }
    }
}

@Composable
fun EnrollmentDataSummary(
    rut: String,
    email: String,
    phone: String
) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        DataLine("RUT:", rut)

        Spacer(Modifier.height(6.dp))

        DataLine("Email:", email, verified = true)

        Spacer(Modifier.height(6.dp))

        DataLine("Teléfono:", phone, verified = true)
    }
}


@Composable
fun DataLine(
    label: String,
    value: String,
    verified: Boolean = false
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontWeight = FontWeight.Bold)

        Spacer(Modifier.width(6.dp))

        Text(value)

        if (verified) {
            Spacer(Modifier.width(6.dp))

            Icon(
                painter = painterResource(id = R.drawable.baseline_verified_user_24),
                contentDescription = null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
/*@Composable
fun DataLine(label: String, value: String) {
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
}*/

/**
 * Preview
 * */

@Preview(showBackground = true)
@Composable
fun Preview_Enrollment_Default() {
    NavTechTheme {
        EnrollmentContent(
            rut = "12.345.678-9",
            email = "usuario@mail.com",
            phone = "+56999887766",
            state = EnrollmentUiState(isLoading = false),
            messageExtra = null,
            onEnroll = { _, _, _ -> },
            onDone = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun Preview_Enrollment_Loading() {
    NavTechTheme {
        EnrollmentContent(
            rut = "12.345.678-9",
            email = "usuario@mail.com",
            phone = "+56999887766",
            state = EnrollmentUiState(isLoading = true),
            messageExtra = null,
            onEnroll = { _, _, _ -> },
            onDone = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun Preview_Enrollment_Success() {
    NavTechTheme {
        EnrollmentContent(
            rut = "12.345.678-9",
            email = "usuario@mail.com",
            phone = "+56999887766",
            state = EnrollmentUiState(
                success = true,
                message = "Dispositivo enrolado exitosamente"
            ),
            messageExtra = null,
            onEnroll = { _, _, _ -> },
            onDone = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun Preview_Enrollment_Error() {
    NavTechTheme {
        EnrollmentContent(
            rut = "12.345.678-9",
            email = "usuario@mail.com",
            phone = "+56999887766",
            state = EnrollmentUiState(
                success = false,
                message = "Ocurrió un problema con el enrolamiento"
            ),
            messageExtra = null,
            onEnroll = { _, _, _ -> },
            onDone = {}
        )
    }
}