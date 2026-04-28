package com.merkost.honq.presentation.components.question

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.merkost.honq.data.local.seed.BundledImagePath
import com.merkost.honq.domain.model.Question
import com.merkost.honq.presentation.theme.HonqSpacing
import com.merkost.honq.presentation.theme.HonqTheme
import honq.shared.generated.resources.Res

@Composable
fun QuestionCard(
    question: Question,
    selectedAnswer: Int?,
    answerRevealed: Boolean,
    onAnswerSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = HonqTheme.colors

    Column(modifier = modifier) {
        Text(
            text = question.text,
            color = colors.textPrimary,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )

        BundledImagePath.resolve(question.imageUrl)?.let { resourcePath ->
            Spacer(modifier = Modifier.height(HonqSpacing.md))
            QuestionImage(
                url = Res.getUri(resourcePath),
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
