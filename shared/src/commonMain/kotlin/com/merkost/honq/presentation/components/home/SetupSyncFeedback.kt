package com.merkost.honq.presentation.components.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.merkost.honq.presentation.theme.HonqPreviewTheme
import com.merkost.honq.presentation.theme.HonqSizing
import com.merkost.honq.presentation.theme.HonqSpacing
import com.merkost.honq.presentation.theme.HonqTheme
import honq.shared.generated.resources.Res
import honq.shared.generated.resources.home_sync_error_retry
import honq.shared.generated.resources.home_sync_error_subtitle
import honq.shared.generated.resources.home_sync_error_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun SetupSyncFeedback(
    modifier: Modifier = Modifier,
    onRetry: () -> Unit,
    enabled: Boolean = true,
) {
    val colors = HonqTheme.colors

    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(containerColor = colors.incorrectSurface),
    ) {
        Column(
            modifier = Modifier.padding(HonqSizing.cardPadding),
            verticalArrangement = Arrangement.spacedBy(HonqSpacing.xs),
        ) {
            Text(
                text = stringResource(Res.string.home_sync_error_title),
                style = MaterialTheme.typography.titleSmall,
                color = colors.incorrect,
            )
            Text(
                text = stringResource(Res.string.home_sync_error_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
            )
            TextButton(
                enabled = enabled,
                onClick = onRetry,
            ) {
                Text(text = stringResource(Res.string.home_sync_error_retry))
            }
        }
    }
}

@Preview
@Composable
private fun SetupSyncFeedbackPreview() {
    HonqPreviewTheme {
        SetupSyncFeedback(onRetry = {})
    }
}
