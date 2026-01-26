package com.merkost.honq.presentation.screens.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.merkost.honq.presentation.components.base.BottomActionBarVertical
import com.merkost.honq.presentation.components.base.HonqButton
import com.merkost.honq.presentation.components.base.HonqButtonVariant
import com.merkost.honq.presentation.components.base.HonqCard
import com.merkost.honq.presentation.components.base.HonqProgressBar
import com.merkost.honq.presentation.components.base.HonqScaffold
import com.merkost.honq.presentation.theme.HonqColors
import com.merkost.honq.presentation.theme.HonqMotion
import com.merkost.honq.presentation.theme.HonqSizing
import com.merkost.honq.presentation.theme.HonqSpacing
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import pro.respawn.flowmvi.compose.dsl.subscribe

@Composable
fun HomeScreen(
    onNavigateToPractice: () -> Unit,
    onNavigateToMockTest: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val container = koinInject<HomeContainer> { parametersOf(scope) }

    val state by container.store.subscribe { action ->
        when (action) {
            HomeAction.NavigateToPractice -> onNavigateToPractice()
            HomeAction.NavigateToMockTest -> onNavigateToMockTest()
        }
    }

    HomeContent(
        state = state,
        onNavigateToPractice = onNavigateToPractice,
        onNavigateToMockTest = onNavigateToMockTest
    )
}

@Composable
private fun HomeContent(
    state: HomeState,
    onNavigateToPractice: () -> Unit,
    onNavigateToMockTest: () -> Unit
) {
    HonqScaffold(title = "Honq") { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(HonqSizing.screenPadding),
                verticalArrangement = Arrangement.spacedBy(HonqSpacing.lg)
            ) {
                SyncIndicator(isSyncing = state.isSyncing)
                ProgressCard(state)
                StatsRow(state)
            }

            BottomActionBarVertical {
                HonqButton(
                    text = "Start Practice",
                    onClick = onNavigateToPractice
                )
                HonqButton(
                    text = "Take Mock Test",
                    onClick = onNavigateToMockTest,
                    variant = HonqButtonVariant.Secondary
                )
            }
        }
    }
}

@Composable
private fun SyncIndicator(isSyncing: Boolean) {
    val alpha by animateFloatAsState(
        targetValue = if (isSyncing) 1f else 0f,
        animationSpec = HonqMotion.tweenMedium()
    )

    if (isSyncing || alpha > 0f) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer { this.alpha = alpha },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                color = HonqColors.Amber,
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.padding(horizontal = HonqSpacing.xs))
            Text(
                text = "Syncing...",
                color = HonqColors.TextMuted,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun ProgressCard(state: HomeState) {
    HonqCard {
        Text(
            text = "Your Progress",
            color = HonqColors.TextSecondary
        )
        Spacer(modifier = Modifier.height(HonqSpacing.sm))
        AnimatedContent(
            targetState = (state.progress.practiceAccuracy * 100).toInt(),
            transitionSpec = {
                fadeIn(HonqMotion.tweenMedium()).togetherWith(fadeOut(HonqMotion.tweenShort()))
            }
        ) { percentage ->
            Text(
                text = "$percentage%",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = HonqColors.Amber
            )
        }
        Spacer(modifier = Modifier.height(HonqSpacing.sm))
        HonqProgressBar(progress = state.progress.practiceAccuracy)
        Spacer(modifier = Modifier.height(HonqSpacing.sm))
        Text(
            text = "${state.progress.correctAnswers} / ${state.progress.totalPracticed} correct",
            color = HonqColors.TextMuted
        )
    }
}

@Composable
private fun StatsRow(state: HomeState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(HonqSpacing.md)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            label = "Mock Tests",
            value = state.progress.mockTestsTaken
        )
        StatCard(
            modifier = Modifier.weight(1f),
            label = "Passed",
            value = state.progress.mockTestsPassed
        )
    }
}

@Composable
private fun StatCard(
    label: String,
    value: Int,
    modifier: Modifier = Modifier
) {
    HonqCard(modifier = modifier) {
        Text(
            text = label,
            color = HonqColors.TextSecondary
        )
        AnimatedContent(
            targetState = value,
            transitionSpec = {
                fadeIn(HonqMotion.tweenMedium()).togetherWith(fadeOut(HonqMotion.tweenShort()))
            }
        ) { animatedValue ->
            Text(
                text = animatedValue.toString(),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = HonqColors.TextPrimary
            )
        }
    }
}
