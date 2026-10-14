package com.zanaaustralis.feature_auth.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zanaaustralis.core.common.ui.component.NavTechButton
import com.zanaaustralis.core.common.ui.component.NavTechInput
import com.zanaaustralis.core.common.ui.theme.NavTechTheme
import com.zanaaustralis.core.common.ui.theme.NavTechTheme.colors
import com.zanaaustralis.feature_auth.R
import com.zanaaustralis.feature_auth.presentation.enrollment.EnrollmentDataSummary

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {

        // 🔵 HEADER (iOS STYLE)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .background(colors.primary)
        ) {

            Spacer(Modifier.height(48.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {


                if (onBack != null) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBack,
                            tint = Color.White,
                            contentDescription = "Volver"
                        )
                    }
                } else {
                   // Spacer(Modifier.width(60.dp))
                }

                Spacer(Modifier.weight(1f))

                Text(
                    text = title,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium
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
            if (step != 0 && totalSteps != 0) {
                LinearProgressIndicator(
                    progress = step / totalSteps.toFloat(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = colors.surface,
                    trackColor = colors.surface.copy(alpha = 0.3f)
                )
            }
        }

        // 🟡 BODY
        Box(
            modifier = Modifier
                .background(colors.background)
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
@Composable
fun BaseScreen2(
    title: String,
    subtitle: String,
    iconRes: Int,
    body: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .background(Color.White)
            //.windowInsetsPadding(WindowInsets.safeDrawing)
            .fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
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
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = title,
                        color = Color.White
                    )
                    Spacer(Modifier.weight(1f))
                }

                Spacer(Modifier.height(8.dp))

            }

            Spacer(Modifier.height(24.dp))

            Column(
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .background(Color(0xFFEAEAFF), shape = CircleShape)
                        .shadow(6.dp, shape = CircleShape)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = null,
                        tint = NavTechTheme.colors.primary,
                        modifier = Modifier.size(58.dp)
                    )
                }

                Spacer(Modifier.height(26.dp))

                Text(
                    title,
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(10.dp))

                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(Modifier.height(28.dp))

                body()
            }
        }
    }
}


/**
 * PREVIEW
 */
@Preview(
    name = "BaseScreen2 - Default",
    showBackground = true,
    device = "spec:width=411dp,height=891dp"
)
@Composable
fun PreviewBaseScreen2() {
    NavTechTheme {

        BaseScreen2(
            title = "Registro de usuario",
            subtitle = "Necesitamos algunos datos para continuar",
            iconRes = R.drawable.baseline_verified_user_24
        ) {

            Column(
                modifier = Modifier.fillMaxWidth()
            ) {

                NavTechInput(
                    value = "",
                    onValueChange = {},
                    placeholder = "RUT"
                )

                Spacer(Modifier.height(12.dp))

                NavTechInput(
                    value = "",
                    onValueChange = {},
                    placeholder = "Correo electrónico"
                )

                Spacer(Modifier.height(12.dp))

                NavTechInput(
                    value = "",
                    onValueChange = {},
                    placeholder = "Número de teléfono"
                )

                Spacer(Modifier.height(20.dp))

                NavTechButton(
                    text = "Continuar",
                    onClick = {},
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
@Preview
@Composable
fun PreviewNavStepScreen() {
    NavTechTheme {

        NavStepScreen(
            title = "Enrolamiento dispositivo",
            step = 4,
            totalSteps = 5,
            onBack = {}
            ,
            bottomContent = {
                NavTechButton(
                    text = "Continuar",
                    onClick = {},
                    modifier = Modifier.fillMaxWidth()
                )
            }
        ) {

            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Icon(
                    painter = painterResource(com.zanaaustralis.core.R.drawable.logo),
                    contentDescription = null,
                    tint = NavTechTheme.colors.primary,
                    modifier = Modifier.size(164.dp)
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "Enrolamiento",
                    style = NavTechTheme.typography.headlineMedium
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Registraremos este dispositivo para firmar desafíos.",
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(24.dp))

                EnrollmentDataSummary(
                    rut = "12.345.678-9",
                    email = "usuario@mail.com",
                    phone = "+56999987766"
                )
            }
        }
    }
}
