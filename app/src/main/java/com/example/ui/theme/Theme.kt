package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = KrishiGreenLight,
    onPrimary = Color.White,
    primaryContainer = KrishiGreenSecondary,
    onPrimaryContainer = KrishiGreenContainer,
    secondary = KrishiOrangeLight,
    onSecondary = Color.Black,
    secondaryContainer = KrishiOrangeOnContainer,
    onSecondaryContainer = KrishiOrangeContainer,
    background = Color(0xFF121512),
    surface = Color(0xFF1A1D1A),
    surfaceVariant = Color(0xFF262B25),
    onBackground = Color(0xFFE2E4DE),
    onSurface = Color(0xFFE2E4DE),
    outline = Color(0xFF8C9388)
)

private val LightColorScheme = lightColorScheme(
    primary = KrishiGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = KrishiGreenContainer,
    onPrimaryContainer = KrishiGreenOnContainer,
    secondary = KrishiGreenSecondary,
    onSecondary = Color.White,
    secondaryContainer = KrishiSurfaceVariant,
    onSecondaryContainer = KrishiGreenSecondary,
    tertiary = KrishiOrange,
    onTertiary = Color.White,
    tertiaryContainer = KrishiOrangeContainer,
    onTertiaryContainer = KrishiOrangeOnContainer,
    background = KrishiBackground,
    surface = KrishiSurface,
    surfaceVariant = KrishiSurfaceVariant,
    onBackground = KrishiTextPrimary,
    onSurface = KrishiTextPrimary,
    onSurfaceVariant = KrishiTextSecondary,
    outline = KrishiOutline
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Enforce crisp, pleasant agricultural light theme to avoid unreadable dark backgrounds
    dynamicColor: Boolean = false, // Keep consistent agricultural branding
    content: @Composable () -> Unit,
) {
    MaterialTheme(colorScheme = LightColorScheme, typography = Typography, content = content)
}

