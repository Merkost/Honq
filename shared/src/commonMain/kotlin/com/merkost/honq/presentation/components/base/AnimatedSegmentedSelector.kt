package com.merkost.honq.presentation.components.base

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.merkost.honq.presentation.theme.HonqPreviewTheme
import com.merkost.honq.presentation.theme.HonqSizing
import com.merkost.honq.presentation.theme.HonqSpacing
import com.merkost.honq.presentation.theme.HonqTheme

@Composable
fun <T> AnimatedSegmentedSelector(
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    labelOf: (T) -> String,
    modifier: Modifier = Modifier,
    labelStyle: TextStyle = MaterialTheme.typography.labelMedium,
    height: androidx.compose.ui.unit.Dp = SELECTOR_HEIGHT
) {
    val colors = HonqTheme.colors
    val selectedIndex = options.indexOf(selected).coerceAtLeast(0)
    val shape = RoundedCornerShape(HonqSizing.cornerRadius)
    val indicatorShape = RoundedCornerShape(HonqSizing.cornerRadius)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(shape)
            .background(colors.surfaceVariant)
            .padding(2.dp)
    ) {
        val cellWidth = maxWidth / options.size
        val targetOffset = cellWidth * selectedIndex
        val animatedOffset by animateDpAsState(
            targetValue = targetOffset,
            animationSpec = spring(dampingRatio = 0.78f, stiffness = 360f),
            label = "segmentedIndicator"
        )

        Box(
            modifier = Modifier
                .offset(x = animatedOffset)
                .width(cellWidth)
                .fillMaxHeight()
                .clip(indicatorShape)
                .background(colors.primary)
        )

        Row(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(),
            horizontalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            options.forEachIndexed { index, option ->
                val isSelected = index == selectedIndex
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) colors.onPrimary else colors.textSecondary,
                    animationSpec = spring(stiffness = 360f),
                    label = "segmentedLabelColor"
                )
                val weightFloat by animateFloatAsState(
                    targetValue = if (isSelected) 1f else 0f,
                    animationSpec = spring(stiffness = 360f),
                    label = "segmentedLabelWeight"
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(indicatorShape)
                        .clickable { onSelect(option) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = labelOf(option),
                        style = labelStyle,
                        fontWeight = lerpFontWeight(weightFloat),
                        color = textColor,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = HonqSpacing.xs)
                    )
                }
            }
        }
    }
}

private fun lerpFontWeight(t: Float): FontWeight {
    val weight = (FontWeight.Normal.weight + (FontWeight.SemiBold.weight - FontWeight.Normal.weight) * t).toInt()
    return FontWeight(weight.coerceIn(100, 900))
}

private val SELECTOR_HEIGHT = 44.dp

@Preview
@Composable
private fun AnimatedSegmentedSelectorThreeOptionsPreview() {
    HonqPreviewTheme {
        AnimatedSegmentedSelector(
            options = listOf("System", "Light", "Dark"),
            selected = "System",
            onSelect = {},
            labelOf = { it }
        )
    }
}

@Preview
@Composable
private fun AnimatedSegmentedSelectorMiddleSelectedPreview() {
    HonqPreviewTheme {
        AnimatedSegmentedSelector(
            options = listOf("System", "Light", "Dark"),
            selected = "Light",
            onSelect = {},
            labelOf = { it }
        )
    }
}

@Preview
@Composable
private fun AnimatedSegmentedSelectorFourOptionsPreview() {
    HonqPreviewTheme {
        AnimatedSegmentedSelector(
            options = listOf("XS", "S", "M", "L"),
            selected = "M",
            onSelect = {},
            labelOf = { it }
        )
    }
}

@Preview
@Composable
private fun AnimatedSegmentedSelectorLightThemePreview() {
    HonqPreviewTheme(darkTheme = false) {
        AnimatedSegmentedSelector(
            options = listOf("System", "Light", "Dark"),
            selected = "Dark",
            onSelect = {},
            labelOf = { it }
        )
    }
}

