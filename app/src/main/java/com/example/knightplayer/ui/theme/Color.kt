package com.example.knightplayer.ui.theme

import androidx.compose.ui.graphics.Color

// ==========================================
// 🌑 DARK MODE COLORS (OLED Optimized)
// ==========================================
val OledBlack = Color(0xFF000000)        // Pure black for battery saving
val SurfaceDark = Color(0xFF0A0A0A)      // Slightly elevated elements
val SurfaceGlassDark = Color(0x99111111) // Translucent dark
val SurfaceHighlightDark = Color(0xFF1A1A1A) // Pressed states / cards
val TextPrimaryDark = Color(0xFFFFFFFF)  // High contrast white
val TextSecondaryDark = Color(0xFF888888) // Dimmed readouts

// ==========================================
// ☀️ LIGHT MODE COLORS
// ==========================================
val BackgroundLight = Color(0xFFF5F5F5)  // Clean off-white
val SurfaceLight = Color(0xFFFFFFFF)     // Pure white cards
val SurfaceGlassLight = Color(0x99FFFFFF)// Translucent light
val SurfaceHighlightLight = Color(0xFFEBEBEB) // Pressed states
val TextPrimaryLight = Color(0xFF111111) // Deep charcoal for readability
val TextSecondaryLight = Color(0xFF666666)// Dimmed gray

// ==========================================
// 🎨 DYNAMIC ACCENT COLORS
// ==========================================
val KnightRed = Color(0xFFFF3B30)    // Default Blade Runner Neon Red
val CyberBlue = Color(0xFF00C6FF)    // Tron Legacy Blue
val MatrixGreen = Color(0xFF00FF87)  // Hacker Green
val RoyalPurple = Color(0xFF9D4EDD)  // Deep Violet
val NeonPink = Color(0xFFFF007F)     // Synthwave Pink
val SunsetOrange = Color(0xFFFF7A00) // Warm Orange

// Enum to power the Settings UI automatically later
enum class AccentColorOption(val colorName: String, val colorValue: Color) {
    RED("Knight Red", KnightRed),
    BLUE("Cyber Blue", CyberBlue),
    GREEN("Matrix Green", MatrixGreen),
    PURPLE("Royal Purple", RoyalPurple),
    PINK("Neon Pink", NeonPink),
    ORANGE("Sunset Orange", SunsetOrange)
}

// Enum for Dark/Light mode selection
enum class ThemeModeOption {
    SYSTEM, LIGHT, DARK
}
