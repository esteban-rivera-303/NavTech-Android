package com.zanaaustralis.feature_kyc.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.zanaaustralis.core.common.ui.theme.NavTechTheme

/**
 * @author esteban.rivera01
 */

@Composable
fun NavStepScreen(
    title: String,
    step: Int,
    totalSteps: Int,
    onBack: (() -> Unit)? = null,
    bottomContent: @Composable () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {

    val colors = NavTechTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .background(colors.background)
    ) {

        // 🔵 HEADER (iOS STYLE)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.primary)
        ) {

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                // ← ATRÁS
                if (onBack != null) {
                    Text(
                        text = "← Atrás",
                        color = Color.White,
                        modifier = Modifier.clickable { onBack() }
                    )
                } else {
                    Spacer(Modifier.width(60.dp))
                }

                Spacer(Modifier.weight(1f))

                Text(
                    text = title,
                    color = Color.White
                )

                Spacer(Modifier.weight(1f))

                if (step != 0 && totalSteps != 0) {
                    Text(
                        text = "$step/$totalSteps",
                        color = Color.White
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // 🔥 PROGRESS BAR
            LinearProgressIndicator(
                progress = step / totalSteps.toFloat(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.3f)
            )
        }

        // 🟡 BODY
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            content()
        }

        // 🔵 BOTTOM CTA
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            bottomContent()
        }
    }
}