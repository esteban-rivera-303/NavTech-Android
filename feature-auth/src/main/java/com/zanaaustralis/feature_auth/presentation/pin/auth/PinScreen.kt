package com.zanaaustralis.feature_auth.presentation.pin.auth

import android.app.Activity
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.zanaaustralis.feature_auth.presentation.pin.ui.PinAuthStyle
import com.zanaaustralis.feature_auth.presentation.pin.ui.PinDots
import com.zanaaustralis.feature_auth.presentation.pin.ui.PinKeyboard

/**
 * @author esteban.rivera01
 */

@Composable
fun PinScreen(
    viewModel: PinViewModel = hiltViewModel(),
    onPinValidated: () -> Unit,
    onCancel: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    val view = LocalView.current
    val window = (view.context as Activity).window

    DisposableEffect(Unit) {

        // Guardamos el estado actual
        val previousStatusBar = window.statusBarColor
        val previousNavigationBar = window.navigationBarColor

        val controller = WindowCompat.getInsetsController(window, view)
        val previousLightStatus = controller.isAppearanceLightStatusBars
        val previousLightNav = controller.isAppearanceLightNavigationBars

        // Aplicamos el estilo del PIN
        window.statusBarColor = Color(0xFF1E4FD8).toArgb()
        window.navigationBarColor = Color(0xFF1E4FD8).toArgb()

        controller.isAppearanceLightStatusBars = false
        controller.isAppearanceLightNavigationBars = false

        onDispose {
            // Restauramos el estado anterior
            window.statusBarColor = previousStatusBar
            window.navigationBarColor = previousNavigationBar

            controller.isAppearanceLightStatusBars = previousLightStatus
            controller.isAppearanceLightNavigationBars = previousLightNav
        }
    }

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
                .padding(horizontal = 24.dp)
        ) {

            // ---------------- HEADER ----------------

            Column(
                modifier = Modifier
                    .weight(0.30f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                Text(
                    text = "Autenticador",
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(
                            com.zanaaustralis.core.R.drawable.logo
                        ),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(42.dp)
                    )

                    Spacer(Modifier.width(10.dp))

                    Text(
                        text = "navtech",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(24.dp))

                Text(
                    text = "¡Hola!",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            // ---------------- PIN ----------------

            Column(
                modifier = Modifier
                    .weight(0.20f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                PinDots(
                    filledCount = pin.length,
                    style = PinAuthStyle
                )

                Spacer(Modifier.height(16.dp))

                uiState.error?.let {
                    Text(
                        text = it,
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // ---------------- KEYBOARD ----------------

            Column(
                modifier = Modifier
                    .weight(0.50f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {

                PinKeyboard(
                    style = PinAuthStyle,
                    onNumber = { digit ->
                        if (pin.length < 6 &&
                            !uiState.loading &&
                            !uiState.blocked
                        ) {
                            pin += digit
                        }

                        if (pin.length == 6 &&
                            !uiState.blocked
                        ) {
                            viewModel.validatePin(pin) {
                                onPinValidated()
                            }
                            pin = ""
                        }
                    },
                    onDelete = {
                        if (pin.isNotEmpty() &&
                            !uiState.loading &&
                            !uiState.blocked
                        ) {
                            pin = pin.dropLast(1)
                        }
                    }
                )

                Text(
                    text = "OLVIDÉ MI PIN",
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.clickable(
                        enabled = !uiState.loading
                    ) {
                        onCancel()
                    }
                )
            }
        }
    }
}


@Preview(showBackground = true, device = "id:pixel_6")
@Composable
fun PinScreenPreview() {
    MaterialTheme {
        PinScreen(
            onPinValidated = { println("PIN ingresado:") },
            onCancel = { println("Cancelado") }
        )
    }
}
