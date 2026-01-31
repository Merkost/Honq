package com.merkost.honq.presentation.components.question

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.merkost.honq.presentation.components.base.HonqCard
import com.merkost.honq.presentation.theme.HonqTheme
import com.merkost.honq.presentation.theme.HonqMotion
import com.merkost.honq.presentation.theme.HonqSpacing

@Composable
fun ExplanationCard(
    explanation: String,
    isCorrect: Boolean,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    val iconScale by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = 0.5f,
            stiffness = 300f
        )
    )

    val icon = if (isCorrect) Icons.Rounded.CheckCircle else Icons.Rounded.Cancel
    val iconColor = if (isCorrect) HonqTheme.colors.correct else HonqTheme.colors.incorrect
    val title = if (isCorrect) "Correct!" else "Incorrect"

    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(HonqMotion.tweenEnter()) + expandVertically(
            animationSpec = HonqMotion.tweenEnter(),
            expandFrom = Alignment.Top
        )
    ) {
        HonqCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier
                        .size(28.dp)
                        .graphicsLayer {
                            scaleX = iconScale
                            scaleY = iconScale
                        }
                )
                Spacer(modifier = Modifier.width(HonqSpacing.sm))
                Text(
                    text = title,
                    color = iconColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            explanation.takeIf { it.isNotBlank() }?.let {
                Spacer(modifier = Modifier.height(HonqSpacing.md))
                Text(
                    text = explanation,
                    color = HonqTheme.colors.textSecondary,
                    lineHeight = 22.sp
                )
            }
        }
    }
}
