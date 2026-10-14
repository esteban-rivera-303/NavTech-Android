package com.zanaaustralis.argos.launch

/**
 * @author esteban.rivera01
 */
data class LaunchUiState(
    val isLoading: Boolean = true,
    val message: String = "Iniciando...",
    val success: Boolean = false,
    val goToHome: Boolean = false,
    val step: LaunchStep = LaunchStep.Splash,
    val goToOnboarding: Boolean = false
)

enum class LaunchStep {
    Splash,
    Intro,
    SecurityBlocked
}