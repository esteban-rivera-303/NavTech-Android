package com.zanaaustralis.feature_auth.presentation.recovery

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
import androidx.compose.material3.Icon
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zanaaustralis.core.R
import com.zanaaustralis.core.common.ui.component.NavTechButton
import com.zanaaustralis.core.common.ui.component.NavTechInput
import com.zanaaustralis.core.common.ui.theme.NavTechTheme
import com.zanaaustralis.core.domain.model.User
import com.zanaaustralis.feature_auth.presentation.common.NavStepScreen


/**
 * @author esteban.rivera01
 */

@Composable
fun RecoveryStartScreen(
    user: User,
    onNext: (String, String, String) -> Unit,
    viewModel: RecoveryViewModel = hiltViewModel()
) {

    val state by viewModel.uiState.collectAsState()
    val colors = NavTechTheme.colors

    val deviceUuid = viewModel.getDeviceUUID()

    var rut by remember { mutableStateOf("") }

    LaunchedEffect(state.initializedSuccess) {
        if (state.initializedSuccess) {
            onNext(user.rut, user.email,user.phone )
        }
    }

    Box(modifier = Modifier) {

        NavStepScreen(
            title = "Recuperar cuenta",
            step = 0,
            totalSteps = 0,
            bottomContent = {
                NavTechButton(
                    text = if (state.isLoading) "Iniciando..." else "Iniciar recuperación",
                    onClick = { viewModel.initiate(rut, deviceUuid) },
                    enabled = rut.isNotBlank() && !state.isLoading,
                    isLoading = state.isLoading,
                    modifier = Modifier.fillMaxWidth().padding(bottom =  36.dp )
                )
            }
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.background)
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(48.dp))
                Icon(
                painter = painterResource(R.drawable.logo_navtech_color),
                contentDescription = null,
                modifier = Modifier.size(156.dp),
                tint = Color(0xFF2962FF)
            )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Ingresa tu RUT",
                    style = NavTechTheme.typography.titleMedium,
                    color = colors.textPrimary
                )

                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Verificaremos tu identidad para comenzar el proceso de recuperación.",
                    style = NavTechTheme.typography.bodyMedium,
                    color = colors.textSecondary
                )

                Spacer(Modifier.height(24.dp))

                NavTechInput(
                    value = rut,
                    onValueChange = { rut = it },
                    placeholder = "12.345.678-9"
                )

                Spacer(Modifier.height(16.dp))

                // 🔴 Error
                state.error?.let {
                    Text(
                        text = it,
                        color = colors.error,
                        style = NavTechTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
/*@Composable
fun RecoveryStartScreen(
    onNext: (String, String) -> Unit,
    viewModel: RecoveryViewModel = hiltViewModel()
) {
    val deviceUuid = viewModel.getDeviceUUID()
    var rut by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Recuperar cuenta", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text("Ingresa tu RUT para verificar tu identidad y comenzar la recuperación.")
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(value = rut, onValueChange = { rut = it }, label = { Text("RUT") })
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { viewModel.initiate(rut, deviceUuid) },
            enabled = rut.isNotBlank()
        ) { Text("Iniciar recuperación") }
    }

    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(state.step) {
        if (state.step == 2) onNext(rut, deviceUuid)
    }
}*/

/*
@Preview(showBackground = true)
@Composable
fun PreviewRecoveryStartScreen2() {
    RecoveryStartScreen(
        onNext = { }
    )
}
*/
