package com.merkost.honq.presentation.components.base

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.unit.dp
import com.merkost.honq.presentation.theme.HonqTheme
import com.merkost.honq.presentation.theme.HonqSizing
import com.merkost.honq.presentation.theme.HonqSpacing

@Composable
fun BottomActionBar(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            HonqTheme.colors.background
                        )
                    )
                )
                .padding(top = 8.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(HonqTheme.colors.background)
                .padding(
                    horizontal = HonqSizing.screenPadding,
                    vertical = HonqSpacing.md
                ),
            horizontalArrangement = Arrangement.spacedBy(HonqSpacing.md),
            content = content
        )
    }
}

@Composable
fun BottomActionBarVertical(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            HonqTheme.colors.background
                        )
                    )
                )
                .padding(top = 4.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(HonqTheme.colors.background)
                .padding(
                    horizontal = HonqSizing.screenPadding,
                    vertical = HonqSpacing.sm
                ),
            verticalArrangement = Arrangement.spacedBy(HonqSpacing.sm),
            content = content
        )
    }
}
