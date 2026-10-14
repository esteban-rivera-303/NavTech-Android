package com.zanaaustralis.feature_auth.presentation.recovery.legacy

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zanaaustralis.feature_auth.R

/**
 * @author esteban.rivera01
 */

@Composable
fun RecoveryCompleteScreen(onProceedToEnrollment: (String) -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.baseline_verified_user_24),
            contentDescription = null,
            tint = Color(0xFF4CAF50),
            modifier = Modifier.size(64.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text("Recuperación completada", fontWeight = FontWeight.Bold)
        Text("Tu cuenta ha sido verificada. Puedes enrolar este dispositivo como nuevo principal.")
        Spacer(Modifier.height(24.dp))
        Button(onClick = { onProceedToEnrollment("123") }) {
            Text("Enrolar dispositivo")
        }
    }
}

/*@Preview(showBackground = true)
@Composable
fun PreviewRecoveryStartScreen() {
    RecoveryStartScreen(
        onNext = {"","","" -> }
    )
}*/

/*
@Preview(showBackground = true)
@Composable
fun PreviewRecoveryVerifyScreen() {
    RecoveryVerifyScreen(
        rut = "12.345.678-9",
        deviceUuid = "mock-device-uuid",
        onRecoveryComplete = {}
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewRecoveryCompleteScreen() {
    RecoveryCompleteScreen(onProceedToEnrollment = {_ -> })
}
*/
