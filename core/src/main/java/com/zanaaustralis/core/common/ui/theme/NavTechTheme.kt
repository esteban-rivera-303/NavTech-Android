package com.zanaaustralis.core.common.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

object NavTechTheme {

    val colors: NavTechColors
        @Composable
        get() = LocalNavTechColors.current

    val spacing: NavTechSpacing
        @Composable
        get() = LocalSpacing.current

    val typography: Typography
        @Composable
        get() = MaterialTheme.typography
}

@Composable
fun NavTechTheme(
    darkIcons: Boolean = true,
    content: @Composable () -> Unit
) {

    val colors = LightColors
    val spacing = NavTechSpacing()
    val view = LocalView.current

    SideEffect {
        val window = (view.context as Activity).window
        WindowCompat.getInsetsController(window, view)
            .isAppearanceLightStatusBars = darkIcons
    }

    CompositionLocalProvider(
        LocalNavTechColors provides colors,
        LocalSpacing provides spacing
    ) {
        MaterialTheme(
            typography = NavTechTypography,
            shapes = NavTechShapes,
            content = content
        )

    }
}

@Composable
fun SystemBarsEffect(
    style: SystemBarStyle
) {
    val view = LocalView.current
    val window = (view.context as Activity).window

    SideEffect {
        val controller = WindowCompat.getInsetsController(window, view)

        when (style) {
            SystemBarStyle.LIGHT -> {
                window.statusBarColor = Color.White.toArgb()
                window.navigationBarColor = Color.White.toArgb()
                controller.isAppearanceLightStatusBars = true
                controller.isAppearanceLightNavigationBars = true
            }
            SystemBarStyle.GRAY -> {
                val color = Color(0xFFF0F0F0).toArgb()
                window.statusBarColor = color
                window.navigationBarColor = color
                controller.isAppearanceLightStatusBars = true
                controller.isAppearanceLightNavigationBars = true
            }

            SystemBarStyle.DARK -> {
                window.statusBarColor = Color.Black.toArgb()
                window.navigationBarColor = Color.Black.toArgb()
                controller.isAppearanceLightStatusBars = false
                controller.isAppearanceLightNavigationBars = false
            }

            SystemBarStyle.PRIMARY -> {
                val color = Color(0xFF1242BD).toArgb() //MaterialTheme.colorScheme.primary.toArgb()
                window.statusBarColor = color
                //window.navigationBarColor = color
                controller.isAppearanceLightStatusBars = false
                //controller.isAppearanceLightNavigationBars = false
            }
            SystemBarStyle.PRIMARY_BLACK -> {
                val color = Color(0xFF1242BD).toArgb() //MaterialTheme.colorScheme.primary.toArgb()
                window.statusBarColor = color
                //window.navigationBarColor = Color.Black.toArgb()
                controller.isAppearanceLightStatusBars = false
                //controller.isAppearanceLightNavigationBars = false
            }
        }
    }
}

enum class SystemBarStyle {
    LIGHT,      // fondo claro → iconos oscuros
    GRAY,      // fondo claro → iconos oscuros
    DARK,       // fondo oscuro → iconos claros
    PRIMARY,   // usa color primary
    PRIMARY_BLACK     // usa color primary
}