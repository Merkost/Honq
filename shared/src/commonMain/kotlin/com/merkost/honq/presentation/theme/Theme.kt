package com.merkost.honq.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

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
    val honqColors = honqDarkColorScheme()

    CompositionLocalProvider(
        LocalHonqColors provides honqColors
    ) {
        MaterialTheme(
            colorScheme = DarkColorScheme,
            typography = HonqTypography,
            content = content
        )
    }
}

object HonqTheme {
    val colors: HonqColorScheme
        @Composable
        @ReadOnlyComposable
        get() = LocalHonqColors.current
}
