package com.merkost.honq.presentation.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween

object HonqMotion {
    val durationShort = 150
    val durationMedium = 300
    val durationLong = 500
    val durationEnter = 400
    val durationExit = 200

    val easingStandard = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    val easingEmphasized = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    val easingEmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
    val easingEmphasizedAccelerate = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)

    val springStiff = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessHigh
    )

    val springDefault = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )

    val springGentle = spring<Float>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessLow
    )

    val springBouncy = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMediumLow
    )

    fun <T> tweenShort() = tween<T>(durationShort, easing = easingStandard)
    fun <T> tweenMedium() = tween<T>(durationMedium, easing = easingStandard)
    fun <T> tweenLong() = tween<T>(durationLong, easing = easingEmphasized)
    fun <T> tweenEnter() = tween<T>(durationEnter, easing = easingEmphasizedDecelerate)
    fun <T> tweenExit() = tween<T>(durationExit, easing = easingEmphasizedAccelerate)
}
