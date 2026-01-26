package com.merkost.honq.presentation.components.charts

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.merkost.honq.presentation.theme.HonqTheme

data class BarChartData(
    val label: String,
    val value: Float,
    val maxValue: Float = 100f
)

@Composable
fun BarChart(
    data: List<BarChartData>,
    modifier: Modifier = Modifier,
    barColor: Color = HonqTheme.colors.primary,
    backgroundColor: Color = HonqTheme.colors.surfaceVariant,
    labelColor: Color = HonqTheme.colors.textMuted,
    barSpacing: Dp = 8.dp,
    cornerRadius: Dp = 4.dp,
    animationDuration: Int = 800
) {
    val colors = HonqTheme.colors
    val textMeasurer = rememberTextMeasurer()
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(data) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(1f, animationSpec = tween(animationDuration))
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        if (data.isEmpty()) return@Canvas

        val labelHeight = 24.dp.toPx()
        val chartHeight = size.height - labelHeight - 8.dp.toPx()
        val barWidth = (size.width - (data.size - 1) * barSpacing.toPx()) / data.size
        val cornerRadiusPx = cornerRadius.toPx()

        data.forEachIndexed { index, item ->
            val x = index * (barWidth + barSpacing.toPx())
            val normalizedValue = (item.value / item.maxValue).coerceIn(0f, 1f)
            val animatedHeight = chartHeight * normalizedValue * animationProgress.value

            // Background bar
            drawRoundRect(
                color = backgroundColor,
                topLeft = Offset(x, 0f),
                size = Size(barWidth, chartHeight),
                cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
            )

            // Value bar
            if (animatedHeight > 0) {
                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(x, chartHeight - animatedHeight),
                    size = Size(barWidth, animatedHeight),
                    cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
                )
            }

            // Label
            val textLayoutResult = textMeasurer.measure(
                text = item.label,
                style = TextStyle(color = labelColor, fontSize = 10.sp)
            )
            val textX = x + (barWidth - textLayoutResult.size.width) / 2
            val textY = chartHeight + 8.dp.toPx()

            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = Offset(textX, textY)
            )
        }
    }
}
