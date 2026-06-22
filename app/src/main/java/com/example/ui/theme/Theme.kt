package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = MintGreen,
    onPrimary = NavyBlue,
    primaryContainer = DarkGreen,
    onPrimaryContainer = Bone,
    secondary = SlateGray,
    onSecondary = White,
    background = NavyBlue,
    onBackground = Bone,
    surface = DarkGreen,
    onSurface = Bone,
    error = Terracotta,
    onError = White
)

private val LightColorScheme = lightColorScheme(
    primary = NavyBlue,
    onPrimary = White,
    primaryContainer = Color(0xFFD1E4FF),      // Polished Light Blue
    onPrimaryContainer = Color(0xFF001D36),    // Midnight Dark Text
    secondary = SlateGray,
    onSecondary = White,
    background = BackgroundBone,
    onBackground = OnSurfaceDark,
    surface = Bone,                            // White cards
    onSurface = OnSurfaceDark,                 // High-contrast charcoal text
    surfaceVariant = Color(0xFFF0F4F8),        // Cool slate active gray
    onSurfaceVariant = SlateGray,
    error = Terracotta,
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
