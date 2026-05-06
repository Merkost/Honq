package com.merkost.honq.presentation.components.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.merkost.honq.presentation.theme.HonqPreviewTheme
import com.merkost.honq.presentation.theme.HonqSizing
import com.merkost.honq.presentation.theme.HonqSpacing
import com.merkost.honq.presentation.theme.HonqTheme

@Composable
fun PrimaryPracticeCta(
    eyebrow: String,
    title: String,
    subtitle: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = HonqTheme.colors
    val onCta = colors.onPrimary
    val contentAlpha = if (enabled) 1f else 0.5f

    val gradient = Brush.verticalGradient(
        colors = listOf(
            colors.primary,
            colors.primaryVariant
        )
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .alpha(contentAlpha)
            .clip(RoundedCornerShape(HonqSizing.cornerRadius))
            .background(gradient)
            .clickable(enabled = enabled, onClick = onClick)
            .drawWithContent {
                drawContent()
                val dashWidth = 16.dp.toPx()
                val gapWidth = 12.dp.toPx()
                val inset = 22.dp.toPx()
                val strokeY = size.height - 9.dp.toPx()
                drawLine(
                    color = onCta.copy(alpha = 0.45f),
                    start = Offset(inset, strokeY),
                    end = Offset(size.width - inset, strokeY),
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(
                        intervals = floatArrayOf(dashWidth, gapWidth),
                        phase = 0f
                    )
                )
            }
            .padding(
                start = HonqSpacing.md,
                end = HonqSpacing.md,
                top = HonqSpacing.md,
                bottom = HonqSpacing.lg
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(HonqSpacing.md)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(onCta.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.PlayArrow,
                contentDescription = null,
                tint = onCta,
                modifier = Modifier.size(HonqSizing.iconSizeSmall)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = eyebrow.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.8.sp,
                    fontSize = 10.sp
                ),
                color = onCta.copy(alpha = 0.6f)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    letterSpacing = (-0.2).sp
                ),
                color = onCta
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelMedium,
                color = onCta.copy(alpha = 0.72f)
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
            contentDescription = null,
            tint = onCta.copy(alpha = 0.85f),
            modifier = Modifier.size(HonqSizing.iconSize20)
        )
    }
}

@Preview
@Composable
private fun PrimaryPracticeCtaContinuePreview() {
    HonqPreviewTheme {
        PrimaryPracticeCta(
            eyebrow = "Continue",
            title = "Practice questions",
            subtitle = "By category",
            enabled = true,
            onClick = {}
        )
    }
}

@Preview
@Composable
private fun PrimaryPracticeCtaStartPreview() {
    HonqPreviewTheme {
        PrimaryPracticeCta(
            eyebrow = "Start",
            title = "Start practising",
            subtitle = "Random selection",
            enabled = true,
            onClick = {}
        )
    }
}

@Preview
@Composable
private fun PrimaryPracticeCtaDisabledPreview() {
    HonqPreviewTheme {
        PrimaryPracticeCta(
            eyebrow = "Start",
            title = "Start practising",
            subtitle = "Loading…",
            enabled = false,
            onClick = {}
        )
    }
}

@Preview
@Composable
private fun PrimaryPracticeCtaContinueLightPreview() {
    HonqPreviewTheme(darkTheme = false) {
        PrimaryPracticeCta(
            eyebrow = "Continue",
            title = "Practice questions",
            subtitle = "By category",
            enabled = true,
            onClick = {}
        )
    }
}

