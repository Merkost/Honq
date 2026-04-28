package com.merkost.honq.presentation.components.charts

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.merkost.honq.presentation.theme.HonqTheme

data class LineChartData(
    val label: String,
    val value: Float
)

@Composable
fun LineChart(
    data: List<LineChartData>,
    modifier: Modifier = Modifier,
    lineColor: Color = HonqTheme.colors.primary,
    fillColor: Color = HonqTheme.colors.primarySurface,
    pointColor: Color = HonqTheme.colors.primary,
    labelColor: Color = HonqTheme.colors.textMuted,
    gridColor: Color = HonqTheme.colors.border,
    showPoints: Boolean = true,
    showFill: Boolean = true,
    showGrid: Boolean = true,
    animationDuration: Int = 1000
) {
    val colors = HonqTheme.colors
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = MaterialTheme.typography.labelSmall.copy(color = labelColor)
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(data) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(1f, animationSpec = tween(animationDuration))
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
    ) {
        if (data.isEmpty()) return@Canvas

        val labelHeight = 24.dp.toPx()
        val topPadding = 16.dp.toPx()
        val chartHeight = size.height - labelHeight - topPadding
        val chartWidth = size.width

        val maxValue = data.maxOfOrNull { it.value } ?: 1f
        val minValue = 0f

        // Draw horizontal grid lines
        if (showGrid) {
            val gridLines = 4
            for (i in 0..gridLines) {
                val y = topPadding + (chartHeight * i / gridLines)
                drawLine(
                    color = gridColor.copy(alpha = 0.3f),
                    start = Offset(0f, y),
                    end = Offset(chartWidth, y),
                    strokeWidth = 1.dp.toPx()
                )
            }
        }

        if (data.size < 2) {
            // Single point - just draw a dot
            val x = chartWidth / 2
            val normalizedValue = ((data[0].value - minValue) / (maxValue - minValue)).coerceIn(0f, 1f)
            val y = topPadding + chartHeight * (1 - normalizedValue)

            if (showPoints) {
                drawCircle(
                    color = pointColor,
                    radius = 6.dp.toPx(),
                    center = Offset(x, y)
                )
            }
            return@Canvas
        }

        val stepX = chartWidth / (data.size - 1)

        // Calculate points
        val points = data.mapIndexed { index, item ->
            val x = index * stepX
            val normalizedValue = if (maxValue == minValue) 0.5f
            else ((item.value - minValue) / (maxValue - minValue)).coerceIn(0f, 1f)
            val y = topPadding + chartHeight * (1 - normalizedValue)
            Offset(x, y)
        }

        // Animate points
        val animatedPoints = points.mapIndexed { index, point ->
            val progress = (animationProgress.value * data.size - index).coerceIn(0f, 1f)
            val startY = topPadding + chartHeight
            Offset(point.x, startY + (point.y - startY) * progress)
        }

        // Draw fill area
        if (showFill && animatedPoints.size >= 2) {
            val fillPath = Path().apply {
                moveTo(animatedPoints.first().x, topPadding + chartHeight)
                animatedPoints.forEach { point ->
                    lineTo(point.x, point.y)
                }
                lineTo(animatedPoints.last().x, topPadding + chartHeight)
                close()
            }

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(fillColor, fillColor.copy(alpha = 0.1f)),
                    startY = topPadding,
                    endY = topPadding + chartHeight
                )
            )
        }

        // Draw line
        if (animatedPoints.size >= 2) {
            val linePath = Path().apply {
                moveTo(animatedPoints.first().x, animatedPoints.first().y)
                for (i in 1 until animatedPoints.size) {
                    lineTo(animatedPoints[i].x, animatedPoints[i].y)
                }
            }

            drawPath(
                path = linePath,
                color = lineColor,
                style = Stroke(
                    width = 3.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }

        // Draw points
        if (showPoints) {
            animatedPoints.forEach { point ->
                drawCircle(
                    color = colors.surface,
                    radius = 6.dp.toPx(),
                    center = point
                )
                drawCircle(
                    color = pointColor,
                    radius = 4.dp.toPx(),
                    center = point
                )
            }
        }

        // Draw labels
        data.forEachIndexed { index, item ->
            if (index % maxOf(1, data.size / 5) == 0 || index == data.size - 1) {
                val x = index * stepX
                val textLayoutResult = textMeasurer.measure(
                    text = item.label,
                    style = labelStyle
                )
                val textX = (x - textLayoutResult.size.width / 2)
                    .coerceIn(0f, chartWidth - textLayoutResult.size.width)
                val textY = topPadding + chartHeight + 8.dp.toPx()

                drawText(
                    textLayoutResult = textLayoutResult,
                    topLeft = Offset(textX, textY)
                )
            }
        }
    }
}
