package com.zanaaustralis.feature_auth.presentation.totp

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zanaaustralis.core.common.ui.component.NavTechButton
import com.zanaaustralis.core.common.ui.theme.NavTechTheme
import java.time.format.TextStyle

/**
 * @author esteban.rivera01
 */

@Composable
fun TotpCardView(
    code: String,
    secondsRemaining: Int,
    onCopy: (String) -> Unit,
    modifier: Modifier = Modifier
) {

    val progress = secondsRemaining / 30f

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(300),
        label = "progress"
    )

    val animatedColor by animateColorAsState(
        targetValue = when {
            secondsRemaining <= 5 -> Color.Red
            secondsRemaining <= 10 -> Color(0xFFFF9800)
            else -> NavTechTheme.colors.primary
        },
        label = "color"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(NavTechTheme.colors.surface)
                .padding(vertical = 12.dp, horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {

                Text(
                    text = formatCode(code),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 6.sp,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.width(36.dp))

                CircularProgressIndicator(
                    progress = animatedProgress,
                    color = animatedColor,
                    strokeWidth = 6.dp,
                    strokeCap = StrokeCap.Round,
                    modifier = Modifier
                        .size(48.dp)
                )
            }
            Spacer(Modifier.height(12.dp))

            OutlinedButton(

                onClick = { onCopy(code) },
                shape = RoundedCornerShape(50),
                border = BorderStroke(
                    width = 1.dp,
                    color = NavTechTheme.colors.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.ContentCopy,
                    tint = NavTechTheme.colors.primary,
                    contentDescription = null
                )
                Spacer(Modifier.width(8.dp))
                Text("Copiar código" ,
                        style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = NavTechTheme.colors.primary)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TotpInputBottomSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    if (!visible) return

    var code by remember { mutableStateOf("") }

    val isValid = code.length == 6

    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(NavTechTheme.colors.background)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Ingresa código de verificación",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(16.dp))

            val colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NavTechTheme.colors.primary,
                cursorColor = NavTechTheme.colors.primary
            )

            // INPUT
            OutlinedTextField(
                value = code,
                onValueChange = {
                    if (it.length <= 6 && it.all { c -> c.isDigit() }) {
                        code = it
                    }
                },
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(
                    textAlign = TextAlign.Center,
                    fontSize = 28.sp,
                    letterSpacing = 8.sp
                ),
                colors = colors,
                placeholder = {
                    Text("123 456", textAlign = TextAlign.Center)
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            NavTechButton(
                text = "Confirmar",
                enabled = isValid,
                onClick = { onConfirm(code) },
                modifier = Modifier.width(200.dp)
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}

fun formatCode(code: String): String {
    return code.chunked(3).joinToString(" ")
}