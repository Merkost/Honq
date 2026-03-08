package com.merkost.honq.presentation.components.base

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.merkost.honq.presentation.theme.HonqTheme

@Composable
fun ProBadge(modifier: Modifier = Modifier) {
    Icon(
        imageVector = Icons.Rounded.Lock,
        contentDescription = null,
        tint = HonqTheme.colors.textMuted,
        modifier = modifier.size(16.dp)
    )
}
