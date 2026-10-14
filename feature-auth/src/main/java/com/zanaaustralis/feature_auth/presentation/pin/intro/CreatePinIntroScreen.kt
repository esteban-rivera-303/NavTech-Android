package com.zanaaustralis.feature_auth.presentation.pin.intro

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zanaaustralis.core.common.ui.PrimaryButton
import com.zanaaustralis.core.common.ui.theme.NavTechTheme
import com.zanaaustralis.feature_auth.R

/**
 * @author esteban.rivera01
 */

@Composable
fun CreatePinIntroScreen(
    onCreatePin: () -> Unit,
    onRecoverPin: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
            //.windowInsetsPadding(WindowInsets.systemBars)
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_lock_24),
                contentDescription = null,
                tint = NavTechTheme.colors.primary,
                modifier = Modifier.size(72.dp)
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Protege tu cuenta",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Configura un PIN para autorizar operaciones sensibles.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }

        Column {
            PrimaryButton(
                text = "Crear PIN",
                onClick = onCreatePin,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            TextButton(
                onClick = onRecoverPin,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Restaurar PIN")
            }

            Spacer(Modifier.height(8.dp))

            TextButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancelar", color = Color.Gray)
            }
        }
    }
}

@Preview(
    showBackground = true,
    device = "id:pixel_6",
    name = "CreatePinIntroScreen"
)
@Composable
fun CreatePinIntroScreenPreview() {
    MaterialTheme {
        CreatePinIntroScreen(
            onCreatePin = {},
            onRecoverPin = {},
            onBack = {}
        )
    }
}
