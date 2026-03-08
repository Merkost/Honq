package com.merkost.honq.presentation.screens.paywall

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.merkost.honq.presentation.components.base.HonqButton
import com.merkost.honq.presentation.components.base.HonqCard
import com.merkost.honq.presentation.theme.HonqMotion
import com.merkost.honq.presentation.theme.HonqSizing
import com.merkost.honq.presentation.theme.HonqSpacing
import com.merkost.honq.presentation.theme.HonqTheme
import honq.shared.generated.resources.Res
import honq.shared.generated.resources.ic_honq_logo
import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.animateLottieCompositionAsState
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

private const val STAGGER_DELAY = 80L
private const val SLIDE_UP_PX = 40f

@OptIn(ExperimentalResourceApi::class)
@Composable
fun PurchaseSuccessScreen(
    onContinue: () -> Unit
) {
    val colors = HonqTheme.colors

    val animProgress = remember { List(4) { Animatable(0f) } }

    val logoScale = remember { Animatable(0f) }

    val confettiComposition by rememberLottieComposition {
        LottieCompositionSpec.JsonString(
            Res.readBytes("files/fullscreen_confetti.json").decodeToString()
        )
    }
    val confettiProgress by animateLottieCompositionAsState(
        composition = confettiComposition,
        iterations = 1
    )

    // Launch stagger animations
    LaunchedEffect(Unit) {
        animProgress.forEachIndexed { index, anim ->
            launch {
                delay(index * STAGGER_DELAY)
                anim.animateTo(
                    1f,
                    animationSpec = tween(
                        durationMillis = HonqMotion.durationEnter,
                        easing = HonqMotion.easingEmphasizedDecelerate
                    )
                )
            }
        }
    }

    // Logo spring scale-in
    LaunchedEffect(Unit) {
        logoScale.animateTo(
            1f,
            animationSpec = HonqMotion.springBouncy
        )
    }

    fun Modifier.staggered(index: Int): Modifier {
        val progress = animProgress.getOrNull(index)?.value ?: 1f
        return this
            .alpha(progress)
            .offset { IntOffset(0, ((1f - progress) * SLIDE_UP_PX).toInt()) }
    }

    val features = listOf(
        "Unlimited mock tests",
        "Practice by category",
        "Smart Practice (spaced repetition)",
        "Detailed statistics & analytics",
        "Weakest questions review",
        "Track unanswered questions"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        // Lottie confetti (above background, behind content)
        if (confettiComposition != null && confettiProgress < 1f) {
            Image(
                painter = rememberLottiePainter(
                    composition = confettiComposition,
                    progress = { confettiProgress }
                ),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = HonqSizing.screenPadding)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Honq Logo
            Image(
                painter = painterResource(Res.drawable.ic_honq_logo),
                contentDescription = "Honq Logo",
                modifier = Modifier
                    .staggered(0)
                    .graphicsLayer {
                        scaleX = logoScale.value
                        scaleY = logoScale.value
                    }
                    .size(96.dp)
            )

            Spacer(modifier = Modifier.height(HonqSpacing.lg))

            // Title
            Text(
                text = "Welcome to Honq Pro!",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = colors.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.staggered(1)
            )

            Spacer(modifier = Modifier.height(HonqSpacing.sm))

            // Subtitle
            Text(
                text = "Thank you for your purchase. You now have full access to all premium features.",
                fontSize = 16.sp,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp,
                modifier = Modifier.staggered(1)
            )

            Spacer(modifier = Modifier.height(HonqSpacing.lg))

            // Features card
            HonqCard(
                modifier = Modifier.staggered(2)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(HonqSpacing.sm)
                ) {
                    features.forEach { feature ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(HonqSpacing.sm)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = colors.correct,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = feature,
                                fontSize = 14.sp,
                                color = colors.textPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Continue button
            HonqButton(
                text = "Let's Go!",
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .staggered(3)
            )

            Spacer(modifier = Modifier.height(HonqSpacing.lg))
        }

    }
}
