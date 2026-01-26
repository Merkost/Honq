package com.merkost.honq.presentation.components.base

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.merkost.honq.presentation.theme.HonqSpacing
import com.merkost.honq.presentation.theme.HonqTheme
import honq.shared.generated.resources.Res
import honq.shared.generated.resources.loading
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FullscreenLoading(
    modifier: Modifier = Modifier,
    message: String? = null
) {
    val colors = HonqTheme.colors
    val displayMessage = message ?: stringResource(Res.string.loading)

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(HonqSpacing.md)
        ) {
            CircularWavyProgressIndicator(
                color = colors.primary,
                trackColor = colors.surfaceVariant
            )
            Text(
                text = displayMessage,
                color = colors.textMuted,
                fontSize = 14.sp
            )
        }
    }
}
