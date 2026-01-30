package com.merkost.honq.presentation.components.base

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.merkost.honq.presentation.theme.HonqMotion
import com.merkost.honq.presentation.theme.HonqSizing
import com.merkost.honq.presentation.theme.HonqSpacing
import com.merkost.honq.presentation.theme.HonqTheme
import honq.shared.generated.resources.Res
import honq.shared.generated.resources.error_subtitle
import honq.shared.generated.resources.error_title
import honq.shared.generated.resources.retry
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun FullscreenError(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = stringResource(Res.string.error_title),
    subtitle: String = stringResource(Res.string.error_subtitle),
    errorDetail: String? = null,
    icon: ImageVector = Icons.Rounded.WifiOff,
    retryText: String = stringResource(Res.string.retry)
) {
    val colors = HonqTheme.colors

    val iconAlpha = remember { Animatable(0f) }
    val contentAlpha = remember { Animatable(0f) }
    val buttonAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            iconAlpha.animateTo(
                1f,
                animationSpec = tween(
                    durationMillis = HonqMotion.durationEnter,
                    easing = HonqMotion.easingEmphasizedDecelerate
                )
            )
        }
        launch {
            kotlinx.coroutines.delay(100)
            contentAlpha.animateTo(
                1f,
                animationSpec = tween(
                    durationMillis = HonqMotion.durationEnter,
                    easing = HonqMotion.easingEmphasizedDecelerate
                )
            )
        }
        launch {
            kotlinx.coroutines.delay(200)
            buttonAlpha.animateTo(
                1f,
                animationSpec = tween(
                    durationMillis = HonqMotion.durationEnter,
                    easing = HonqMotion.easingEmphasizedDecelerate
                )
            )
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(horizontal = HonqSizing.screenPadding + 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .alpha(iconAlpha.value)
                    .size(80.dp)
                    .background(
                        color = colors.surfaceVariant,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = colors.textMuted
                )
            }

            Spacer(modifier = Modifier.height(HonqSpacing.lg))

            Column(
                modifier = Modifier.alpha(contentAlpha.value),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    color = colors.textPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(HonqSpacing.sm))

                Text(
                    text = subtitle,
                    color = colors.textSecondary,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                if (errorDetail != null) {
                    Spacer(modifier = Modifier.height(HonqSpacing.sm))

                    Text(
                        text = errorDetail,
                        color = colors.textMuted,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(HonqSpacing.xl))

            Box(modifier = Modifier.alpha(buttonAlpha.value)) {
                HonqButton(
                    text = retryText,
                    onClick = onRetry
                )
            }
        }
    }
}
