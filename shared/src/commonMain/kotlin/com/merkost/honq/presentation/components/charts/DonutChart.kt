package com.merkost.honq.presentation.components.charts

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.merkost.honq.presentation.theme.HonqTheme

data class DonutChartSegment(
    val value: Float,
    val color: Color,
    val label: String
)

@Composable
fun DonutChart(
    segments: List<DonutChartSegment>,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 24.dp,
    backgroundColor: Color = HonqTheme.colors.surfaceVariant,
    centerText: String? = null,
    centerSubtext: String? = null,
    animationDuration: Int = 1000
) {
    val colors = HonqTheme.colors
    val animationProgress = remember { Animatable(0f) }
    val total = segments.sumOf { it.value.toDouble() }.toFloat()

    LaunchedEffect(segments) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(1f, animationSpec = tween(animationDuration))
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val strokeWidthPx = strokeWidth.toPx()
            val radius = (minOf(size.width, size.height) - strokeWidthPx) / 2
            val centerX = size.width / 2
            val centerY = size.height / 2

            // Background circle
            drawCircle(
                color = backgroundColor,
                radius = radius,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )

            if (total > 0 && segments.isNotEmpty()) {
                var startAngle = -90f

                segments.forEach { segment ->
                    val sweepAngle = (segment.value / total) * 360f * animationProgress.value

                    drawArc(
                        color = segment.color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round),
                        topLeft = androidx.compose.ui.geometry.Offset(
                            centerX - radius,
                            centerY - radius
                        ),
                        size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
                    )

                    startAngle += sweepAngle
                }
            }
        }

        // Center content
        if (centerText != null || centerSubtext != null) {
            androidx.compose.foundation.layout.Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                centerText?.let {
                    Text(
                        text = it,
                        color = colors.textPrimary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                centerSubtext?.let {
                    Text(
                        text = it,
                        color = colors.textMuted,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    progressColor: Color = HonqTheme.colors.primary,
    backgroundColor: Color = HonqTheme.colors.surfaceVariant,
    strokeWidth: Dp = 12.dp,
    centerText: String? = null,
    centerSubtext: String? = null,
    animationDuration: Int = 1000
) {
    val colors = HonqTheme.colors
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(progress) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(1f, animationSpec = tween(animationDuration))
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val strokeWidthPx = strokeWidth.toPx()
            val radius = (minOf(size.width, size.height) - strokeWidthPx) / 2
            val centerX = size.width / 2
            val centerY = size.height / 2

            // Background circle
            drawCircle(
                color = backgroundColor,
                radius = radius,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )

            // Progress arc
            val sweepAngle = progress.coerceIn(0f, 1f) * 360f * animationProgress.value

            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round),
                topLeft = androidx.compose.ui.geometry.Offset(
                    centerX - radius,
                    centerY - radius
                ),
                size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
            )
        }

        // Center content
        if (centerText != null || centerSubtext != null) {
            androidx.compose.foundation.layout.Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                centerText?.let {
                    Text(
                        text = it,
                        color = colors.textPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                centerSubtext?.let {
                    Text(
                        text = it,
                        color = colors.textMuted,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
