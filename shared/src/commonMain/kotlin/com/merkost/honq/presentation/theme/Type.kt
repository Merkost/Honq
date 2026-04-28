package com.merkost.honq.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import honq.shared.generated.resources.Res
import honq.shared.generated.resources.dm_sans_bold
import honq.shared.generated.resources.dm_sans_medium
import honq.shared.generated.resources.dm_sans_regular
import honq.shared.generated.resources.dm_sans_semibold
import org.jetbrains.compose.resources.Font

@Composable
fun DmSansFontFamily(): FontFamily = FontFamily(
    Font(Res.font.dm_sans_regular, FontWeight.Normal),
    Font(Res.font.dm_sans_medium, FontWeight.Medium),
    Font(Res.font.dm_sans_semibold, FontWeight.SemiBold),
    Font(Res.font.dm_sans_bold, FontWeight.Bold),
)

private fun Float.scaled(scale: Float): TextUnit = (this * scale).sp

@Composable
fun honqTypography(fontScale: Float = 1f): Typography {
    val fontFamily = DmSansFontFamily()
    val s = fontScale
    return Typography(
        displayLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 57f.scaled(s),
            lineHeight = 64f.scaled(s),
            letterSpacing = (-0.25).sp
        ),
        displayMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 45f.scaled(s),
            lineHeight = 52f.scaled(s)
        ),
        displaySmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 36f.scaled(s),
            lineHeight = 44f.scaled(s)
        ),
        headlineLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 32f.scaled(s),
            lineHeight = 40f.scaled(s)
        ),
        headlineMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 28f.scaled(s),
            lineHeight = 36f.scaled(s)
        ),
        headlineSmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 24f.scaled(s),
            lineHeight = 32f.scaled(s)
        ),
        titleLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 22f.scaled(s),
            lineHeight = 28f.scaled(s)
        ),
        titleMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 17f.scaled(s),
            lineHeight = 24f.scaled(s),
            letterSpacing = 0.1.sp
        ),
        titleSmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 15f.scaled(s),
            lineHeight = 21f.scaled(s),
            letterSpacing = 0.1.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 17f.scaled(s),
            lineHeight = 25f.scaled(s),
            letterSpacing = 0.15.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 15f.scaled(s),
            lineHeight = 22f.scaled(s),
            letterSpacing = 0.15.sp
        ),
        bodySmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 13f.scaled(s),
            lineHeight = 19f.scaled(s),
            letterSpacing = 0.25.sp
        ),
        labelLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 15f.scaled(s),
            lineHeight = 21f.scaled(s),
            letterSpacing = 0.1.sp
        ),
        labelMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 13f.scaled(s),
            lineHeight = 18f.scaled(s),
            letterSpacing = 0.25.sp
        ),
        labelSmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12f.scaled(s),
            lineHeight = 16f.scaled(s),
            letterSpacing = 0.4.sp
        )
    )
}
