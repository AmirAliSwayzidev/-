package com.planora.ai.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = VioletPrimary,
    onPrimary = Color.White,
    primaryContainer = VioletDark,
    onPrimaryContainer = VioletLight,
    secondary = BlueAccent,
    onSecondary = Color.White,
    tertiary = CyanAccent,
    onTertiary = NavyDeep,
    background = NavyDeep,
    onBackground = OnSurface,
    surface = SurfaceDark,
    onSurface = OnSurface,
    surfaceVariant = SurfaceCard,
    onSurfaceVariant = OnSurfaceVariant,
    error = Error,
    onError = Color.White,
    outline = NavyLight
)

private val LightColorScheme = lightColorScheme(
    primary = VioletPrimary,
    onPrimary = Color.White,
    primaryContainer = VioletLight.copy(alpha = 0.2f),
    onPrimaryContainer = VioletDark,
    secondary = BlueAccent,
    onSecondary = Color.White,
    tertiary = CyanAccent,
    onTertiary = Color.White,
    background = SurfaceLight,
    onBackground = OnSurfaceLight,
    surface = SurfaceCardLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = OnSurfaceVariantLight,
    error = Error,
    onError = Color.White,
    outline = Color(0xFFCBD5E1)
)

@Composable
fun PlanoraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
