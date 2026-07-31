package com.merkost.honq.presentation.screens.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.merkost.honq.domain.model.LicenseType
import com.merkost.honq.domain.model.State
import com.merkost.honq.presentation.theme.HonqPreviewTheme
import com.merkost.honq.presentation.theme.HonqSizing
import com.merkost.honq.presentation.theme.HonqSpacing
import com.merkost.honq.presentation.theme.HonqTheme
import honq.shared.generated.resources.Res
import honq.shared.generated.resources.onboarding_setup_summary_license
import honq.shared.generated.resources.onboarding_setup_summary_state
import honq.shared.generated.resources.onboarding_setup_summary_title
import org.jetbrains.compose.resources.stringResource

internal data class SetupSummary(
    val stateName: String,
    val stateCode: String,
    val licenseName: String,
    val licenseCode: String,
)

internal fun createSetupSummary(
    state: State?,
    licenseType: LicenseType?,
): SetupSummary? = if (state == null || licenseType == null) {
    null
} else {
    SetupSummary(state.name, state.shortName, licenseType.name, licenseType.shortName)
}

@Composable
internal fun OnboardingSetupSummaryCard(
    summary: SetupSummary,
    modifier: Modifier = Modifier,
) {
    val colors = HonqTheme.colors

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(HonqSizing.cornerRadius),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = BorderStroke(1.dp, colors.border),
    ) {
        Column(
            modifier = Modifier.padding(HonqSizing.cardPadding),
            verticalArrangement = Arrangement.spacedBy(HonqSpacing.md),
        ) {
            Text(
                text = stringResource(Res.string.onboarding_setup_summary_title),
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary,
            )
            SetupSummaryRow(
                label = stringResource(Res.string.onboarding_setup_summary_state),
                name = summary.stateName,
                code = summary.stateCode,
            )
            SetupSummaryRow(
                label = stringResource(Res.string.onboarding_setup_summary_license),
                name = summary.licenseName,
                code = summary.licenseCode,
            )
        }
    }
}

@Composable
private fun SetupSummaryRow(label: String, name: String, code: String) {
    val colors = HonqTheme.colors

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(HonqSpacing.xs)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = colors.textMuted,
            )
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textPrimary,
            )
        }
        Text(
            text = code,
            style = MaterialTheme.typography.labelLarge,
            color = colors.primary,
        )
    }
}

@Preview
@Composable
private fun OnboardingSetupSummaryPreview() {
    HonqPreviewTheme {
        OnboardingSetupSummaryCard(
            summary = SetupSummary("New South Wales", "NSW", "Car", "C"),
            modifier = Modifier.padding(HonqSizing.screenPadding),
        )
    }
}
