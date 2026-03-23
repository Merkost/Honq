package com.merkost.honq.presentation.screens.practice

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Immutable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import com.merkost.honq.domain.model.Question
import com.merkost.honq.presentation.components.base.AnimatedFavoriteButton
import com.merkost.honq.presentation.components.base.BottomActionBarVertical
import com.merkost.honq.presentation.components.base.HonqButton
import com.merkost.honq.presentation.components.base.HonqButtonVariant
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
fun PracticeScreen(
    categoryId: String? = null,
    categoryName: String? = null,
    smartMode: Boolean = false,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val container = koinInject<PracticeContainer> { parametersOf(categoryId, categoryName, smartMode, scope) }

    val state by container.store.subscribe { action ->
        when (action) {
            PracticeAction.NavigateBack -> onNavigateBack()
        }
    }

    PracticeContent(
        state = state,
        onIntent = container.store::intent,
        onNavigateBack = onNavigateBack
    )
}

@Composable
private fun PracticeContent(
    state: PracticeState,
    onIntent: (PracticeIntent) -> Unit,
    onNavigateBack: () -> Unit
) {
    val title = when {
        state.smartMode -> "Smart Practice"
        state.categoryName != null -> "Practice: ${state.categoryName}"
        else -> "Practice"
    }

    HonqScaffold(
        title = title,
        onNavigateBack = onNavigateBack,
        actions = {
            val question = state.currentQuestion
            if (question != null) {
                val isFavorite = state.favoriteQuestionIds.contains(question.id)
                AnimatedFavoriteButton(
                    isFavorite = isFavorite,
                    onClick = { onIntent(PracticeIntent.ToggleFavorite(question.id)) }
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
                state.currentQuestion != null -> {
                    val swipeThreshold = 100f
                    val dragAccumulator = remember { mutableFloatStateOf(0f) }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(state.currentIndex, state.answerRevealed) {
                                detectHorizontalDragGestures(
                                    onDragStart = { dragAccumulator.floatValue = 0f },
                                    onDragEnd = {
                                        val drag = dragAccumulator.floatValue
                                        when {
                                            drag > swipeThreshold && !state.isFirstQuestion ->
                                                onIntent(PracticeIntent.PreviousQuestion)
                                            drag < -swipeThreshold && state.answerRevealed ->
                                                onIntent(PracticeIntent.NextQuestion)
                                        }
                                    },
                                    onHorizontalDrag = { _, dragAmount ->
                                        dragAccumulator.floatValue += dragAmount
                                    }
                                )
                            }
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                                .padding(HonqSizing.screenPadding)
                        ) {
                            ScoreHeader(state)
                            Spacer(modifier = Modifier.height(HonqSpacing.lg))

                            AnimatedContent(
                                targetState = QuestionSnapshot(
                                    question = state.currentQuestion!!,
                                    selectedAnswer = state.selectedAnswer,
                                    answerRevealed = state.answerRevealed,
                                    questionIndex = state.currentIndex,
                                    navigationDirection = state.navigationDirection
                                ),
                                transitionSpec = {
                                    val isForward = targetState.navigationDirection == PracticeNavigationDirection.Forward
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
                                Column {
                                    QuestionCard(
                                        question = snapshot.question,
                                        selectedAnswer = snapshot.selectedAnswer,
                                        answerRevealed = snapshot.answerRevealed,
                                        onAnswerSelected = { onIntent(PracticeIntent.AnswerSelected(it)) }
                                    )
                                    if (snapshot.answerRevealed) {
                                        Spacer(modifier = Modifier.height(HonqSpacing.lg))
                                        ExplanationCard(
                                            explanation = snapshot.question.explanation,
                                            isCorrect = snapshot.selectedAnswer == snapshot.question.correctIndex
                                        )
                                    }
                                }
                            }
                        }

                        BottomActionBarVertical {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(HonqSpacing.md)
                            ) {
                                HonqButton(
                                    text = "Previous",
                                    onClick = { onIntent(PracticeIntent.PreviousQuestion) },
                                    variant = HonqButtonVariant.Secondary,
                                    enabled = !state.isFirstQuestion,
                                    modifier = Modifier.weight(1f)
                                )
                                HonqButton(
                                    text = "Next Question",
                                    onClick = { onIntent(PracticeIntent.NextQuestion) },
                                    enabled = state.answerRevealed,
                                    loading = state.isLoadingNext,
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
private fun ScoreHeader(state: PracticeState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Question ${state.currentQuestionNumber}",
            style = MaterialTheme.typography.bodyMedium,
            color = HonqTheme.colors.textSecondary
        )
        Text(
            text = "${state.correctAnswers} correct",
            style = MaterialTheme.typography.labelMedium,
            color = HonqTheme.colors.correct,
            fontWeight = FontWeight.Medium
        )
    }
}

@Immutable
private data class QuestionSnapshot(
    val question: Question,
    val selectedAnswer: Int?,
    val answerRevealed: Boolean,
    val questionIndex: Int,
    val navigationDirection: PracticeNavigationDirection
)
