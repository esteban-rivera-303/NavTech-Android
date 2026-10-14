package com.zanaaustralis.argos.launch

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.zanaaustralis.core.NavRoute
import com.zanaaustralis.core.common.ui.component.NavTechButton
import com.zanaaustralis.core.common.ui.theme.NavTechTheme
import com.zanaaustralis.feature_auth.presentation.components.BiometricRequestScreen

/**
 * @author esteban.rivera01
 */

@Composable
fun LaunchScreen(
    navController: NavHostController,
    viewModel: LaunchViewModel = hiltViewModel()
) {

    val state by viewModel.uiState.collectAsState()
    var showBiometric by remember { mutableStateOf(false) }

    val colors = NavTechTheme.colors


    // Navegación si ya hay sesión
    LaunchedEffect(state.goToHome) {
        if (state.goToHome) {
            showBiometric = true
        }
    }

    if (showBiometric) {
        BiometricRequestScreen("Acceso Seguro") {
            navController.navigate(NavRoute.Home.route) {
                popUpTo(0)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.primary)
    ) {

        AnimatedContent(
            targetState = state.step,
            transitionSpec = {
                fadeIn(tween(500)) togetherWith fadeOut(tween(300))
            }
        ) { step ->

            when (step) {

                LaunchStep.Splash -> SplashLoading()

                LaunchStep.Intro -> SplashIntro(
                    onStartClick = {
                        navController.navigate(NavRoute.UserStatus.route){
                            popUpTo(0) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                )

                LaunchStep.SecurityBlocked -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Acceso bloqueado por razones de seguridad.",
                            color = Color.White,
                            style = MaterialTheme.typography.headlineMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SplashLoading() {

    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Image(
            painter = painterResource(com.zanaaustralis.argos.R.drawable.logo_full),
            contentDescription = "NavTech",
            modifier = Modifier.size(180.dp)
        )

        Spacer(Modifier.height(32.dp))

        CircularProgressIndicator(
            color = Color.White,
            strokeWidth = 3.dp
        )
    }
}

@Composable
fun SplashIntro(
    onStartClick: () -> Unit
) {

    val spacing = NavTechTheme.spacing

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        NavTechTheme.colors.primary,
                        NavTechTheme.colors.primaryDark
                    )
                )
            )
            .padding(spacing.lg),

        verticalArrangement = Arrangement.Bottom
    ) {

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {

            Image(
                painter = painterResource(com.zanaaustralis.argos.R.drawable.logo),
                contentDescription = "NavTech Logo",
                modifier = Modifier.size(200.dp)
            )
        }

        Spacer(Modifier.height(spacing.xxl))

        Text(
            text = "Seguridad para\ntus desafíos.",
            style = NavTechTheme.typography.headlineLarge,
            color = NavTechTheme.colors.surface
        )

        Spacer(Modifier.height(spacing.sm))

        Text(
            text = "Aprueba de forma segura tus desafíos.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.8f)
        )

        Spacer(Modifier.height(spacing.xl))

        NavTechButton(
            text = "Comenzar",
            onClick = onStartClick
        )

        Spacer(Modifier.height(40.dp))
    }
}

// Prview
/*@Preview(
    name = "Splash Loading",
    showBackground = true,
    device = "spec:width=411dp,height=891dp"
)
@Composable
fun PreviewSplashLoading() {
    NavTechTheme {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(NavTechTheme.colors.primary)
        ) {
            SplashLoading()
        }

    }
}

@Preview(
    name = "Splash Intro",
    showBackground = true,
    device = "spec:width=411dp,height=891dp"
)
@Composable
fun PreviewSplashIntro() {

    NavTechTheme {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(NavTechTheme.colors.primary)
        ) {

            SplashIntro(
                onStartClick = {}
            )
        }
    }
}

@Preview(
    name = "Splash Intro Gradient",
    showBackground = true
)
@Composable
fun PreviewSplashIntroGradient() {

    NavTechTheme {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF1242BD),
                            Color(0xFF0C2E82)
                        )
                    )
                )
        ) {

            SplashIntro(
                onStartClick = {}
            )
        }
    }
}

@Preview
@Composable
fun PreviewLaunchSplashState() {

    NavTechTheme {
        SplashLoading()
    }
}*/
/*
@Composable
fun LaunchScreen(
    navController: NavHostController,
    viewModel: LaunchViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    // Navegación automática
    LaunchedEffect(state.goToHome, state.goToOnboarding) {
        when {
            state.goToHome -> {
                delay(500)
                navController.navigate(NavRoute.Home.route) {
                    popUpTo(0)
                }
            }

            state.goToOnboarding -> {
                delay(500)
                navController.navigate(NavRoute.UserStatus.route) {
                    popUpTo(0)
                }
            }
        }
    }

    // UI del Splash/Launch Screen
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo central
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(Color(0xFFEAEAFF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Image(
                    painter = painterResource(id = R.drawable.baseline_verified_user_24),
                    contentDescription = "Logo",
                    modifier = Modifier.size(64.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Argos 2FA",
                fontWeight = FontWeight.Bold,
                color = NavTechTheme.colors.primary,
                fontSize = 22.sp
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = state.message,
                textAlign = TextAlign.Center,
                color = Color.Gray,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(Modifier.height(32.dp))

            CircularProgressIndicator(
                color = NavTechTheme.colors.primary,
                strokeWidth = 3.dp,
                modifier = Modifier.scale(1.1f)
            )
        }
    }
}
*/
