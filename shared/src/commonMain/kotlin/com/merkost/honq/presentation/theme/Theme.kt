package com.merkost.honq.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import com.merkost.honq.data.local.ThemePreferences
import org.koin.compose.koinInject

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

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFCC8800),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFF996600),
    onSecondary = Color(0xFF1C1C1E),
    tertiary = Color(0xFF28A745),
    background = Color(0xFFF2F2F7),
    onBackground = Color(0xFF1C1C1E),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1C1C1E),
    surfaceVariant = Color(0xFFE5E5EA),
    onSurfaceVariant = Color(0xFF636366),
    outline = Color(0xFFD1D1D6),
    error = Color(0xFFDC3545),
    onError = Color(0xFFFFFFFF)
)

@Composable
fun HonqTheme(content: @Composable () -> Unit) {
    val themePreferences = koinInject<ThemePreferences>()
    val isDarkTheme by themePreferences.isDarkTheme.collectAsState()

    val honqColors = if (isDarkTheme) honqDarkColorScheme() else honqLightColorScheme()
    val materialScheme = if (isDarkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(
        LocalHonqColors provides honqColors
    ) {
        MaterialTheme(
            colorScheme = materialScheme,
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
