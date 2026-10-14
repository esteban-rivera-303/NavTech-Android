package com.zanaaustralis.feature_auth.presentation.register

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zanaaustralis.core.common.ui.component.NavTechButton
import com.zanaaustralis.core.common.ui.component.NavTechHeader
import com.zanaaustralis.core.common.ui.component.NavTechInput
import com.zanaaustralis.core.common.ui.component.NavTechPhoneInput
import com.zanaaustralis.core.common.ui.theme.NavTechTheme


/**
 * @author esteban.rivera01
 */

@Composable
fun RegisterScreen(
    rutPrefill: String? = null,
    onDone: (email: String, phone: String, rut: String) -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val state by viewModel.uiStateRegister.collectAsState()

    RegisterContent(
        state = state,
        rutPrefill = rutPrefill,
        onRegister = { rut, email, phone ->
            viewModel.registerUser(rut, email, phone)
        },
        onNext = { email, phone, rut ->
            onDone(email,phone,rut, )
        }
    )
}
@Composable
fun RegisterContent(
    state: RegisterUiState,
    rutPrefill: String? = null,
    onRegister: (rut: String, email: String, phone: String) -> Unit,
    onNext: (email: String, phone: String, rut: String) -> Unit
) {

    val colors = NavTechTheme.colors

    var rut by remember { mutableStateOf(rutPrefill ?: "") }
    var email by remember(state.email) { mutableStateOf(state.email ?: "") }
    var phone by remember { mutableStateOf(state.phone ?: "") }

    // Handling error
    var emailTouched by remember { mutableStateOf(false) }
    val emailValid = isValidEmail(email)
    val showEmailError = emailTouched && !emailValid

    val isValid =
        rut.isNotBlank() &&
                email.isNotBlank() &&
                phone.isNotBlank() &&
                phone.length == 9 &&
                !state.isLoading

    // Navegación
    LaunchedEffect(state.success) {
        if (state.success) {
            onNext(email, phone, rut)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {

        // 🔷 HEADER NUEVO
        NavTechHeader(
            title = "Registro",
            subtitle = "Ingresa tus datos para crear tu cuenta."
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {

            // RUT
            Text(
                text = "Rut",
                style = NavTechTheme.typography.labelMedium,
                color = colors.textPrimary
            )

            Spacer(Modifier.height(6.dp))

            NavTechInput(
                value = rut,
                onValueChange = { rut = it },
                placeholder = "12.345.678-9"
            )

            Spacer(Modifier.height(16.dp))

            // EMAIL
            Text(
                text = "Correo electrónico",
                style = NavTechTheme.typography.labelMedium,
                color = colors.textPrimary
            )

            Spacer(Modifier.height(6.dp))

            NavTechInput(
                value = email,
                onValueChange = {
                    email = it
                    emailTouched = true
                },
                placeholder = "Escribe tu correo",
                isError = showEmailError,
                errorMessage = if (showEmailError) "Correo inválido" else null
            )

            Spacer(Modifier.height(16.dp))

            // PHONE
            Text(
                text = "Número de teléfono",
                style = NavTechTheme.typography.labelMedium,
                color = colors.textPrimary
            )

            Spacer(Modifier.height(6.dp))

            NavTechPhoneInput(
                value = phone,
                onValueChange = { phone = it }
            )

            Spacer(Modifier.height(28.dp))

            // 🔘 BOTÓN NUEVO
            NavTechButton(
                text = if (state.isLoading) "Registrando..." else "Continuar",
                enabled = isValid,
                isLoading = state.isLoading,
                onClick = {
                    onRegister(rut, email, phone)
                }
            )

            Spacer(Modifier.height(16.dp))

            // Mensajes
            state.message?.let {
                Text(
                    text = it,
                    style = NavTechTheme.typography.bodyMedium,
                    color = if (state.success) colors.success else colors.error
                )
            }
        }
    }
}

fun isValidEmail(email: String): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

/**
 * Preview
 * */

@Preview(showBackground = true)
@Composable
fun Preview_Register_Initial() {
    NavTechTheme {
        RegisterContent(
            state = RegisterUiState(),
            rutPrefill = null,
            onRegister = { _, _, _ -> },
            onNext = { _, _, _ -> }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun Preview_Register_PrefilledRut() {
    NavTechTheme {
        RegisterContent(
            state = RegisterUiState(),
            rutPrefill = "12.345.678-9",
            onRegister = { _, _, _ -> },
            onNext = { _, _, _ -> }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun Preview_Register_Loading() {
    NavTechTheme {
        RegisterContent(
            state = RegisterUiState(
                isLoading = true
            ),
            rutPrefill = null,
            onRegister = { _, _, _ -> },
            onNext = { _, _, _ -> }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun Preview_Register_Success() {
    NavTechTheme {
        RegisterContent(
            state = RegisterUiState(
                success = true,
                message = "Registro exitoso",
            ),
            rutPrefill = "11.222.333-4",
            onRegister = { _, _, _ -> },
            onNext = { _, _, _ -> }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun Preview_Register_AlreadyExists() {
    NavTechTheme {
        RegisterContent(
            state = RegisterUiState(
                alreadyPreregister = true,
                email = "user@mail.com",
                phone = "+56944556677",
                rut = "18.443.123-6"
            ),
            rutPrefill = null,
            onRegister = { _, _, _ -> },
            onNext = { _, _, _ -> }
        )
    }
}
