package com.merkost.honq.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = HonqColors.Amber,
    onPrimary = HonqColors.Background,
    secondary = HonqColors.AmberDark,
    onSecondary = HonqColors.TextPrimary,
    tertiary = HonqColors.Correct,
    background = HonqColors.Background,
    onBackground = HonqColors.TextPrimary,
    surface = HonqColors.Surface,
    onSurface = HonqColors.TextPrimary,
    surfaceVariant = HonqColors.SurfaceVariant,
    onSurfaceVariant = HonqColors.TextSecondary,
    outline = HonqColors.Border,
    error = HonqColors.Incorrect,
    onError = HonqColors.TextPrimary
)

@Composable
fun HonqTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = HonqTypography,
        content = content
    )
}
