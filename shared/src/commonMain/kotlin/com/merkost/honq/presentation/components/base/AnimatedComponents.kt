package com.merkost.honq.presentation.components.base

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import com.merkost.honq.presentation.theme.HonqMotion

@Composable
fun AnimatedAppear(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(HonqMotion.tweenEnter()) + expandVertically(
            animationSpec = HonqMotion.tweenEnter(),
            expandFrom = Alignment.Top
        ),
        exit = fadeOut(HonqMotion.tweenExit()) + shrinkVertically(
            animationSpec = HonqMotion.tweenExit(),
            shrinkTowards = Alignment.Top
        )
    ) {
        content()
    }
}

@Composable
fun AnimatedSlideIn(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(HonqMotion.tweenEnter()) + slideInVertically(
            animationSpec = HonqMotion.tweenEnter(),
            initialOffsetY = { it / 4 }
        ),
        exit = fadeOut(HonqMotion.tweenExit()) + slideOutVertically(
            animationSpec = HonqMotion.tweenExit(),
            targetOffsetY = { -it / 4 }
        )
    ) {
        content()
    }
}

@Composable
fun AnimatedScale(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(HonqMotion.tweenEnter()) + scaleIn(
            animationSpec = HonqMotion.tweenEnter(),
            initialScale = 0.8f
        ),
        exit = fadeOut(HonqMotion.tweenExit()) + scaleOut(
            animationSpec = HonqMotion.tweenExit(),
            targetScale = 0.8f
        )
    ) {
        content()
    }
}

@Composable
fun <T> AnimatedStateChange(
    targetState: T,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit
) {
    AnimatedContent(
        targetState = targetState,
        modifier = modifier,
        transitionSpec = {
            (fadeIn(HonqMotion.tweenMedium()) + scaleIn(
                animationSpec = HonqMotion.tweenMedium(),
                initialScale = 0.92f
            )).togetherWith(
                fadeOut(HonqMotion.tweenShort()) + scaleOut(
                    animationSpec = HonqMotion.tweenShort(),
                    targetScale = 0.92f
                )
            )
        }
    ) { state ->
        content(state)
    }
}

@Composable
fun PressableScale(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    pressedScale: Float = 0.96f,
    content: @Composable () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) pressedScale else 1f,
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = 400f
        )
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .pointerInput(enabled) {
                if (enabled) {
                    detectTapGestures(
                        onPress = {
                            isPressed = true
                            tryAwaitRelease()
                            isPressed = false
                            onClick()
                        }
                    )
                }
            }
    ) {
        content()
    }
}
