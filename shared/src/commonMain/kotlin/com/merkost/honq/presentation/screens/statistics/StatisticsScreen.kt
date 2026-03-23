package com.merkost.honq.presentation.screens.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.merkost.honq.domain.model.MockTestResult
import com.merkost.honq.presentation.components.base.FullscreenLoading
import com.merkost.honq.presentation.components.base.HonqCard
import com.merkost.honq.presentation.components.base.HonqScaffold
import com.merkost.honq.presentation.components.charts.LineChart
import com.merkost.honq.presentation.components.charts.LineChartData
import com.merkost.honq.presentation.components.charts.ProgressRing
import com.merkost.honq.presentation.theme.HonqChartSizing
import com.merkost.honq.presentation.theme.HonqSizing
import com.merkost.honq.presentation.theme.HonqSpacing
import com.merkost.honq.presentation.theme.HonqTheme
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import pro.respawn.flowmvi.compose.dsl.subscribe

@Composable
fun StatisticsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToWeakestQuestions: () -> Unit = {},
    onNavigateToUnansweredQuestions: () -> Unit = {},
    onNavigateToMockTestReview: (Long) -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val container = koinInject<StatisticsContainer> { parametersOf(scope) }

    val state by container.store.subscribe { action ->
        when (action) {
            StatisticsAction.NavigateBack -> onNavigateBack()
            StatisticsAction.NavigateToWeakestQuestions -> onNavigateToWeakestQuestions()
            StatisticsAction.NavigateToUnansweredQuestions -> onNavigateToUnansweredQuestions()
            is StatisticsAction.NavigateToMockTestReview -> onNavigateToMockTestReview(action.mockTestResultId)
        }
    }

    StatisticsContent(
        state = state,
        onNavigateBack = onNavigateBack,
        onIntent = container.store::intent
    )
}

@Composable
private fun StatisticsContent(
    state: StatisticsState,
    onNavigateBack: () -> Unit,
    onIntent: (StatisticsIntent) -> Unit
) {
    val colors = HonqTheme.colors

    HonqScaffold(
        title = "Statistics",
        onNavigateBack = onNavigateBack
    ) { padding ->
        when {
            state.isLoading -> {
                FullscreenLoading()
            }
            !state.hasData -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(HonqSizing.screenPadding)
                    ) {
                        Text(
                            text = "No statistics yet",
                            color = colors.textSecondary,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(HonqSpacing.sm))
                        Text(
                            text = "Start practicing to see your progress here!",
                            color = colors.textMuted,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(HonqSizing.screenPadding),
                    verticalArrangement = Arrangement.spacedBy(HonqSpacing.lg)
                ) {
                    OverviewSection(state)
                    PracticeAccuracySection(state)
                    MockTestPerformanceSection(state, onIntent)
                    InsightsSection(state, onIntent)
                    Spacer(modifier = Modifier.height(HonqSpacing.md))
                }
            }
        }
    }
}

@Composable
private fun OverviewSection(state: StatisticsState) {
    val colors = HonqTheme.colors

    HonqCard {
        Text(
            text = "Overview",
            color = colors.textSecondary,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(HonqSpacing.md))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ProgressRing(
                    progress = state.progress.completionProgress,
                    modifier = Modifier.size(HonqChartSizing.ringSize),
                    progressColor = colors.primary,
                    centerText = "${(state.progress.completionProgress * 100).toInt()}%",
                    centerSubtext = "Complete"
                )
                Spacer(modifier = Modifier.height(HonqSpacing.sm))
                Text(
                    text = "${state.progress.uniqueQuestionsAnswered}/${state.progress.totalQuestions}",
                    color = colors.textMuted,
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    text = "Questions seen",
                    color = colors.textMuted,
                    style = MaterialTheme.typography.labelSmall
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val accuracyColor = when {
                    state.progress.practiceAccuracy >= 0.9f -> colors.correct
                    state.progress.practiceAccuracy >= 0.75f -> colors.primary
                    state.progress.practiceAccuracy >= 0.5f -> colors.warning
                    else -> colors.incorrect
                }
                ProgressRing(
                    progress = state.progress.practiceAccuracy,
                    modifier = Modifier.size(HonqChartSizing.ringSize),
                    progressColor = accuracyColor,
                    centerText = "${(state.progress.practiceAccuracy * 100).toInt()}%",
                    centerSubtext = "Accuracy"
                )
                Spacer(modifier = Modifier.height(HonqSpacing.sm))
                Text(
                    text = "${state.progress.correctAnswers}/${state.progress.totalPracticed}",
                    color = colors.textMuted,
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    text = "Correct answers",
                    color = colors.textMuted,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
private fun PracticeAccuracySection(state: StatisticsState) {
    val colors = HonqTheme.colors

    HonqCard {
        Text(
            text = "Practice Summary",
            color = colors.textSecondary,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(HonqSpacing.md))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                value = state.progress.totalPracticed.toString(),
                label = "Total Practiced",
                color = colors.primary
            )
            StatItem(
                value = state.progress.correctAnswers.toString(),
                label = "Correct",
                color = colors.correct
            )
            StatItem(
                value = (state.progress.totalPracticed - state.progress.correctAnswers).toString(),
                label = "Incorrect",
                color = colors.incorrect
            )
        }

    }
}

@Composable
private fun MockTestPerformanceSection(state: StatisticsState, onIntent: (StatisticsIntent) -> Unit) {
    val colors = HonqTheme.colors

    HonqCard {
        Text(
            text = "Mock Test Performance",
            color = colors.textSecondary,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(HonqSpacing.md))

        if (state.mockTestResults.isEmpty()) {
            Text(
                text = "Take a mock test to track your performance",
                color = colors.textMuted,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(vertical = HonqSpacing.md)
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    value = state.progress.mockTestsTaken.toString(),
                    label = "Tests Taken",
                    color = colors.primary
                )
                StatItem(
                    value = state.progress.mockTestsPassed.toString(),
                    label = "Passed",
                    color = colors.correct
                )
                StatItem(
                    value = "${state.averageMockTestScore}%",
                    label = "Avg Score",
                    color = if (state.averageMockTestScore >= 90) colors.correct else colors.primary
                )
            }

            if (state.recentMockTests.size >= 2) {
                Spacer(modifier = Modifier.height(HonqSpacing.lg))

                Text(
                    text = "Recent Test Scores",
                    color = colors.textMuted,
                    style = MaterialTheme.typography.labelSmall
                )
                Spacer(modifier = Modifier.height(HonqSpacing.sm))

                val chartData = state.recentMockTests
                    .reversed()
                    .mapIndexed { index, result ->
                        LineChartData(
                            label = formatTestDate(result),
                            value = result.scorePercentage.toFloat()
                        )
                    }

                LineChart(
                    data = chartData,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    lineColor = colors.primary,
                    fillColor = colors.primarySurface
                )

                Spacer(modifier = Modifier.height(HonqSpacing.xs))
                Text(
                    text = "90% = passing score",
                    color = colors.textMuted,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End
                )
            }

            if (state.recentMockTests.isNotEmpty()) {
                Spacer(modifier = Modifier.height(HonqSpacing.lg))

                Text(
                    text = "Recent Tests",
                    color = colors.textMuted,
                    style = MaterialTheme.typography.labelSmall
                )
                Spacer(modifier = Modifier.height(HonqSpacing.sm))

                Column(
                    verticalArrangement = Arrangement.spacedBy(HonqSpacing.xs)
                ) {
                    state.recentMockTests.take(5).forEach { result ->
                        MockTestResultItem(
                            result = result,
                            onClick = { onIntent(StatisticsIntent.OpenMockTestReview(result.id)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MockTestResultItem(result: MockTestResult, onClick: () -> Unit) {
    val colors = HonqTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(HonqSizing.cornerRadiusSmall))
            .background(color = colors.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = HonqSpacing.md, vertical = HonqSpacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = formatTestDateFull(result),
                color = colors.textPrimary,
                style = MaterialTheme.typography.labelMedium
            )
            Text(
                text = "${result.correctAnswers}/${result.totalQuestions} correct",
                color = colors.textMuted,
                style = MaterialTheme.typography.labelSmall
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${result.scorePercentage}%",
                color = if (result.passed) colors.correct else colors.incorrect,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (result.passed) "PASSED" else "FAILED",
                color = if (result.passed) colors.correct else colors.incorrect,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun InsightsSection(
    state: StatisticsState,
    onIntent: (StatisticsIntent) -> Unit
) {
    val colors = HonqTheme.colors

    Column(verticalArrangement = Arrangement.spacedBy(HonqSpacing.sm)) {
        Text(
            text = "Insights",
            color = colors.textSecondary,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )

        if (state.weakestQuestionCount > 0) {
            HonqCard(onClick = { onIntent(StatisticsIntent.OpenWeakestQuestions) }) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Weakest Questions",
                            color = colors.textPrimary,
                            fontWeight = FontWeight.Medium,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Spacer(modifier = Modifier.height(HonqSpacing.xs))
                        Text(
                            text = "${state.weakestQuestionCount} questions you got wrong",
                            color = colors.textMuted,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = "View All",
                        tint = colors.textMuted
                    )
                }
            }
        }

        HonqCard(onClick = { onIntent(StatisticsIntent.OpenUnansweredQuestions) }) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Unanswered Questions",
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Medium,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(HonqSpacing.xs))
                    Text(
                        text = "${state.unansweredQuestionCount} questions not yet attempted",
                        color = colors.textMuted,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = "View All",
                    tint = colors.textMuted
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    value: String,
    label: String,
    color: androidx.compose.ui.graphics.Color
) {
    val colors = HonqTheme.colors

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            color = color,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = colors.textMuted,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

private fun formatTestDate(result: MockTestResult): String {
    val localDateTime = result.completedAt.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${localDateTime.dayOfMonth}/${localDateTime.monthNumber}"
}

private fun formatTestDateFull(result: MockTestResult): String {
    val localDateTime = result.completedAt.toLocalDateTime(TimeZone.currentSystemDefault())
    val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    return "${localDateTime.dayOfMonth} ${months[localDateTime.monthNumber - 1]} ${localDateTime.year}"
}
