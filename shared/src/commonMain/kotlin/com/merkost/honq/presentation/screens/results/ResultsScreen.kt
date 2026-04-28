package com.merkost.honq.presentation.screens.results

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.merkost.honq.domain.model.CategoryScore
import com.merkost.honq.presentation.components.base.HonqButton
import com.merkost.honq.presentation.components.base.HonqButtonVariant
import com.merkost.honq.presentation.components.base.HonqCard
import com.merkost.honq.presentation.components.base.HonqScaffold
import com.merkost.honq.presentation.theme.HonqSizing
import com.merkost.honq.presentation.theme.HonqSpacing
import com.merkost.honq.presentation.theme.HonqTheme

@Composable
fun ResultsScreen(
    score: Int,
    total: Int,
    passPercentage: Int,
    categoryBreakdown: List<CategoryScore>,
    hasIncorrect: Boolean,
    onNavigateHome: () -> Unit,
    onRetry: () -> Unit,
    onReviewIncorrect: () -> Unit
) {
    val percentage = if (total > 0) ((score.toFloat() / total) * 100).toInt() else 0
    val passed = percentage >= passPercentage

    HonqScaffold(title = "Results") { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(HonqSizing.screenPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(HonqSpacing.xl))
            ResultIcon(passed)
            Spacer(modifier = Modifier.height(HonqSpacing.lg))
            ResultTitle(passed, passPercentage)
            Spacer(modifier = Modifier.height(HonqSpacing.lg))
            ScoreCard(score, total, percentage, passed)
            if (categoryBreakdown.isNotEmpty()) {
                Spacer(modifier = Modifier.height(HonqSpacing.lg))
                CategoryBreakdownSection(categoryBreakdown)
            }
            Spacer(modifier = Modifier.height(HonqSpacing.xxl))
            ActionButtons(
                hasIncorrect = hasIncorrect,
                onNavigateHome = onNavigateHome,
                onRetry = onRetry,
                onReviewIncorrect = onReviewIncorrect
            )
            Spacer(modifier = Modifier.height(HonqSpacing.lg))
        }
    }
}

@Composable
private fun ResultIcon(passed: Boolean) {
    val colors = HonqTheme.colors
    val (icon, color) = if (passed) {
        Icons.Default.Check to colors.correct
    } else {
        Icons.Default.Close to colors.incorrect
    }

    Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier.size(80.dp),
        tint = color
    )
}

@Composable
private fun ResultTitle(passed: Boolean, passPercentage: Int) {
    val colors = HonqTheme.colors
    val (title, subtitle) = if (passed) {
        "Congratulations!" to "You passed the mock test"
    } else {
        "Keep Practicing" to "You need $passPercentage% to pass"
    }

    Text(
        text = title,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = colors.textPrimary
    )
    Spacer(modifier = Modifier.height(HonqSpacing.sm))
    Text(
        text = subtitle,
        style = MaterialTheme.typography.bodyMedium,
        color = colors.textSecondary,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun ScoreCard(score: Int, total: Int, percentage: Int, passed: Boolean) {
    val colors = HonqTheme.colors
    HonqCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$score/$total",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = if (passed) colors.correct else colors.incorrect
            )
            Spacer(modifier = Modifier.height(HonqSpacing.sm))
            Text(
                text = "$percentage%",
                style = MaterialTheme.typography.headlineSmall,
                color = colors.textSecondary
            )
        }
    }
}

@Composable
private fun CategoryBreakdownSection(categoryBreakdown: List<CategoryScore>) {
    val colors = HonqTheme.colors
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(HonqSpacing.sm)
    ) {
        Text(
            text = "Category Breakdown",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = colors.textPrimary
        )
        Spacer(modifier = Modifier.height(HonqSpacing.xs))
        categoryBreakdown.forEach { category ->
            CategoryScoreRow(category)
        }
    }
}

@Composable
private fun CategoryScoreRow(category: CategoryScore) {
    val colors = HonqTheme.colors
    val fraction = if (category.total > 0) category.correct.toFloat() / category.total else 0f
    val color = when {
        category.percentage >= 80 -> colors.correct
        category.percentage >= 50 -> colors.warning
        else -> colors.incorrect
    }

    HonqCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(HonqSpacing.xs)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = category.categoryName,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${category.correct}/${category.total} (${category.percentage}%)",
                    style = MaterialTheme.typography.labelMedium,
                    color = color,
                    fontWeight = FontWeight.Medium
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(HonqSizing.progressBarHeightMedium)
                    .clip(RoundedCornerShape(HonqSizing.progressBarHeightMedium / 2))
                    .background(colors.progressTrack)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .height(HonqSizing.progressBarHeightMedium)
                        .clip(RoundedCornerShape(HonqSizing.progressBarHeightMedium / 2))
                        .background(color)
                )
            }
        }
    }
}

@Composable
private fun ActionButtons(
    hasIncorrect: Boolean,
    onNavigateHome: () -> Unit,
    onRetry: () -> Unit,
    onReviewIncorrect: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(HonqSpacing.md)
    ) {
        if (hasIncorrect) {
            HonqButton(
                text = "Review Incorrect Answers",
                onClick = onReviewIncorrect
            )
        }
        HonqButton(
            text = "Try Again",
            onClick = onRetry,
            variant = if (hasIncorrect) HonqButtonVariant.Secondary else HonqButtonVariant.Primary
        )
        HonqButton(
            text = "Back to Home",
            onClick = onNavigateHome,
            variant = HonqButtonVariant.Text
        )
    }
}
