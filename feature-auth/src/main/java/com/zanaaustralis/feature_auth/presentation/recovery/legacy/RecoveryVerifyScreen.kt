package com.zanaaustralis.feature_auth.presentation.recovery.legacy

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zanaaustralis.feature_auth.presentation.recovery.RecoveryViewModel

/**
 * @author esteban.rivera01
 */

/*
@Composable
fun RecoveryVerifyScreen(
    rut: String,
    deviceUuid: String,
    onRecoveryComplete: () -> Unit,
    viewModel: RecoveryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var emailCode by remember { mutableStateOf("") }
    var phoneCode by remember { mutableStateOf("") }

    Column(
        Modifier.fillMaxSize().padding(24.dp).background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Verificación de identidad", fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("Te enviamos códigos a tu correo y teléfono registrados.")

        Spacer(Modifier.height(24.dp))
        OutlinedTextField(value = emailCode, onValueChange = { emailCode = it }, label = { Text("Código de correo") })
        Button(onClick = { viewModel.verifyEmail(emailCode) }) { Text("Verificar correo") }

        Spacer(Modifier.height(16.dp))
        OutlinedTextField(value = phoneCode, onValueChange = { phoneCode = it }, label = { Text("Código de teléfono") })
        Button(onClick = { viewModel.verifyPhone(phoneCode) }) { Text("Verificar teléfono") }

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { viewModel.completeRecovery() },
            enabled = state.emailVerified && state.phoneVerified
        ) { Text("Completar recuperación") }
    }

    LaunchedEffect(state.step) {
        if (state.step == 4) onRecoveryComplete()
    }
}
*/
