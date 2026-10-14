package com.zanaaustralis.feature_challanges.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zanaaustralis.core.common.ui.theme.NavTechTheme
import com.zanaaustralis.feature_auth.AuthFactor
import com.zanaaustralis.feature_challanges.domain.model.ChallengeItem


/**
 * @author esteban.rivera01
 */

/*@Composable
fun ChallengeCard(challenge: ChallengeItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)

    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(challenge.appName, fontWeight = FontWeight.Bold)
            Text(challenge.description)
            Text("Monto: ${challenge.amount ?: 0}")
            Text("Estado: ${challenge.status}")
        }
    }
}*/

@Composable
fun ChallengeCard(
    challenge: ChallengeItem,
    onClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 12.dp)
            .clickable(enabled = challenge.status == "pendiente") { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8FF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // 🔹 Título y fecha
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = challenge.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Text(
                text = challenge.title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF2C2C2C)
            )

            Spacer(Modifier.height(8.dp))

            // 🔹 Descripción
            Text(
                text = challenge.description?:"",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF5A5A5A)
            )

            Spacer(Modifier.height(12.dp))

            // 🔹 Indicador según estado o tipo
            when (challenge.status) {
                "pendiente" -> {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (challenge.requiresBiometric) {
                                RequirementChip(
                                    text = if (AuthFactor.BIOMETRIC in challenge.authorizedFactors)
                                        "Biometría ✅" else "Biometría",
                                    backgroundColor = if (AuthFactor.BIOMETRIC in challenge.authorizedFactors)
                                        Color(0xFF4CAF50) else Color(0xFFFF6D6D)
                                )
                            }
                            if (challenge.isCritical) {
                                RequirementChip(
                                    text = if (AuthFactor.PIN in challenge.authorizedFactors)
                                        "PIN ✅" else "PIN",
                                    backgroundColor = if (AuthFactor.PIN in challenge.authorizedFactors)
                                        Color(0xFF4CAF50) else NavTechTheme.colors.primary
                                )
                            }
                            if (challenge.requiresTotp) {
                                RequirementChip(
                                    text = if (AuthFactor.TOTP in challenge.authorizedFactors)
                                        "TOTP ✅" else "TOTP",
                                    backgroundColor = if (AuthFactor.TOTP in challenge.authorizedFactors)
                                        Color(0xFF4CAF50) else Color(0xFF2196F3)
                                )
                            }
                            if (challenge.requiresLocation) {
                                RequirementChip(
                                    text = "Ubicación",
                                    backgroundColor = Color(0xFFFFC107)
                                )
                            }
                        }
                    }
                }

                "enviando" -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(16.dp)
                                .padding(end = 8.dp),
                            strokeWidth = 2.dp,
                            color = NavTechTheme.colors.primary
                        )
                        Text(
                            text = "Autorizando...",
                            color = NavTechTheme.colors.primary,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                "enviado" -> {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF4CAF50), RoundedCornerShape(50))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Autorizado ✅",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        AnimatedVisibility(visible = expanded) {
            Column {
                Spacer(Modifier.height(8.dp))
                Text("App: ${challenge.appName}", color = Color.Gray)
                challenge.amount?.let {
                    Text("Monto: $${it}", color = Color.Gray)
                }

                Spacer(Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { /* Autorizar siguiente factor */ },
                        colors = ButtonDefaults.buttonColors(containerColor = NavTechTheme.colors.primary)
                    ) {
                        Text("Autorizar")
                    }
                    OutlinedButton(
                        onClick = { /* Rechazar desafío */ },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                    ) {
                        Text("Rechazar")
                    }
                }
            }
        }
    }
}

@Composable
fun RequirementChip(text: String, backgroundColor: Color) {
    Box(
        modifier = Modifier
            .background(backgroundColor, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ChallengeCardPreview() {
    val fakeChallenge = ChallengeItem(
        id = "1234",
        appName = "Banco Ejemplo",
        description = "Autorización de transferencia bancaria",
        amount = 50000.0,
        status = "pending",
        createdAt = "2025-12-10T10:00:00Z",
        expiresAt = "2025-12-10T10:05:00Z",
        requiresLocation = true,
        requiresTotp = true,
        requiresBiometric = true,
        title = "Title",
        date = "",
        risk_level = "LOW"

    )

    ChallengeCard(challenge = fakeChallenge, onClick = {})
}
