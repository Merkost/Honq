package com.merkost.honq.presentation.screens.practice

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
import com.merkost.honq.presentation.components.base.AnimatedAppear
import com.merkost.honq.presentation.components.base.BottomActionBarVertical
import com.merkost.honq.presentation.components.base.HonqButton
import com.merkost.honq.presentation.components.base.HonqScaffold
import com.merkost.honq.presentation.components.question.ExplanationCard
import com.merkost.honq.presentation.components.question.QuestionCard
import com.merkost.honq.presentation.theme.HonqColors
import com.merkost.honq.presentation.theme.HonqSizing
import com.merkost.honq.presentation.theme.HonqSpacing
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import pro.respawn.flowmvi.compose.dsl.subscribe

@Composable
fun PracticeScreen(
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val container = koinInject<PracticeContainer> { parametersOf(scope) }

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
    HonqScaffold(
        title = "Practice",
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
                state.currentQuestion != null -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                                .padding(HonqSizing.screenPadding)
                        ) {
                            ScoreHeader(state)
                            Spacer(modifier = Modifier.height(HonqSpacing.lg))
                            QuestionCard(
                                question = state.currentQuestion,
                                selectedAnswer = state.selectedAnswer,
                                answerRevealed = state.answerRevealed,
                                onAnswerSelected = { onIntent(PracticeIntent.AnswerSelected(it)) }
                            )
                            AnimatedAppear(visible = state.answerRevealed) {
                                Column {
                                    Spacer(modifier = Modifier.height(HonqSpacing.lg))
                                    ExplanationCard(
                                        explanation = state.currentQuestion.explanation,
                                        isCorrect = state.selectedAnswer == state.currentQuestion.correctIndex
                                    )
                                }
                            }
                        }

                        AnimatedAppear(visible = state.answerRevealed) {
                            BottomActionBarVertical {
                                HonqButton(
                                    text = "Next Question",
                                    onClick = { onIntent(PracticeIntent.NextQuestion) }
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
            text = "Question ${state.questionsAnswered}",
            color = HonqColors.TextSecondary
        )
        Text(
            text = "${state.correctAnswers} correct",
            color = HonqColors.Correct,
            fontWeight = FontWeight.Medium
        )
    }
}
