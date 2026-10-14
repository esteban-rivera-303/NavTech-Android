package com.zanaaustralis.feature_kyc.presentation

import android.Manifest
import android.os.Build
import android.util.Log
import android.webkit.CookieManager
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.zanaaustralis.core.common.ui.component.NavTechButton
import com.zanaaustralis.core.common.ui.theme.NavTechTheme


/**
 * @author esteban.rivera01
 */

@Composable
fun KyCScreen(
    rut: String,
    onSuccess: () -> Unit,
    viewModel: KyCViewModel = hiltViewModel()
) {

    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.initiate(rut)
    }

    NavStepScreen(
        title = "Validación identidad",
        step = 3,
        totalSteps = 3,
        bottomContent = {
            if (state.approved == true) {
                NavTechButton(
                    text = "Continuar",
                    onClick = onSuccess,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 64.dp)
                )
            }
        }
    ) {

        Box(
            modifier = Modifier
                .align(Alignment.Center)

        ) {

            when {

                // 🔵 LOADING
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                state.error != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Text(
                            text = "No pudimos iniciar la validación",
                            color = NavTechTheme.colors.error,
                            style = NavTechTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(16.dp))

                        NavTechButton(
                            text = "Reintentar",
                            onClick = { viewModel.initiate(rut) }
                        )
                    }
                }

                // 🟡 WEBVIEW (FLUJO ACTIVO)
                state.kycUrl != null && state.approved == null && state.isInReview == null -> {

                    KycWebView(
                        url = state.kycUrl!!,
                        onCompleted = {
                            viewModel.startPolling(rut)
                        },
                    )
                }

                state.approved == false -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Text(
                            text = "No pudimos validar tu identidad." +
                                    "Intenta nuevamente.",
                            color = NavTechTheme.colors.error,
                            style = NavTechTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(12.dp))

                        state.lastSubmission?.rejection_reason?.let {
                            Text(
                                text = it,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(Modifier.height(20.dp))

                        NavTechButton(
                            text = "Reintentar",
                            onClick = { viewModel.initiate(rut) }
                        )
                    }
                }

                state.approved == true -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Icon(
                            painter = painterResource(com.zanaaustralis.core.R.drawable.logo_navtech_color),
                            contentDescription = null,
                            modifier = Modifier.size(150.dp)
                        )

                        Spacer(Modifier.height(16.dp))

                        Text(
                            text = "Identidad validada con éxito",
                            style = NavTechTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                state.isInReview != null && state.isInReview == true -> {
                    KycInReviewScreen(
                        onRetry = {
                            viewModel.startPolling(rut)
                        })
                }
            }
        }
    }
}

@Composable
fun KycWebView(
    url: String,
    onCompleted: () -> Unit
) {
    RequestPermissions()

    AndroidView(
        factory = { context ->
            WebView(context).apply {
                // Habilitar JavaScript
                settings.javaScriptEnabled = true

                // Permitir reproducción inline de video
                settings.mediaPlaybackRequiresUserGesture = false

                // Asegurar compatibilidad de UI (a veces Didit requiere esto)
                settings.domStorageEnabled = true

                // Solo HTTPS: no se permite cargar recursos HTTP dentro de páginas HTTPS
                settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW

                // Activar cookies (Didit las usa para validar sesión)
                CookieManager.getInstance().setAcceptCookie(true)
                CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
                CookieManager.getInstance().flush()

                // Setear User Agent recomendado (evita rechazos de sesión)
                settings.userAgentString =
                    "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"

                // Para manejar reproducción multimedia
                webChromeClient = object : WebChromeClient() {
                    override fun onPermissionRequest(request: PermissionRequest?) {
                        request ?: return
                        // Cámara/micrófono solo para orígenes HTTPS de Didit
                        val granted = KycWebPolicy.grantablePermissions(
                            origin = request.origin?.toString(),
                            requested = request.resources
                        )
                        if (granted.isEmpty()) request.deny() else request.grant(granted)
                    }
                }
                settings.cacheMode = WebSettings.LOAD_NO_CACHE
                settings.allowFileAccess = false
                settings.allowContentAccess = false
                if (KycWebPolicy.isTrustedUrl(url)) loadUrl(url)
            }
        },
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding(),
        update = { web ->
            web.webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {

                    val newUrl = request?.url.toString()
                    if (isKycFinishedUrl(newUrl)) {
                        onCompleted()
                        return true
                    }

                    // Bloquea la navegación fuera de los dominios de Didit
                    return !KycWebPolicy.isTrustedUrl(newUrl)
                }
                override fun onPageFinished(view: WebView?, url: String?) {
                    // Detect the Didit final callback page if needed
                    if (url?.contains("completed") == true) {
                        onCompleted()
                    }
                }
            }
        }
    )
}

@Composable
fun LoadingState(
    label: String = "Procesando verificación..."
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        CircularProgressIndicator()

        Spacer(Modifier.height(12.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ErrorState(
    message: String,
    onRetry: (() -> Unit)? = null
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "Error",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(46.dp)
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "Error en la verificación",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.error
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        if (onRetry != null) {
            Spacer(Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text("Reintentar")
            }
        }
    }
}

@Composable
fun RejectState(
    rejectionReason: String?,
    onRetry: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Icon(
            imageVector = Icons.Default.Clear,
            contentDescription = "Rechazado",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(52.dp)
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "Verificación Rechazada",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.error
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = rejectionReason ?: "Hemos detectado inconsistencias en tu verificación. Por favor revisa tus documentos y vuelve a intentarlo.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp),
            color = MaterialTheme.colorScheme.onSurface
        )

        if (onRetry != null) {
            Spacer(Modifier.height(20.dp))
            Button(onClick = onRetry) {
                Text("Intentar nuevamente")
            }
        }
    }
}

@Composable
fun KycInReviewScreen(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F6FA))
            .statusBarsPadding()
    ) {

        // 🔵 CONTENIDO CENTRAL
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // LOGO
            Image(
                painter = painterResource(com.zanaaustralis.core.R.drawable.logo_navtech_color),
                contentDescription = null,
                modifier = Modifier.height(28.dp)
            )

            Spacer(Modifier.height(32.dp))

            Text(
                text = "Estamos validando tu identidad",
                style = NavTechTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2C2C2C),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Este proceso puede tardar unos minutos.\nTe avisaremos cuando esté listo.",
                style = NavTechTheme.typography.bodyMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(32.dp))

            // 🔄 INDICADOR
            CircularProgressIndicator(
                strokeWidth = 3.dp,
                color = NavTechTheme.colors.primary
            )
        }

        // 🔵 BOTÓN ABAJO
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .navigationBarsPadding()
        ) {

            NavTechButton(
                text = "Revisar estado",
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestPermissions() {

    val cameraPermission =
        Manifest.permission.CAMERA

    val readImagesPermission =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_IMAGES
        else
            Manifest.permission.READ_EXTERNAL_STORAGE

    val multiplePermissionsState = rememberMultiplePermissionsState(
        listOf(
            cameraPermission,
            readImagesPermission
        )
    )

    val allGranted = multiplePermissionsState.permissions.all {
        it.status.isGranted
    }

    LaunchedEffect(allGranted) {
        if (!allGranted) {
            multiplePermissionsState.launchMultiplePermissionRequest()
        }
    }

}

fun isKycFinishedUrl(url: String): Boolean {
    return url.contains("callback", ignoreCase = true) ||
            url.contains("result", ignoreCase = true) ||
            url.contains("status", ignoreCase = true) ||
            url.contains("approved", ignoreCase = true)
}

/**
 * Preview
 * */

@Preview(showBackground = true)
@Composable
fun PreviewLoadingState() {
    LoadingState()
}

@Preview(showBackground = true)
@Composable
fun PreviewErrorState() {
    ErrorState(message = "No se pudo iniciar la verificación con Didit.")
}

@Preview(showBackground = true)
@Composable
fun PreviewRejectState() {
    RejectState(
        rejectionReason = "Documento ilegible. Asegúrate de que la imagen sea nítida."
    )
}




