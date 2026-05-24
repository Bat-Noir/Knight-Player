package com.example.knightplayer.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun KnightPlayerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    accentColor: Color = KnightRed, // 🔥 Now it accepts ANY color dynamically!
    content: @Composable () -> Unit
) {
    // 🌑 Dynamic Dark Scheme
    val darkScheme = darkColorScheme(
        primary = accentColor,
        background = OledBlack,
        surface = SurfaceDark,
        surfaceVariant = SurfaceHighlightDark,
        onBackground = TextPrimaryDark,
        onSurface = TextPrimaryDark
    )

    // ☀️ Dynamic Light Scheme
    val lightScheme = lightColorScheme(
        primary = accentColor,
        background = BackgroundLight,
        surface = SurfaceLight,
        surfaceVariant = SurfaceHighlightLight,
        onBackground = TextPrimaryLight,
        onSurface = TextPrimaryLight
    )

    val colorScheme = if (darkTheme) darkScheme else lightScheme
    val view = LocalView.current

    // 📱 EDGE-TO-EDGE IMMERSION (Now Adaptive)
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            // Transparent bars
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            // In onCreate()
            WindowCompat.setDecorFitsSystemWindows(window, false)

            // Auto-switch icon colors based on the theme!
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = KnightTypography,
        content = content
    )
}
