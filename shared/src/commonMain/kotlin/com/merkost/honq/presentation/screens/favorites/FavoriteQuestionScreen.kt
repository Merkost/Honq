package com.merkost.honq.presentation.screens.favorites

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import com.merkost.honq.presentation.components.base.AnimatedAppear
import com.merkost.honq.presentation.components.base.AnimatedFavoriteButton
import com.merkost.honq.presentation.components.base.BottomActionBarVertical
import com.merkost.honq.presentation.components.base.HonqButton
import com.merkost.honq.presentation.components.base.HonqScaffold
import com.merkost.honq.presentation.components.question.ExplanationCard
import com.merkost.honq.presentation.components.question.QuestionCard
import com.merkost.honq.presentation.theme.HonqSizing
import com.merkost.honq.presentation.theme.HonqSpacing
import com.merkost.honq.presentation.theme.HonqTheme
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import pro.respawn.flowmvi.compose.dsl.subscribe

@Composable
fun FavoriteQuestionScreen(
    questionId: String,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val container = koinInject<FavoriteQuestionContainer> { parametersOf(questionId, scope) }

    val state by container.store.subscribe { action ->
        when (action) {
            FavoriteQuestionAction.NavigateBack -> onNavigateBack()
        }
    }

    FavoriteQuestionContent(
        state = state,
        onIntent = container.store::intent
    )
}

@Composable
private fun FavoriteQuestionContent(
    state: FavoriteQuestionState,
    onIntent: (FavoriteQuestionIntent) -> Unit
) {
    val colors = HonqTheme.colors

    HonqScaffold(
        title = "Saved Question",
        onNavigateBack = { onIntent(FavoriteQuestionIntent.NavigateBack) },
        actions = {
            val question = state.question
            if (question != null) {
                val isFavorite = state.favoriteQuestionIds.contains(question.id)
                AnimatedFavoriteButton(
                    isFavorite = isFavorite,
                    onClick = { onIntent(FavoriteQuestionIntent.ToggleFavorite) }
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
                state.question != null -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                                .padding(HonqSizing.screenPadding)
                        ) {
                            QuestionCard(
                                question = state.question,
                                selectedAnswer = state.selectedAnswer,
                                answerRevealed = state.answerRevealed,
                                onAnswerSelected = { onIntent(FavoriteQuestionIntent.AnswerSelected(it)) }
                            )

                            AnimatedAppear(visible = state.answerRevealed) {
                                Column {
                                    Spacer(modifier = Modifier.height(HonqSpacing.lg))
                                    ExplanationCard(
                                        explanation = state.question.explanation,
                                        isCorrect = state.selectedAnswer == state.question.correctIndex
                                    )
                                }
                            }
                        }

                        AnimatedAppear(visible = state.answerRevealed) {
                            BottomActionBarVertical {
                                HonqButton(
                                    text = "Try Again",
                                    onClick = { onIntent(FavoriteQuestionIntent.TryAgain) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
