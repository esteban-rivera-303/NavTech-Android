package com.zanaaustralis.feature_auth.presentation.components

import android.content.Intent
import android.hardware.biometrics.BiometricManager.Authenticators.BIOMETRIC_STRONG
import android.provider.Settings
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import com.zanaaustralis.core.secure.biometric.BiometricHelper
import com.zanaaustralis.core.secure.biometric.BiometricStatus

/**
 * @author esteban.rivera01
 */

@Composable
fun BiometricRequestScreen(
    title: String = "Tu dispositivo está registrado para operar",
    onCancel: () -> Unit = {},
    onComplete: () -> Unit,
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity ?: return
    val biometricHelper = remember { BiometricHelper(context) }

    var showDialog by remember { mutableStateOf(false) }
    var hasLaunched by remember { mutableStateOf(false) }

    // 🔥 evitar múltiples ejecuciones
    LaunchedEffect(Unit) {
        if (hasLaunched) return@LaunchedEffect
        hasLaunched = true

        when (biometricHelper.getAuthStatus()) {

            BiometricStatus.Available -> {
                biometricHelper.authenticate(
                    activity = activity,
                    title = title,
                    subtitle = "",
                    onSuccess = onComplete,
                    onError = {
                        onCancel()
                    }
                )
            }

            BiometricStatus.NotEnrolled -> {
                showDialog = true
            }

            else -> {
                onCancel()
            }
        }
    }

    // 🔹 UI base
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }

    // 🔥 Dialogo cuando no hay biometría
    if (showDialog) {
        NoBiometricDialog(
            onGoToSettings = {
                val intent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                    putExtra(
                        Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                        BIOMETRIC_STRONG
                    )
                }
                context.startActivity(intent)
                showDialog = false
                onCancel()
            },
            onCancel = {
                showDialog = false
                onCancel()
            }
        )
    }
}

@Composable
fun NoBiometricDialog(
    onGoToSettings: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Biometría no configurada") },
        text = { Text("Debes configurar biometría en tu dispositivo para continuar.") },
        confirmButton = {
            TextButton(onClick = onGoToSettings) {
                Text("Ir a configuración")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("Cancelar")
            }
        }
    )
}

/*
@Composable
fun BiometryicRequestScreen(title: String = "Tu dispositivo está registrado para operar",onComplete: () -> Unit) {
    val context = LocalContext.current
    val activity = context as FragmentActivity
    val biometricHelper = remember { BiometricHelper(context) }

    if (biometricHelper.canAuthenticate()) {
        biometricHelper.authenticate(
            activity = activity,
            title = title,
            subtitle = "",
            onSuccess = onComplete,
            onError = { }
        )
    } else {
        Text("Tu dispositivo no tiene biometría configurada.")
    }
}
*/
