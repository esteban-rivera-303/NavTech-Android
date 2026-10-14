package com.zanaaustralis.feature_auth.presentation.pin.recovery

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.zanaaustralis.core.NavRoute
import kotlinx.coroutines.delay
import java.time.Instant
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.zanaaustralis.core.common.ui.theme.NavTechTheme


/**
 * @author esteban.rivera01
 */

@Composable
fun RestorePinScreen(
    navController: NavController,
    viewModel: PinRecoveryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkValidationStatus()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(state.validationId) {
        while (state.validationId != null && !state.success) {
            delay(5_000)
            viewModel.checkValidationStatus()
        }
    }
    // 🔁 Navegación al éxito
    LaunchedEffect(state.success) {
        if (state.success) {
            navController.navigate(NavRoute.CreatePIN.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    LaunchedEffect(state.validationUrl) {
        state.validationUrl?.let { url ->
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        }
    }

    val remainingSeconds = rememberCountdownSeconds(state.expiresAt)



    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavTechTheme.colors.background)
            //.windowInsetsPadding(WindowInsets.systemBars)
            .padding(horizontal = 24.dp),

    ) {
        // Flecha superior
        IconButton(
            onClick = { navController.popBackStack() },
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
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // 🔐 Preview / icono
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = NavTechTheme.colors.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Verificación de identidad",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Necesitamos confirmar tu identidad antes de permitir la restauración del PIN.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ⏱ Countdown visible
            if (remainingSeconds > 0 && state.validationId != null) {
                Text(
                    text = "Tiempo restante: ${formatSeconds(remainingSeconds)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

            if (state.remainingAttempts > 0) {
                Text(
                    text = "Intentos restantes: ${state.remainingAttempts}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = "Has agotado los intentos disponibles",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ⚠️ Error
            state.error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 🔘 Acción principal
            if (state.loading) {
                CircularProgressIndicator()
            } else {
                val canStartValidation =
                    state.validationId == null ||
                            (state.remainingAttempts > 0 && remainingSeconds > 0)


                Button(
                    onClick = { viewModel.startRecovery() },
                    enabled = canStartValidation,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NavTechTheme.colors.primary,
                        disabledContainerColor = Color(0xFFB7B3FF),
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        if (state.validationId == null)
                            "Iniciar verificación"
                        else
                            "Reintentar verificación"
                    )
                }
            }
        }
    }
}

@Composable
fun rememberCountdownSeconds(expiresAt: String?): Long {
    var remainingSeconds by remember(expiresAt) { mutableLongStateOf(0L) }

    LaunchedEffect(expiresAt) {
        if (expiresAt.isNullOrBlank()) {
            remainingSeconds = 0L
            return@LaunchedEffect
        }

        val expiryMillis = Instant.parse(expiresAt).toEpochMilli()

        while (true) {
            val diff = (expiryMillis - System.currentTimeMillis()) / 1000
            remainingSeconds = diff.coerceAtLeast(0)

            if (diff <= 0) break
            delay(1_000)
        }
    }

    return remainingSeconds
}

fun formatSeconds(seconds: Long): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%02d:%02d".format(m, s)
}
