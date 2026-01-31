package com.merkost.honq.presentation.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class HonqColorScheme(
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val border: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,

    val primary: Color,
    val primaryVariant: Color,
    val onPrimary: Color,
    val primarySurface: Color,

    val correct: Color,
    val correctSurface: Color,
    val incorrect: Color,
    val incorrectSurface: Color,
    val warning: Color,

    val loadingIndicator: Color,
    val progressTrack: Color,
    val progressIndicator: Color,

    val imagePlaceholder: Color,
    val imagePlaceholderIcon: Color
)

internal val LocalHonqColors = staticCompositionLocalOf {
    honqDarkColorScheme()
}

fun honqDarkColorScheme(): HonqColorScheme {
    val background = Color(0xFF1C1C1E)
    val surface = Color(0xFF2C2C2E)
    val surfaceVariant = Color(0xFF3A3A3C)
    val textPrimary = Color(0xFFF5F5F7)
    val textSecondary = Color(0xFF8E8E93)
    val amber = Color(0xFFFFD60A)
    val amberDark = Color(0xFFCC9900)

    return HonqColorScheme(
        background = background,
        surface = surface,
        surfaceVariant = surfaceVariant,
        border = Color(0xFF3A3A3C),
        textPrimary = textPrimary,
        textSecondary = textSecondary,
        textMuted = Color(0xFF636366),

        primary = amber,
        primaryVariant = amberDark,
        onPrimary = background,
        primarySurface = Color(0xFF3D3A1A),

        correct = Color(0xFF30D158),
        correctSurface = Color(0xFF1A3D2A),
        incorrect = Color(0xFFFF453A),
        incorrectSurface = Color(0xFF3D1A1A),
        warning = Color(0xFFFF9F0A),

        loadingIndicator = amber,
        progressTrack = surfaceVariant,
        progressIndicator = amber,

        imagePlaceholder = surfaceVariant,
        imagePlaceholderIcon = textSecondary
    )
}

fun honqLightColorScheme(): HonqColorScheme {
    val background = Color(0xFFF2F2F7)
    val surface = Color(0xFFFFFFFF)
    val surfaceVariant = Color(0xFFE5E5EA)
    val textPrimary = Color(0xFF1C1C1E)
    val textSecondary = Color(0xFF636366)
    val amber = Color(0xFFCC8800)
    val amberDark = Color(0xFF996600)

    return HonqColorScheme(
        background = background,
        surface = surface,
        surfaceVariant = surfaceVariant,
        border = Color(0xFFD1D1D6),
        textPrimary = textPrimary,
        textSecondary = textSecondary,
        textMuted = Color(0xFF8E8E93),

        primary = amber,
        primaryVariant = amberDark,
        onPrimary = Color(0xFFFFFFFF),
        primarySurface = Color(0xFFFFF3D6),

        correct = Color(0xFF28A745),
        correctSurface = Color(0xFFD4EDDA),
        incorrect = Color(0xFFDC3545),
        incorrectSurface = Color(0xFFF8D7DA),
        warning = Color(0xFFE67E00),

        loadingIndicator = amber,
        progressTrack = surfaceVariant,
        progressIndicator = amber,

        imagePlaceholder = surfaceVariant,
        imagePlaceholderIcon = textSecondary
    )
}

object HonqColors {
    val Background = Color(0xFF1C1C1E)
    val Surface = Color(0xFF2C2C2E)
    val SurfaceVariant = Color(0xFF3A3A3C)
    val Border = Color(0xFF3A3A3C)
    val TextPrimary = Color(0xFFF5F5F7)
    val TextSecondary = Color(0xFF8E8E93)
    val TextMuted = Color(0xFF636366)
    val Amber = Color(0xFFFFD60A)
    val AmberDark = Color(0xFFCC9900)
    val AmberSurface = Color(0xFF3D3A1A)
    val Correct = Color(0xFF30D158)
    val CorrectSurface = Color(0xFF1A3D2A)
    val Incorrect = Color(0xFFFF453A)
    val IncorrectSurface = Color(0xFF3D1A1A)
    val Warning = Color(0xFFFF9F0A)
    val ProgressTrack = Color(0xFF3A3A3C)
}
