package com.merkost.honq.presentation.components.base

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import com.merkost.honq.presentation.theme.HonqColors
import com.merkost.honq.presentation.theme.HonqSizing

@Composable
fun HonqCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.98f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f)
    )

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                },
            interactionSource = interactionSource,
            shape = RoundedCornerShape(HonqSizing.cornerRadius),
            colors = CardDefaults.cardColors(
                containerColor = HonqColors.Surface
            )
        ) {
            Column(
                modifier = Modifier.padding(HonqSizing.cardPadding),
                content = content
            )
        }
    } else {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(HonqSizing.cornerRadius),
            colors = CardDefaults.cardColors(
                containerColor = HonqColors.Surface
            )
        ) {
            Column(
                modifier = Modifier.padding(HonqSizing.cardPadding),
                content = content
            )
        }
    }
}
