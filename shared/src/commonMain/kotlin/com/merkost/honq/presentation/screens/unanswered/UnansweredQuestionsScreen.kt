package com.merkost.honq.presentation.screens.unanswered

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.merkost.honq.domain.model.Question
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
fun UnansweredQuestionsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToQuestion: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val container = koinInject<UnansweredQuestionsContainer> { parametersOf(scope) }

    val state by container.store.subscribe { action ->
        when (action) {
            UnansweredQuestionsAction.NavigateBack -> onNavigateBack()
            is UnansweredQuestionsAction.NavigateToQuestion -> onNavigateToQuestion(action.questionId)
        }
    }

    UnansweredQuestionsContent(
        state = state,
        onIntent = container.store::intent
    )
}

@Composable
private fun UnansweredQuestionsContent(
    state: UnansweredQuestionsState,
    onIntent: (UnansweredQuestionsIntent) -> Unit
) {
    HonqScaffold(
        title = "Unanswered Questions",
        onNavigateBack = { onIntent(UnansweredQuestionsIntent.NavigateBack) }
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
                    UnansweredQuestionsList(
                        questions = state.questions,
                        onQuestionClick = { questionId ->
                            onIntent(UnansweredQuestionsIntent.OpenQuestion(questionId))
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun UnansweredQuestionsList(
    questions: List<Question>,
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
                    text = "All questions answered",
                    color = colors.textPrimary,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(HonqSpacing.xs))
                Text(
                    text = "You've attempted every question. Great job!",
                    color = colors.textMuted,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        } else {
            questions.forEach { question ->
                UnansweredQuestionCard(
                    question = question,
                    onClick = { onQuestionClick(question.id) }
                )
            }
        }
    }
}

@Composable
private fun UnansweredQuestionCard(
    question: Question,
    onClick: () -> Unit
) {
    val colors = HonqTheme.colors

    HonqCard(onClick = onClick) {
        Text(
            text = question.text,
            color = colors.textPrimary,
            fontWeight = FontWeight.Medium,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(HonqSpacing.sm))
        Row(horizontalArrangement = Arrangement.spacedBy(HonqSpacing.xs)) {
            val categoryLabel = question.categoryName.ifBlank {
                question.categoryId
                    .replace("_", " ")
                    .split(" ")
                    .joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } }
            }
            CategoryPill(text = categoryLabel)
        }
    }
}

@Composable
private fun CategoryPill(text: String) {
    val colors = HonqTheme.colors

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(HonqSizing.cornerRadiusSmall))
            .background(colors.surfaceVariant)
            .padding(horizontal = HonqSpacing.sm, vertical = HonqSpacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            color = colors.textSecondary,
            style = MaterialTheme.typography.labelSmall
        )
    }
}
