package com.merkost.honq.presentation.screens.review

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.merkost.honq.domain.model.IncorrectAnswer
import com.merkost.honq.presentation.components.base.AnimatedFavoriteButton
import com.merkost.honq.presentation.components.base.BottomActionBarVertical
import com.merkost.honq.presentation.components.base.HonqButton
import com.merkost.honq.presentation.components.base.HonqButtonVariant
import com.merkost.honq.presentation.components.base.HonqProgressBar
import com.merkost.honq.presentation.components.base.HonqScaffold
import com.merkost.honq.presentation.components.question.ExplanationCard
import com.merkost.honq.presentation.components.question.QuestionCard
import com.merkost.honq.presentation.theme.HonqTheme
import com.merkost.honq.presentation.theme.HonqMotion
import com.merkost.honq.presentation.theme.HonqSizing
import com.merkost.honq.presentation.theme.HonqSpacing
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import pro.respawn.flowmvi.compose.dsl.subscribe

@Composable
fun ReviewIncorrectScreen(
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val container = koinInject<ReviewIncorrectContainer> { parametersOf(scope) }

    val state by container.store.subscribe { action ->
        when (action) {
            ReviewIncorrectAction.NavigateBack -> onNavigateBack()
        }
    }

    ReviewIncorrectContent(
        state = state,
        onIntent = container.store::intent,
        onNavigateBack = onNavigateBack
    )
}

@Composable
private fun ReviewIncorrectContent(
    state: ReviewIncorrectState,
    onIntent: (ReviewIncorrectIntent) -> Unit,
    onNavigateBack: () -> Unit
) {
    HonqScaffold(
        title = "Review Incorrect",
        onNavigateBack = onNavigateBack,
        actions = {
            val currentAnswer = state.currentAnswer
            if (currentAnswer != null) {
                val isFavorite = state.favoriteQuestionIds.contains(currentAnswer.question.id)
                AnimatedFavoriteButton(
                    isFavorite = isFavorite,
                    onClick = { onIntent(ReviewIncorrectIntent.ToggleFavorite(currentAnswer.question.id)) }
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
                        color = HonqTheme.colors.primary
                    )
                }
                state.error != null -> {
                    Text(
                        text = state.error,
                        color = HonqTheme.colors.incorrect,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                state.incorrectAnswers.isEmpty() -> {
                    EmptyState(modifier = Modifier.align(Alignment.Center))
                }
                state.currentAnswer != null -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                                .padding(HonqSizing.screenPadding)
                        ) {
                            ReviewHeader(state)
                            Spacer(modifier = Modifier.height(HonqSpacing.sm))
                            HonqProgressBar(progress = state.progress)
                            Spacer(modifier = Modifier.height(HonqSpacing.lg))

                            AnimatedContent(
                                targetState = ReviewSnapshot(
                                    incorrectAnswer = state.currentAnswer!!,
                                    questionIndex = state.currentIndex
                                ),
                                transitionSpec = {
                                    val direction = if (targetState.questionIndex > initialState.questionIndex) 1 else -1
                                    (slideInHorizontally(
                                        animationSpec = tween(HonqMotion.durationMedium, easing = HonqMotion.easingStandard),
                                        initialOffsetX = { fullWidth -> fullWidth * direction }
                                    ) + fadeIn(
                                        animationSpec = tween(HonqMotion.durationMedium)
                                    )).togetherWith(
                                        slideOutHorizontally(
                                            animationSpec = tween(HonqMotion.durationMedium, easing = HonqMotion.easingStandard),
                                            targetOffsetX = { fullWidth -> -fullWidth * direction }
                                        ) + fadeOut(
                                            animationSpec = tween(HonqMotion.durationShort)
                                        )
                                    )
                                },
                                contentKey = { it.questionIndex }
                            ) { snapshot ->
                                Column {
                                    QuestionCard(
                                        question = snapshot.incorrectAnswer.question,
                                        selectedAnswer = snapshot.incorrectAnswer.selectedAnswerIndex,
                                        answerRevealed = true,
                                        onAnswerSelected = { }
                                    )
                                    Spacer(modifier = Modifier.height(HonqSpacing.lg))
                                    ExplanationCard(
                                        explanation = snapshot.incorrectAnswer.question.explanation,
                                        isCorrect = false
                                    )
                                }
                            }
                        }

                        BottomActionBarVertical {
                            NavigationButtons(
                                state = state,
                                onPrevious = { onIntent(ReviewIncorrectIntent.PreviousQuestion) },
                                onNext = { onIntent(ReviewIncorrectIntent.NextQuestion) },
                                onDone = onNavigateBack
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewHeader(state: ReviewIncorrectState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Question ${state.currentQuestionNumber} of ${state.totalQuestions}",
            style = MaterialTheme.typography.bodyMedium,
            color = HonqTheme.colors.textSecondary
        )
        Text(
            text = "${state.totalQuestions} incorrect",
            style = MaterialTheme.typography.labelMedium,
            color = HonqTheme.colors.incorrect,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun NavigationButtons(
    state: ReviewIncorrectState,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onDone: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(HonqSpacing.md)
    ) {
        if (!state.isFirstQuestion) {
            HonqButton(
                text = "Previous",
                onClick = onPrevious,
                variant = HonqButtonVariant.Secondary,
                modifier = Modifier.weight(1f)
            )
        }
        if (state.isLastQuestion) {
            HonqButton(
                text = "Done",
                onClick = onDone,
                modifier = Modifier.weight(1f)
            )
        } else {
            HonqButton(
                text = "Next",
                onClick = onNext,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "No incorrect answers to review",
            style = MaterialTheme.typography.bodyMedium,
            color = HonqTheme.colors.textSecondary
        )
    }
}

@Immutable
private data class ReviewSnapshot(
    val incorrectAnswer: IncorrectAnswer,
    val questionIndex: Int
)
