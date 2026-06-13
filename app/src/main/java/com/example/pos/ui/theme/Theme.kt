package com.example.pos.ui.theme

import android.app.Activity
import android.os.Build
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

// DARK COLOR SCHEME
private val DarkColorScheme = darkColorScheme(
    primary          = Blue80,
    onPrimary        = Blue10,
    primaryContainer = Blue20,
    onPrimaryContainer = Blue90,
    secondary        = Teal80,
    onSecondary      = Color(0xFF003731),
    background       = Grey10,
    onBackground     = Grey90,
    surface          = Color(0xFF1C1E21),
    onSurface        = Grey90,
    error            = Red80,
)

// LIGHT COLOR SCHEME
private val LightColorScheme = lightColorScheme(
    primary            = Blue40,
    onPrimary          = Color.White,
    primaryContainer   = Blue90,
    onPrimaryContainer = Blue10,
    secondary          = Teal40,
    onSecondary        = Color.White,
    background         = Grey99,
    onBackground       = Grey10,
    surface            = Color.White,
    onSurface          = Grey10,
    surfaceVariant     = Color(0xFFF0F2FF),
    error              = Red40,
    onError            = Color.White,
)

@Composable
fun POSTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
        dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    // Tint status bar sesuai warna primary (biru POS)
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}