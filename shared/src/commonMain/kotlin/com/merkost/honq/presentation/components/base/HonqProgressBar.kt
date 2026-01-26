package com.merkost.honq.presentation.components.base

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import com.merkost.honq.presentation.theme.HonqColors
import com.merkost.honq.presentation.theme.HonqSizing

@Composable
fun HonqProgressBar(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = spring(
            dampingRatio = 0.8f,
            stiffness = 100f
        )
    )

    val shape = RoundedCornerShape(HonqSizing.progressBarHeight / 2)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(HonqSizing.progressBarHeight)
            .clip(shape)
            .background(HonqColors.SurfaceVariant)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animatedProgress)
                .height(HonqSizing.progressBarHeight)
                .clip(shape)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(HonqColors.Amber, HonqColors.AmberDark)
                    )
                )
        )
    }
}
