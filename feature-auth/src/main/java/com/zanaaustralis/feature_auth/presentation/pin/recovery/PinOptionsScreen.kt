package com.zanaaustralis.feature_auth.presentation.pin.recovery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zanaaustralis.core.common.ui.PrimaryButton
import com.zanaaustralis.core.common.ui.SecondaryButton

/**
 * @author esteban.rivera01
 */
@Composable
fun PinOptionsScreen(
    onChangePin: () -> Unit,
    onForgotPin: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            //.windowInsetsPadding(WindowInsets.systemBars)
            .padding(horizontal = 24.dp)
    ) {

        // Flecha superior
        IconButton(
            onClick = onBack,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.ArrowBack,
                contentDescription = "Volver"
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {

            // 📝 Texto
            Column {
                Text(
                    text = "Tu PIN está activo",
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "Puedes cambiar tu PIN o restablecerlo si lo olvidaste.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

            Spacer(Modifier.height(32.dp))

            // 🔘 Acciones
            Column {
                PrimaryButton(
                    text = "Cambiar PIN",
                    onClick = onChangePin,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                SecondaryButton(
                    text = "Olvidé mi PIN",
                    onClick = onForgotPin,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/*@Composable
fun PinOptionsScreen(
    onChangePin: () -> Unit,
    onForgotPin: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .windowInsetsPadding(WindowInsets.systemBars),
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        Column {
            Text(
                text = "Tu PIN está activo",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Puedes cambiar tu PIN o restablecerlo si lo olvidaste.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }

        Column {
            PrimaryButton(
                text = "Cambiar PIN",
                onClick = onChangePin,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            SecondaryButton(
                text = "Olvidé mi PIN",
                onClick = onForgotPin,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            TextButton(onClick = onBack) {
                Text("Volver")
            }
        }
    }
}*/

@Preview(
    showBackground = true,
    device = "id:pixel_6",
    name = "PinOptionsScreen Preview"
)
@Composable
fun PinOptionsScreenPreview() {
    MaterialTheme {
        PinOptionsScreen(
            onChangePin = {
                println("Cambiar PIN")
            },
            onForgotPin = {
                println("Olvidé mi PIN")
            },
            onBack = {
                println("Volver")
            }
        )
    }
}

