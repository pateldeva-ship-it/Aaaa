package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SleekDarkColorScheme = darkColorScheme(
    primary = ElectricVioletLight,
    onPrimary = Color(0xFF1E004A),
    primaryContainer = ElectricVioletContainer,
    onPrimaryContainer = Color(0xFFDDD6FE),

    secondary = CyberCyanLight,
    onSecondary = Color(0xFF00363F),
    secondaryContainer = CyberCyanContainer,
    onSecondaryContainer = Color(0xFFCFFAFE),

    tertiary = CoralAlertLight,
    onTertiary = Color(0xFF4C0014),
    tertiaryContainer = CoralContainer,
    onTertiaryContainer = Color(0xFFFFE4E6),

    background = DarkBg,
    onBackground = TextPrimary,

    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,

    outline = DarkBorder,
    outlineVariant = DarkSurfaceHighlight,

    error = CoralAlert,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to sleek dark mode
    dynamicColor: Boolean = false, // Preserve premium custom aesthetic
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = SleekDarkColorScheme,
        typography = Typography,
        content = content
    )
}
