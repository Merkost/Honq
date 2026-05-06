package com.merkost.honq.presentation.components.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.merkost.honq.presentation.screens.home.Readiness
import com.merkost.honq.presentation.screens.home.ReadinessZone
import com.merkost.honq.presentation.theme.HonqPreviewTheme
import com.merkost.honq.presentation.theme.HonqTheme
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun ReadinessGauge(
    score: Int,
    passMark: Int,
    modifier: Modifier = Modifier
) {
    val colors = HonqTheme.colors

    val animatedScore = remember { Animatable(0f) }
    LaunchedEffect(score) {
        animatedScore.animateTo(
            targetValue = score.toFloat().coerceIn(0f, 100f),
            animationSpec = tween(durationMillis = 1200, easing = EaseOutCubic)
        )
    }

    val pivotPulse by rememberInfiniteTransition(label = "pivotPulse").animateFloat(
        initialValue = 0.78f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = EaseOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pivotPulseAlpha"
    )

    val zone = Readiness.zone(animatedScore.value.roundToInt(), passMark)
    val needleColor = when (zone) {
        ReadinessZone.Green -> colors.correct
        ReadinessZone.Amber -> colors.primary
        ReadinessZone.Red -> colors.incorrect
    }

    Canvas(modifier = modifier.aspectRatio(GAUGE_ASPECT_RATIO)) {
        drawSpeedometer(
            score = animatedScore.value,
            passMark = passMark,
            trackColor = colors.surfaceVariant,
            redColor = colors.incorrect,
            amberColor = colors.primary,
            greenColor = colors.correct,
            needleColor = needleColor,
            tickColor = colors.textMuted,
            pivotFillColor = colors.surface,
            pivotRingColor = needleColor,
            pivotPulse = pivotPulse
        )
    }
}

private const val GAUGE_ASPECT_RATIO = 1.17f
private val OUTER_PADDING = 10.dp
private val STROKE_WIDTH = 10.dp
private const val VERTICAL_EXTENT_FACTOR = 1.707f // 1 + sin(45°)
private const val ARC_START_ANGLE = 135f
private const val ARC_TOTAL_SWEEP = 270f
private const val DEGREES_PER_POINT = ARC_TOTAL_SWEEP / 100f
private const val ZONE_TRACK_ALPHA = 0.42f

private fun DrawScope.drawSpeedometer(
    score: Float,
    passMark: Int,
    trackColor: Color,
    redColor: Color,
    amberColor: Color,
    greenColor: Color,
    needleColor: Color,
    tickColor: Color,
    pivotFillColor: Color,
    pivotRingColor: Color,
    pivotPulse: Float
) {
    val strokeWidth = STROKE_WIDTH.toPx()
    val outerPadding = strokeWidth / 2f + OUTER_PADDING.toPx()

    val maxRadiusByWidth = size.width / 2f - outerPadding
    val maxRadiusByHeight = size.height / VERTICAL_EXTENT_FACTOR - outerPadding
    val radius = min(maxRadiusByWidth, maxRadiusByHeight)
    if (radius <= 0f) return

    val centerX = size.width / 2f
    val centerY = radius + outerPadding

    val arcSize = Size(radius * 2f, radius * 2f)
    val arcTopLeft = Offset(centerX - radius, centerY - radius)

    drawArc(
        color = trackColor,
        startAngle = ARC_START_ANGLE,
        sweepAngle = ARC_TOTAL_SWEEP,
        useCenter = false,
        topLeft = arcTopLeft,
        size = arcSize,
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
    )

    val redEnd = (passMark - 20).coerceAtLeast(0)
    val redSweep = redEnd * DEGREES_PER_POINT
    val amberSweep = (passMark - redEnd) * DEGREES_PER_POINT
    val greenSweep = (100 - passMark).coerceAtLeast(0) * DEGREES_PER_POINT

    if (redSweep > 0f) {
        drawArc(
            color = redColor.copy(alpha = ZONE_TRACK_ALPHA),
            startAngle = ARC_START_ANGLE,
            sweepAngle = redSweep,
            useCenter = false,
            topLeft = arcTopLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
        )
    }
    if (amberSweep > 0f) {
        drawArc(
            color = amberColor.copy(alpha = ZONE_TRACK_ALPHA),
            startAngle = ARC_START_ANGLE + redSweep,
            sweepAngle = amberSweep,
            useCenter = false,
            topLeft = arcTopLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
        )
    }
    if (greenSweep > 0f) {
        drawArc(
            color = greenColor.copy(alpha = ZONE_TRACK_ALPHA),
            startAngle = ARC_START_ANGLE + redSweep + amberSweep,
            sweepAngle = greenSweep,
            useCenter = false,
            topLeft = arcTopLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }

    drawTickMarks(
        center = Offset(centerX, centerY),
        radius = radius,
        strokeWidth = strokeWidth,
        passMark = passMark,
        tickColor = tickColor,
        passColor = greenColor
    )

    drawNeedle(
        center = Offset(centerX, centerY),
        radius = radius,
        strokeWidth = strokeWidth,
        score = score,
        color = needleColor
    )

    drawCircle(
        color = pivotRingColor.copy(alpha = 0.10f * pivotPulse),
        radius = 12.dp.toPx(),
        center = Offset(centerX, centerY)
    )
    drawCircle(
        color = pivotFillColor,
        radius = 6.dp.toPx(),
        center = Offset(centerX, centerY)
    )
    drawCircle(
        color = pivotRingColor.copy(alpha = pivotPulse),
        radius = 6.dp.toPx(),
        center = Offset(centerX, centerY),
        style = Stroke(width = 1.5.dp.toPx())
    )
}

private fun DrawScope.drawTickMarks(
    center: Offset,
    radius: Float,
    strokeWidth: Float,
    passMark: Int,
    tickColor: Color,
    passColor: Color
) {
    val innerInsideArc = radius - strokeWidth / 2f - 5.dp.toPx()
    val outerInsideArc = radius - strokeWidth / 2f - 0.5.dp.toPx()
    for (point in 0..100 step 10) {
        if (point == passMark) continue
        val angleRad = ((ARC_START_ANGLE + point * DEGREES_PER_POINT) * PI / 180.0).toFloat()
        val cosA = cos(angleRad)
        val sinA = sin(angleRad)
        drawLine(
            color = tickColor.copy(alpha = 0.55f),
            start = Offset(center.x + cosA * innerInsideArc, center.y + sinA * innerInsideArc),
            end = Offset(center.x + cosA * outerInsideArc, center.y + sinA * outerInsideArc),
            strokeWidth = 1.5.dp.toPx(),
            cap = StrokeCap.Round
        )
    }

    val passAngleRad = ((ARC_START_ANGLE + passMark * DEGREES_PER_POINT) * PI / 180.0).toFloat()
    val passCos = cos(passAngleRad)
    val passSin = sin(passAngleRad)
    val passInner = radius + strokeWidth / 2f + 2.dp.toPx()
    val passOuter = radius + strokeWidth / 2f + 10.dp.toPx()
    drawLine(
        color = passColor,
        start = Offset(center.x + passCos * passInner, center.y + passSin * passInner),
        end = Offset(center.x + passCos * passOuter, center.y + passSin * passOuter),
        strokeWidth = 2.5.dp.toPx(),
        cap = StrokeCap.Round
    )
    drawCircle(
        color = passColor,
        radius = 3.dp.toPx(),
        center = Offset(center.x + passCos * passOuter, center.y + passSin * passOuter)
    )
}

private fun DrawScope.drawNeedle(
    center: Offset,
    radius: Float,
    strokeWidth: Float,
    score: Float,
    color: Color
) {
    val needleLength = radius - strokeWidth - 8.dp.toPx()
    val needleAngleRad =
        ((ARC_START_ANGLE + score * DEGREES_PER_POINT) * PI / 180.0).toFloat()
    val perpAngleRad = needleAngleRad + (PI / 2.0).toFloat()
    val baseHalfWidth = 4.dp.toPx()

    val tip = Offset(
        x = center.x + cos(needleAngleRad) * needleLength,
        y = center.y + sin(needleAngleRad) * needleLength
    )
    val baseLeft = Offset(
        x = center.x + cos(perpAngleRad) * baseHalfWidth,
        y = center.y + sin(perpAngleRad) * baseHalfWidth
    )
    val baseRight = Offset(
        x = center.x - cos(perpAngleRad) * baseHalfWidth,
        y = center.y - sin(perpAngleRad) * baseHalfWidth
    )

    val needlePath = Path().apply {
        moveTo(tip.x, tip.y)
        lineTo(baseLeft.x, baseLeft.y)
        lineTo(baseRight.x, baseRight.y)
        close()
    }
    drawPath(needlePath, color = color)
}

@Preview
@Composable
private fun ReadinessGaugeReadyPreview() {
    HonqPreviewTheme {
        Box(modifier = Modifier.size(width = 132.dp, height = 113.dp)) {
            ReadinessGauge(score = 88, passMark = 80)
        }
    }
}

@Preview
@Composable
private fun ReadinessGaugeOnTrackPreview() {
    HonqPreviewTheme {
        Box(modifier = Modifier.size(width = 132.dp, height = 113.dp)) {
            ReadinessGauge(score = 72, passMark = 80)
        }
    }
}

@Preview
@Composable
private fun ReadinessGaugeKeepStudyingPreview() {
    HonqPreviewTheme {
        Box(modifier = Modifier.size(width = 132.dp, height = 113.dp)) {
            ReadinessGauge(score = 35, passMark = 80)
        }
    }
}

@Preview
@Composable
private fun ReadinessGaugeOnTrackLightPreview() {
    HonqPreviewTheme(darkTheme = false) {
        Box(modifier = Modifier.size(width = 132.dp, height = 113.dp)) {
            ReadinessGauge(score = 72, passMark = 80)
        }
    }
}

@Preview
@Composable
private fun ReadinessGaugeZeroStatePreview() {
    HonqPreviewTheme {
        Box(modifier = Modifier.size(width = 132.dp, height = 113.dp)) {
            ReadinessGauge(score = 0, passMark = 80)
        }
    }
}

@Preview
@Composable
private fun ReadinessGaugeMaxedPreview() {
    HonqPreviewTheme {
        Box(modifier = Modifier.size(width = 132.dp, height = 113.dp)) {
            ReadinessGauge(score = 100, passMark = 80)
        }
    }
}

@Preview
@Composable
private fun ReadinessGaugeCustomPassMarkPreview() {
    HonqPreviewTheme {
        Box(modifier = Modifier.size(width = 132.dp, height = 113.dp)) {
            ReadinessGauge(score = 60, passMark = 75)
        }
    }
}

