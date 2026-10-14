package com.zanaaustralis.feature_auth.presentation.verify

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zanaaustralis.core.common.ui.theme.NavTechTheme
import com.zanaaustralis.feature_auth.R
import com.zanaaustralis.feature_auth.presentation.verification.VerifyViewModel

/**
 * @author esteban.rivera01
 */


@Composable
fun VerifyScreen(
    verificationType: VerificationType,
    targetValue: String,
    onNext: () -> Unit,
    viewModel: VerifyViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    VerifyContent(
        state = state,
        verificationType = verificationType,
        targetValue = targetValue,
        onVerify = { code ->
            viewModel.verifyCode(
                target = targetValue,
                code = code,
                verificationType = verificationType
            )
        },
        onNext = onNext
    )
}

@Composable
fun VerifyContent(state: VerifyUiState,
         verificationType: VerificationType,
         targetValue: String,
         onVerify: (String) -> Unit,
         onNext: () -> Unit){
    val shimmerAlpha by rememberInfiniteTransition().animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(Modifier.height(24.dp))

            // Avatar / Icon
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .background(Color(0xFFEAEAFF), shape = CircleShape)
                    .alpha(if (state.isLoading) shimmerAlpha else 1f),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_verified_user_24),
                    contentDescription = null,
                    tint = NavTechTheme.colors.primary,
                    modifier = Modifier.size(60.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                "Argos 2FA",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
            )

            Spacer(Modifier.height(8.dp))

            Text(
                "Este paso nos permite comprobar que la cuenta pertenece a ti",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))

            // Body Section
            Body(state, verificationType, targetValue, onVerify, onNext)

        }
    }
}

@Composable
fun Body(state: VerifyUiState,
         verificationType: VerificationType,
         targetValue: String,
         onVerify: (String) -> Unit,
         onNext: () -> Unit){

    var codeInput by remember { mutableStateOf("") }

    val targetLabel =
        if (verificationType == VerificationType.EMAIL) "correo electrónico"
        else "número de teléfono"

    val labelText =
        if (verificationType == VerificationType.EMAIL)
            "Código enviado a tu correo"
        else
            "Código enviado a tu teléfono"

    val purpleMain = Color(0xFF6C4BFF)
    val purpleDark = Color(0xFF5739D9)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {

            Text(
                text = "Hemos enviado un código a tu $targetLabel:",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = targetValue,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = purpleDark
            )

            Spacer(Modifier.height(14.dp))

            OutlinedTextField(
                value = codeInput,
                onValueChange = { codeInput = it },
                label = { Text(labelText) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(18.dp))

            Button(
                onClick = { onVerify(codeInput) },
                modifier = Modifier.fillMaxWidth(),
                enabled = codeInput.isNotBlank() && !state.isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = purpleMain,
                    contentColor = Color.White
                )
            ) {
                Text(if (state.isLoading) "Verificando..." else "Validar código")
            }

            Spacer(Modifier.height(16.dp))

            when {
                state.isLoading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = purpleMain
                )

                state.message != null -> Text(
                    text = state.message!!,
                    color = if (state.verified) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }

    if (state.verified) {
        LaunchedEffect(Unit) { onNext() }
    }
}


// PREVIEWS

@Preview(showBackground = true)
@Composable
fun Preview_VerifyEmailStyled() {
    NavTechTheme {
        VerifyContent(
            state = VerifyUiState(),
            verificationType = VerificationType.EMAIL,
            targetValue = "usuario@mail.com",
            onVerify = {},
            onNext = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun Preview_VerifyPhoneStyled() {
    NavTechTheme {
        VerifyContent(
            state = VerifyUiState(),
            verificationType = VerificationType.PHONE,
            targetValue = "+56999887766",
            onVerify = {},
            onNext = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun Preview_VerifySuccessStyled() {
    NavTechTheme {
        VerifyContent(
            state = VerifyUiState(
                verified = true,
                message = "Código validado correctamente ✓"
            ),
            verificationType = VerificationType.PHONE,
            targetValue = "+56944556677",
            onVerify = {},
            onNext = {}
        )
    }
}
