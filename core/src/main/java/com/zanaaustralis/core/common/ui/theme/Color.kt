package com.zanaaustralis.core.common.ui.theme

import androidx.compose.ui.graphics.Color

data class NavTechColors(

    val primary: Color,
    val primaryVariant: Color,
    val primaryDark: Color,

    val accent: Color,
    val secondary: Color,
    val secondaryVariant: Color,

    val background: Color,
    val surface: Color,

    val textPrimary: Color,
    val textSecondary: Color,
    val textDisabled: Color,

    val border: Color,
    val divider: Color,

    val success: Color,
    val warning: Color,
    val error: Color,
    val pending: Color,
    val alert: Color
)

val LightColors = NavTechColors(

    // 🔵 PRIMARY
    primary = Color(0xFF1242BD),        // Azul principal (brand)
    primaryVariant = Color(0xFF0F36A0), // Más oscuro (pressed / emphasis)
    primaryDark = Color(0xFF0A2472),    // Dark / headers profundos

    // 🔹 SECONDARY
    secondary = Color(0xFF3E8ECE),      // Azul claro
    secondaryVariant = Color(0xFF2F6FA5),

    // 🟢 ACCENT (nuevo, puedes usarlo para highlights suaves)
    accent = Color(0xFFBFD7CF),

    background = Color(0xFFFFFFFF),     // Fondo app (match Home)
    surface = Color(0xFFF0F0F0),        // Cards

    textPrimary = Color(0xFF262626),    // Neutral 100
    textSecondary = Color(0xFF656565),  // Neutral 80
    textDisabled = Color(0xFFC7C7C7),   // Neutral 70

    border = Color(0xFFE0E0E0),
    divider = Color(0xFFF0F0F0),

    success = Color(0xFF19A800),
    warning = Color(0xFFFF8D28),
    error = Color(0xFFB30000),
    pending = Color(0xFFFFBFBD),
    alert = Color(0xFFFFF8DB)
)