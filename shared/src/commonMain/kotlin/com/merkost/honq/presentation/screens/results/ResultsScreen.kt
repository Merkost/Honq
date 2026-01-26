package com.merkost.honq.presentation.screens.results

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.merkost.honq.presentation.components.base.HonqButton
import com.merkost.honq.presentation.components.base.HonqButtonVariant
import com.merkost.honq.presentation.components.base.HonqCard
import com.merkost.honq.presentation.components.base.HonqScaffold
import com.merkost.honq.presentation.theme.HonqColors
import com.merkost.honq.presentation.theme.HonqSizing
import com.merkost.honq.presentation.theme.HonqSpacing

@Composable
fun ResultsScreen(
    score: Int,
    total: Int,
    onNavigateHome: () -> Unit,
    onRetry: () -> Unit
) {
    val passed = score >= (total * 0.9).toInt()
    val percentage = ((score.toFloat() / total) * 100).toInt()

    HonqScaffold(title = "Results") { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(HonqSizing.screenPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            ResultIcon(passed)
            Spacer(modifier = Modifier.height(HonqSpacing.lg))
            ResultTitle(passed)
            Spacer(modifier = Modifier.height(HonqSpacing.lg))
            ScoreCard(score, total, percentage, passed)
            Spacer(modifier = Modifier.height(HonqSpacing.xxl))
            ActionButtons(onNavigateHome, onRetry)
        }
    }
}

@Composable
private fun ResultIcon(passed: Boolean) {
    val (icon, color) = if (passed) {
        Icons.Default.Check to HonqColors.Correct
    } else {
        Icons.Default.Close to HonqColors.Incorrect
    }

    Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier.size(80.dp),
        tint = color
    )
}

@Composable
private fun ResultTitle(passed: Boolean) {
    val (title, subtitle) = if (passed) {
        "Congratulations!" to "You passed the mock test"
    } else {
        "Keep Practicing" to "You need 90% to pass"
    }

    Text(
        text = title,
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        color = HonqColors.TextPrimary
    )
    Spacer(modifier = Modifier.height(HonqSpacing.sm))
    Text(
        text = subtitle,
        color = HonqColors.TextSecondary,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun ScoreCard(score: Int, total: Int, percentage: Int, passed: Boolean) {
    HonqCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$score/$total",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = if (passed) HonqColors.Correct else HonqColors.Incorrect
            )
            Spacer(modifier = Modifier.height(HonqSpacing.sm))
            Text(
                text = "$percentage%",
                fontSize = 24.sp,
                color = HonqColors.TextSecondary
            )
        }
    }
}

@Composable
private fun ActionButtons(
    onNavigateHome: () -> Unit,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(HonqSpacing.md)
    ) {
        HonqButton(
            text = "Try Again",
            onClick = onRetry
        )
        HonqButton(
            text = "Back to Home",
            onClick = onNavigateHome,
            variant = HonqButtonVariant.Secondary
        )
    }
}
