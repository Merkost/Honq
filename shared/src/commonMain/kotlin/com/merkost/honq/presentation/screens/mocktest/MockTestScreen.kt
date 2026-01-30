package com.merkost.honq.presentation.screens.mocktest

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.merkost.honq.domain.model.Question
import com.merkost.honq.presentation.components.base.AnimatedFavoriteButton
import com.merkost.honq.presentation.components.base.BottomActionBar
import com.merkost.honq.presentation.components.base.HonqButton
import com.merkost.honq.presentation.components.base.HonqButtonVariant
import com.merkost.honq.presentation.components.base.HonqProgressBar
import com.merkost.honq.presentation.components.base.HonqScaffold
import com.merkost.honq.presentation.components.question.QuestionCard
import com.merkost.honq.presentation.theme.HonqMotion
import com.merkost.honq.presentation.theme.HonqSizing
import com.merkost.honq.presentation.theme.HonqSpacing
import com.merkost.honq.presentation.theme.HonqTheme
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import pro.respawn.flowmvi.compose.dsl.subscribe

@Composable
fun MockTestScreen(
    onNavigateBack: () -> Unit,
    onNavigateToResults: (score: Int, total: Int, hasIncorrect: Boolean, passPercentage: Int, categoryBreakdown: List<com.merkost.honq.domain.model.CategoryScore>) -> Unit
) {
    val scope = rememberCoroutineScope()
    val container = koinInject<MockTestContainer> { parametersOf(scope) }

    val state by container.store.subscribe { action ->
        when (action) {
            MockTestAction.NavigateBack -> onNavigateBack()
            is MockTestAction.NavigateToResults -> onNavigateToResults(action.score, action.total, action.hasIncorrect, action.passPercentage, action.categoryBreakdown)
        }
    }

    MockTestContent(
        state = state,
        onIntent = container.store::intent,
        onNavigateBack = onNavigateBack
    )
}

@Composable
private fun MockTestContent(
    state: MockTestState,
    onIntent: (MockTestIntent) -> Unit,
    onNavigateBack: () -> Unit
) {
    val colors = HonqTheme.colors

    HonqScaffold(
        title = "Mock Test",
        onNavigateBack = onNavigateBack,
        actions = {
            val question = state.session.currentQuestion
            if (question != null) {
                val isFavorite = state.favoriteQuestionIds.contains(question.id)
                AnimatedFavoriteButton(
                    isFavorite = isFavorite,
                    onClick = { onIntent(MockTestIntent.ToggleFavorite(question.id)) }
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = colors.loadingIndicator
                    )
                }
                state.error != null -> {
                    Text(
                        text = state.error,
                        color = colors.incorrect,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                state.session.currentQuestion != null -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                                .padding(HonqSizing.screenPadding)
                        ) {
                            TestHeader(state)
                            Spacer(modifier = Modifier.height(HonqSpacing.md))
                            HonqProgressBar(progress = state.session.progress)
                            Spacer(modifier = Modifier.height(HonqSpacing.lg))

                            AnimatedContent(
                                targetState = MockTestQuestionSnapshot(
                                    question = state.session.currentQuestion,
                                    selectedAnswer = state.selectedAnswer,
                                    questionIndex = state.session.currentIndex,
                                    navigationDirection = state.navigationDirection
                                ),
                                transitionSpec = {
                                    val isForward = targetState.navigationDirection == NavigationDirection.Forward
                                    val enterOffset = if (isForward) 1 else -1
                                    val exitOffset = if (isForward) -1 else 1

                                    (slideInHorizontally(
                                        animationSpec = tween(HonqMotion.durationMedium, easing = HonqMotion.easingStandard),
                                        initialOffsetX = { fullWidth -> fullWidth * enterOffset }
                                    ) + fadeIn(
                                        animationSpec = tween(HonqMotion.durationMedium)
                                    )).togetherWith(
                                        slideOutHorizontally(
                                            animationSpec = tween(HonqMotion.durationMedium, easing = HonqMotion.easingStandard),
                                            targetOffsetX = { fullWidth -> fullWidth * exitOffset }
                                        ) + fadeOut(
                                            animationSpec = tween(HonqMotion.durationShort)
                                        )
                                    )
                                },
                                contentKey = { it.questionIndex }
                            ) { snapshot ->
                                snapshot.question?.let {
                                    QuestionCard(
                                        question = it,
                                        selectedAnswer = snapshot.selectedAnswer,
                                        answerRevealed = false,
                                        onAnswerSelected = { index -> onIntent(MockTestIntent.AnswerSelected(index)) }
                                    )
                                }
                            }
                        }

                        BottomActionBar {
                            if (state.session.currentIndex > 0) {
                                HonqButton(
                                    text = "Previous",
                                    onClick = { onIntent(MockTestIntent.PreviousQuestion) },
                                    variant = HonqButtonVariant.Secondary,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            if (state.session.currentIndex < state.session.questions.lastIndex) {
                                HonqButton(
                                    text = "Next",
                                    onClick = { onIntent(MockTestIntent.NextQuestion) },
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                HonqButton(
                                    text = "Submit",
                                    onClick = { onIntent(MockTestIntent.SubmitTest) },
                                    loading = state.isSubmitting,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TestHeader(state: MockTestState) {
    val colors = HonqTheme.colors

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Question ${state.currentQuestionNumber}/${state.totalQuestions}",
            color = colors.textSecondary
        )
        Text(
            text = formatTime(state.timeRemaining.inWholeSeconds),
            color = if (state.timeRemaining.inWholeMinutes < 5) colors.incorrect else colors.textSecondary,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun formatTime(totalSeconds: Long): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
}

@Immutable
private data class MockTestQuestionSnapshot(
    val question: Question?,
    val selectedAnswer: Int?,
    val questionIndex: Int,
    val navigationDirection: NavigationDirection
)
