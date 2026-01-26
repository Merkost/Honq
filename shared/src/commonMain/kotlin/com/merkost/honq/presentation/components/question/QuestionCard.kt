package com.merkost.honq.presentation.components.question

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import coil3.compose.AsyncImage
import com.merkost.honq.domain.model.Question
import com.merkost.honq.presentation.theme.HonqColors
import com.merkost.honq.presentation.theme.HonqSpacing

@Composable
fun QuestionCard(
    question: Question,
    selectedAnswer: Int?,
    answerRevealed: Boolean,
    onAnswerSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = question.text,
            color = HonqColors.TextPrimary,
            fontWeight = FontWeight.Medium
        )

        question.imageUrl?.let { url ->
            Spacer(modifier = Modifier.height(HonqSpacing.md))
            AsyncImage(
                model = url,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(HonqSpacing.lg))

        Column(verticalArrangement = Arrangement.spacedBy(HonqSpacing.sm)) {
            question.options.forEachIndexed { index, option ->
                val state = when {
                    !answerRevealed && selectedAnswer == index -> AnswerOptionState.Selected
                    !answerRevealed -> AnswerOptionState.Default
                    index == question.correctIndex -> AnswerOptionState.Correct
                    selectedAnswer == index -> AnswerOptionState.Incorrect
                    else -> AnswerOptionState.Disabled
                }

                AnswerOption(
                    text = option,
                    index = index,
                    state = state,
                    onClick = { if (!answerRevealed) onAnswerSelected(index) }
                )
            }
        }
    }
}
