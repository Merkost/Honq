package com.merkost.honq.presentation.screens.weakest

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.merkost.honq.domain.model.WeakQuestion
import com.merkost.honq.presentation.components.base.FullscreenLoading
import com.merkost.honq.presentation.components.base.HonqCard
import com.merkost.honq.presentation.components.base.HonqScaffold
import com.merkost.honq.presentation.theme.HonqMotion
import com.merkost.honq.presentation.theme.HonqSizing
import com.merkost.honq.presentation.theme.HonqSpacing
import com.merkost.honq.presentation.theme.HonqTheme
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import pro.respawn.flowmvi.compose.dsl.subscribe

@Composable
fun WeakestQuestionsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToQuestion: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val container = koinInject<WeakestQuestionsContainer> { parametersOf(scope) }

    val state by container.store.subscribe { action ->
        when (action) {
            WeakestQuestionsAction.NavigateBack -> onNavigateBack()
            is WeakestQuestionsAction.NavigateToQuestion -> onNavigateToQuestion(action.questionId)
        }
    }

    WeakestQuestionsContent(
        state = state,
        onIntent = container.store::intent
    )
}

@Composable
private fun WeakestQuestionsContent(
    state: WeakestQuestionsState,
    onIntent: (WeakestQuestionsIntent) -> Unit
) {
    HonqScaffold(
        title = "Weakest Questions",
        onNavigateBack = { onIntent(WeakestQuestionsIntent.NavigateBack) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AnimatedContent(
                targetState = state.isLoading,
                transitionSpec = {
                    fadeIn(HonqMotion.tweenMedium()).togetherWith(fadeOut(HonqMotion.tweenShort()))
                },
                contentKey = { it }
            ) { isLoading ->
                if (isLoading) {
                    FullscreenLoading()
                } else {
                    WeakestQuestionsList(
                        questions = state.questions,
                        onQuestionClick = { questionId ->
                            onIntent(WeakestQuestionsIntent.OpenQuestion(questionId))
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun WeakestQuestionsList(
    questions: List<WeakQuestion>,
    onQuestionClick: (String) -> Unit
) {
    val colors = HonqTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(HonqSizing.screenPadding)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(HonqSpacing.md)
    ) {
        if (questions.isEmpty()) {
            HonqCard {
                Text(
                    text = "No weak questions",
                    color = colors.textPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(HonqSpacing.xs))
                Text(
                    text = "You haven't gotten any questions wrong yet. Keep practicing!",
                    color = colors.textMuted,
                    fontSize = 12.sp
                )
            }
        } else {
            questions.forEach { weakQuestion ->
                WeakQuestionCard(
                    weakQuestion = weakQuestion,
                    onClick = { onQuestionClick(weakQuestion.question.id) }
                )
            }
        }
    }
}

@Composable
private fun WeakQuestionCard(
    weakQuestion: WeakQuestion,
    onClick: () -> Unit
) {
    val colors = HonqTheme.colors

    HonqCard(onClick = onClick) {
        Text(
            text = weakQuestion.question.text,
            color = colors.textPrimary,
            fontWeight = FontWeight.Medium,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(HonqSpacing.sm))
        Row(horizontalArrangement = Arrangement.spacedBy(HonqSpacing.xs)) {
            MetaPill(
                text = "Wrong ${weakQuestion.wrongCount}/${weakQuestion.totalAttempts}",
                isHighlighted = true
            )
            val categoryLabel = weakQuestion.question.categoryName.ifBlank {
                weakQuestion.question.categoryId
                    .replace("_", " ")
                    .split(" ")
                    .joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } }
            }
            MetaPill(text = categoryLabel)
        }
    }
}

@Composable
private fun MetaPill(
    text: String,
    isHighlighted: Boolean = false
) {
    val colors = HonqTheme.colors

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(HonqSizing.cornerRadiusSmall))
            .background(if (isHighlighted) colors.incorrect.copy(alpha = 0.15f) else colors.surfaceVariant)
            .padding(horizontal = HonqSpacing.sm, vertical = HonqSpacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            color = if (isHighlighted) colors.incorrect else colors.textSecondary,
            fontSize = 12.sp,
            fontWeight = if (isHighlighted) FontWeight.Medium else FontWeight.Normal
        )
    }
}
