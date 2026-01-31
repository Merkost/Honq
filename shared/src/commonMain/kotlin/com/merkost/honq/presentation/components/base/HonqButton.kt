package com.merkost.honq.presentation.components.base

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.merkost.honq.presentation.theme.HonqTheme
import com.merkost.honq.presentation.theme.HonqMotion
import com.merkost.honq.presentation.theme.HonqSizing

enum class HonqButtonVariant { Primary, Secondary, Text }

@Composable
fun HonqButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: HonqButtonVariant = HonqButtonVariant.Primary,
    enabled: Boolean = true,
    loading: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled && !loading) 0.97f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f)
    )

    val buttonModifier = modifier
        .fillMaxWidth()
        .height(HonqSizing.buttonHeight)
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }

    val buttonContent: @Composable () -> Unit = {
        AnimatedContent(
            targetState = loading,
            transitionSpec = {
                (fadeIn(HonqMotion.tweenShort()) + scaleIn(
                    animationSpec = HonqMotion.tweenShort(),
                    initialScale = 0.8f
                )).togetherWith(
                    fadeOut(HonqMotion.tweenShort()) + scaleOut(
                        animationSpec = HonqMotion.tweenShort(),
                        targetScale = 0.8f
                    )
                )
            }
        ) { isLoading ->
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = when (variant) {
                        HonqButtonVariant.Primary -> HonqTheme.colors.background
                        else -> HonqTheme.colors.primary
                    },
                    strokeWidth = 2.dp
                )
            } else {
                Text(text)
            }
        }
    }

    when (variant) {
        HonqButtonVariant.Primary -> {
            Button(
                onClick = onClick,
                modifier = buttonModifier,
                enabled = enabled && !loading,
                interactionSource = interactionSource,
                shape = RoundedCornerShape(HonqSizing.cornerRadius),
                colors = ButtonDefaults.buttonColors(
                    containerColor = HonqTheme.colors.primary,
                    contentColor = HonqTheme.colors.background
                )
            ) {
                buttonContent()
            }
        }
        HonqButtonVariant.Secondary -> {
            OutlinedButton(
                onClick = onClick,
                modifier = buttonModifier,
                enabled = enabled && !loading,
                interactionSource = interactionSource,
                shape = RoundedCornerShape(HonqSizing.cornerRadius),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = HonqTheme.colors.primary
                )
            ) {
                buttonContent()
            }
        }
        HonqButtonVariant.Text -> {
            TextButton(
                onClick = onClick,
                modifier = buttonModifier,
                enabled = enabled && !loading,
                interactionSource = interactionSource,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = HonqTheme.colors.primary
                )
            ) {
                buttonContent()
            }
        }
    }
}
