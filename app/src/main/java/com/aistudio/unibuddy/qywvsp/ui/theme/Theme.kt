package com.aistudio.unibuddy.qywvsp.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = ProBlue,
    onPrimary = NavyBlue,
    primaryContainer = DeepBlue,
    onPrimaryContainer = Color.White,
    secondary = DarkGray,
    onSecondary = Color.White,
    background = NavyBlue,
    onBackground = Color.White,
    surface = DeepBlue,
    onSurface = Color.White,
    error = ProRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = NavyBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD1E4FF),      // Polished Light Blue
    onPrimaryContainer = NavyBlue,
    secondary = DarkGray,
    onSecondary = Color.White,
    background = BackgroundGray,
    onBackground = OnSurfaceDark,
    surface = Color.White,                            // White cards
    onSurface = OnSurfaceDark,                 // High-contrast charcoal text
    surfaceVariant = Color(0xFFF0F4F8),        // Cool slate active gray
    onSurfaceVariant = DarkGray,
    error = ProRed,
    onError = Color.White
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
