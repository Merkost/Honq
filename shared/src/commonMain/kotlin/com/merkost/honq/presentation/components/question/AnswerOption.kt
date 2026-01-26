package com.merkost.honq.presentation.components.question

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.merkost.honq.presentation.theme.HonqColors
import com.merkost.honq.presentation.theme.HonqMotion
import com.merkost.honq.presentation.theme.HonqSizing
import com.merkost.honq.presentation.theme.HonqSpacing

enum class AnswerOptionState { Default, Selected, Correct, Incorrect, Disabled }

@Composable
fun AnswerOption(
    text: String,
    index: Int,
    state: AnswerOptionState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val label = ('A' + index).toString()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isEnabled = state == AnswerOptionState.Default || state == AnswerOptionState.Selected

    val scale by animateFloatAsState(
        targetValue = if (isPressed && isEnabled) 0.98f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f)
    )

    val backgroundColor by animateColorAsState(
        targetValue = when (state) {
            AnswerOptionState.Default -> HonqColors.Surface
            AnswerOptionState.Selected -> HonqColors.AmberSurface
            AnswerOptionState.Correct -> HonqColors.CorrectSurface
            AnswerOptionState.Incorrect -> HonqColors.IncorrectSurface
            AnswerOptionState.Disabled -> HonqColors.Surface
        },
        animationSpec = HonqMotion.tweenMedium()
    )

    val borderColor by animateColorAsState(
        targetValue = when (state) {
            AnswerOptionState.Default -> HonqColors.Border
            AnswerOptionState.Selected -> HonqColors.Amber
            AnswerOptionState.Correct -> HonqColors.Correct
            AnswerOptionState.Incorrect -> HonqColors.Incorrect
            AnswerOptionState.Disabled -> HonqColors.Border
        },
        animationSpec = HonqMotion.tweenMedium()
    )

    val textColor by animateColorAsState(
        targetValue = when (state) {
            AnswerOptionState.Default -> HonqColors.TextPrimary
            AnswerOptionState.Selected -> HonqColors.TextPrimary
            AnswerOptionState.Correct -> HonqColors.Correct
            AnswerOptionState.Incorrect -> HonqColors.Incorrect
            AnswerOptionState.Disabled -> HonqColors.TextMuted
        },
        animationSpec = HonqMotion.tweenMedium()
    )

    val labelBackgroundColor by animateColorAsState(
        targetValue = when (state) {
            AnswerOptionState.Default -> HonqColors.SurfaceVariant
            AnswerOptionState.Selected -> HonqColors.Amber
            AnswerOptionState.Correct -> HonqColors.Correct
            AnswerOptionState.Incorrect -> HonqColors.Incorrect
            AnswerOptionState.Disabled -> HonqColors.SurfaceVariant
        },
        animationSpec = HonqMotion.tweenMedium()
    )

    val labelTextColor by animateColorAsState(
        targetValue = when (state) {
            AnswerOptionState.Default -> HonqColors.TextSecondary
            AnswerOptionState.Selected -> HonqColors.Background
            AnswerOptionState.Correct -> HonqColors.Background
            AnswerOptionState.Incorrect -> HonqColors.Background
            AnswerOptionState.Disabled -> HonqColors.TextMuted
        },
        animationSpec = HonqMotion.tweenMedium()
    )

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        enabled = isEnabled,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(HonqSizing.cornerRadius),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(1.5.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(HonqSpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OptionLabel(
                label = label,
                backgroundColor = labelBackgroundColor,
                textColor = labelTextColor
            )
            Spacer(modifier = Modifier.width(HonqSpacing.md))
            Text(
                text = text,
                color = textColor,
                fontWeight = if (state != AnswerOptionState.Default) FontWeight.Medium else FontWeight.Normal
            )
        }
    }
}

@Composable
private fun OptionLabel(
    label: String,
    backgroundColor: Color,
    textColor: Color
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}
