package com.merkost.honq.presentation.screens.mocktest

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.merkost.honq.presentation.components.base.BottomActionBar
import com.merkost.honq.presentation.components.base.HonqButton
import com.merkost.honq.presentation.components.base.HonqButtonVariant
import com.merkost.honq.presentation.components.base.HonqProgressBar
import com.merkost.honq.presentation.components.base.HonqScaffold
import com.merkost.honq.presentation.components.question.AnswerOption
import com.merkost.honq.presentation.components.question.AnswerOptionState
import com.merkost.honq.presentation.theme.HonqColors
import com.merkost.honq.presentation.theme.HonqSizing
import com.merkost.honq.presentation.theme.HonqSpacing
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import pro.respawn.flowmvi.compose.dsl.subscribe

@Composable
fun MockTestScreen(
    onNavigateBack: () -> Unit,
    onNavigateToResults: (score: Int, total: Int) -> Unit
) {
    val scope = rememberCoroutineScope()
    val container = koinInject<MockTestContainer> { parametersOf(scope) }

    val state by container.store.subscribe { action ->
        when (action) {
            MockTestAction.NavigateBack -> onNavigateBack()
            is MockTestAction.NavigateToResults -> onNavigateToResults(action.score, action.total)
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
    HonqScaffold(
        title = "Mock Test",
        onNavigateBack = onNavigateBack
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
                        color = HonqColors.Amber
                    )
                }
                state.error != null -> {
                    Text(
                        text = state.error,
                        color = HonqColors.Incorrect,
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
                            QuestionContent(state, onIntent)
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
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Question ${state.currentQuestionNumber}/${state.totalQuestions}",
            color = HonqColors.TextSecondary
        )
        Text(
            text = formatTime(state.timeRemaining.inWholeSeconds),
            color = if (state.timeRemaining.inWholeMinutes < 5) HonqColors.Incorrect else HonqColors.TextSecondary,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun QuestionContent(
    state: MockTestState,
    onIntent: (MockTestIntent) -> Unit
) {
    val question = state.session.currentQuestion ?: return

    Text(
        text = question.text,
        color = HonqColors.TextPrimary,
        fontWeight = FontWeight.Medium
    )

    Spacer(modifier = Modifier.height(HonqSpacing.lg))

    Column(verticalArrangement = Arrangement.spacedBy(HonqSpacing.sm)) {
        question.options.forEachIndexed { index, option ->
            val optionState = if (state.selectedAnswer == index) {
                AnswerOptionState.Selected
            } else {
                AnswerOptionState.Default
            }

            AnswerOption(
                text = option,
                index = index,
                state = optionState,
                onClick = { onIntent(MockTestIntent.AnswerSelected(index)) }
            )
        }
    }
}

private fun formatTime(totalSeconds: Long): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
}
