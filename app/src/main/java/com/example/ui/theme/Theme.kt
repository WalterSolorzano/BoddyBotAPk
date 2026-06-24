package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = ProBlue,
    onPrimary = NavyBlue,
    primaryContainer = DeepBlue,
    onPrimaryContainer = White,
    secondary = DarkGray,
    onSecondary = White,
    background = NavyBlue,
    onBackground = White,
    surface = DeepBlue,
    onSurface = White,
    error = ProRed,
    onError = White
)

private val LightColorScheme = lightColorScheme(
    primary = NavyBlue,
    onPrimary = White,
    primaryContainer = Color(0xFFD1E4FF),      // Polished Light Blue
    onPrimaryContainer = NavyBlue,
    secondary = DarkGray,
    onSecondary = White,
    background = BackgroundGray,
    onBackground = OnSurfaceDark,
    surface = White,                            // White cards
    onSurface = OnSurfaceDark,                 // High-contrast charcoal text
    surfaceVariant = Color(0xFFF0F4F8),        // Cool slate active gray
    onSurfaceVariant = DarkGray,
    error = ProRed,
    onError = White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Default to clean light/tactile paper theme
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
