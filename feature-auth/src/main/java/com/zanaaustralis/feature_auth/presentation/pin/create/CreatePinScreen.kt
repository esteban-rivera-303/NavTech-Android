package com.zanaaustralis.feature_auth.presentation.pin.create

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zanaaustralis.core.common.ui.theme.NavTechTheme
import com.zanaaustralis.feature_auth.R
import com.zanaaustralis.feature_auth.presentation.pin.CreatePinStep
import com.zanaaustralis.feature_auth.presentation.pin.ui.PinCreateStyle
import com.zanaaustralis.feature_auth.presentation.pin.ui.PinDots
import com.zanaaustralis.feature_auth.presentation.pin.ui.PinInput
import com.zanaaustralis.feature_auth.presentation.pin.ui.PinKeyboard

/**
 * @author esteban.rivera01
 */

@Composable
fun CreatePinScreen(
    onPinCreated: () -> Unit,
    onCancel: () -> Unit,
    viewModel: CreatePinViewModel = hiltViewModel()
) {
    val step by viewModel.step.collectAsState()
    val error by viewModel.error.collectAsState()

    CreatePinContent(
        step = step,
        error = error,
        onPinComplete = { pin ->
            viewModel.onPinCompleted(pin, onPinCreated)
        },
        onCancel = onCancel
    )
}
@Composable
fun CreatePinContent(
    step: CreatePinStep,
    error: String?,
    onPinComplete: (String) -> Unit,
    onCancel: () -> Unit
) {

    var pin by remember { mutableStateOf("") }

    val style = PinCreateStyle

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 24.dp)
            .background(Color.White) // 👈 cambio clave

    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(Modifier.height(32.dp))

            // 👈 Header simple (como diseño blanco)
            Icon(
                painter = painterResource(com.zanaaustralis.core.R.drawable.logo_navtech_color),
                contentDescription = null,
                modifier = Modifier.size(156.dp),
                tint = Color(0xFF2962FF)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Crea tu PIN",
                style = MaterialTheme.typography.titleLarge,
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = when (step) {
                    CreatePinStep.ENTER -> "Crea un PIN para autorizar acciones en la App"
                    CreatePinStep.CONFIRM -> "Ingresa nuevamente tu PIN"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(32.dp))

            // 👇 dots con estilo nuevo
            PinDots(
                filledCount = pin.length,
                total = 6,
                style = style
            )

            Spacer(Modifier.height(24.dp))

            error?.let {
                Text(
                    text = it,
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.weight(1f))

            // 👇 teclado con botones circulares
            PinKeyboard(
                style = style,
                onNumber = { digit ->
                    if (pin.length < 6) {
                        pin += digit
                    }

                    if (pin.length == 6) {
                        onPinComplete(pin)
                        pin = ""
                    }
                },
                onDelete = {
                    if (pin.isNotEmpty()) {
                        pin = pin.dropLast(1)
                    }
                }
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Cancelar",
                color = Color.Gray,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.clickable { onCancel() }
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}
/*
@Composable
fun CreatePinContent(
    step: CreatePinStep,
    error: String?,
    onPinComplete: (String) -> Unit,
    onCancel: () -> Unit
) {

    var pin by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF1E4FD8),
                        Color(0xFF0B2B6B)
                    )
                )
            )
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(Modifier.height(40.dp))

            Text(
                text = " ",
                color = Color.White.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Icon(
                    painter = painterResource(com.zanaaustralis.core.R.drawable.logo),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    text = "navtech",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(48.dp))

            Text(
                text = when (step) {
                    CreatePinStep.ENTER -> "Crea tu PIN"
                    CreatePinStep.CONFIRM -> "Confirma tu PIN"
                },
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = when (step) {
                    CreatePinStep.ENTER -> "Este PIN protegerá tus autorizaciones."
                    CreatePinStep.CONFIRM -> "Ingresa nuevamente tu PIN."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(32.dp))

            PinDots(
                filledCount = pin.length
            )

            Spacer(Modifier.height(24.dp))

            error?.let {
                Text(
                    text = it,
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(Modifier.height(8.dp))
            }

            Spacer(Modifier.weight(1f))

            PinKeyboard(
                onNumber = { digit ->
                    if (pin.length < 6) {
                        pin += digit
                    }

                    if (pin.length == 6) {
                        onPinComplete(pin)
                        pin = ""
                    }
                },
                onDelete = {
                    if (pin.isNotEmpty()) {
                        pin = pin.dropLast(1)
                    }
                }
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = "CANCELAR",
                color = Color.White.copy(alpha = 0.8f),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.clickable {
                    onCancel()
                }
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}
*/

// PREVIEW
@Preview(
    showBackground = true,
    device = "id:pixel_6",
    name = "Create PIN – Step ENTER"
)
@Composable
fun CreatePinScreenEnterPreview() {
    MaterialTheme {
        CreatePinContent(
            step = CreatePinStep.ENTER,
            error = null,
            onPinComplete = {},
            onCancel = {}
        )
    }
}

@Preview(
    showBackground = true,
    device = "id:pixel_6",
    name = "Create PIN – Step CONFIRM (Error)"
)
@Composable
fun CreatePinScreenConfirmErrorPreview() {
    MaterialTheme {
        CreatePinContent(
            step = CreatePinStep.CONFIRM,
            error = "Los PIN no coinciden",
            onPinComplete = {},
            onCancel = {}
        )
    }
}
