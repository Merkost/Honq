package com.merkost.honq.presentation.components.base

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.merkost.honq.presentation.theme.HonqSizing
import com.merkost.honq.presentation.theme.HonqSpacing
import com.merkost.honq.presentation.theme.HonqTheme

@Composable
fun BottomActionBar(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = HonqSizing.screenPadding,
                vertical = HonqSpacing.md
            ),
        horizontalArrangement = Arrangement.spacedBy(HonqSpacing.md),
        content = content
    )
}

@Composable
fun BottomActionBarVertical(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val bg = HonqTheme.colors.background
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colorStops = arrayOf(
                        0f to Color.Transparent,
                        0.3f to bg.copy(alpha = 0.85f),
                        1f to bg
                    )
                )
            )
            .padding(
                horizontal = HonqSizing.screenPadding,
                vertical = HonqSpacing.sm
            ),
        verticalArrangement = Arrangement.spacedBy(HonqSpacing.sm),
        content = content
    )
}
