package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val RBrowserColorScheme = darkColorScheme(
    primary = CyberCyan,
    onPrimary = Color(0xFF00363B),
    primaryContainer = CyberSurfaceHighlight,
    onPrimaryContainer = CyberCyan,
    secondary = CyberNeonGreen,
    onSecondary = Color(0xFF00391A),
    secondaryContainer = Color(0xFF0F3822),
    onSecondaryContainer = CyberNeonGreen,
    tertiary = CyberPurple,
    onTertiary = Color.White,
    background = CyberBg,
    onBackground = TextPrimary,
    surface = CyberSurface,
    onSurface = TextPrimary,
    surfaceVariant = CyberSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = Color(0xFF334155),
    error = CyberRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = RBrowserColorScheme,
        typography = Typography,
        content = content
    )
}
