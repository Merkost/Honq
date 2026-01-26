package com.merkost.honq.presentation.screens.statistics

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.merkost.honq.domain.model.MockTestResult
import com.merkost.honq.presentation.components.base.FullscreenLoading
import com.merkost.honq.presentation.components.base.HonqCard
import com.merkost.honq.presentation.components.base.HonqScaffold
import com.merkost.honq.presentation.components.charts.BarChart
import com.merkost.honq.presentation.components.charts.BarChartData
import com.merkost.honq.presentation.components.charts.DonutChartSegment
import com.merkost.honq.presentation.components.charts.LineChart
import com.merkost.honq.presentation.components.charts.LineChartData
import com.merkost.honq.presentation.components.charts.ProgressRing
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
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val container = koinInject<StatisticsContainer> { parametersOf(scope) }

    val state by container.store.subscribe { action ->
        when (action) {
            StatisticsAction.NavigateBack -> onNavigateBack()
        }
    }

    StatisticsContent(
        state = state,
        onNavigateBack = onNavigateBack
    )
}

@Composable
private fun StatisticsContent(
    state: StatisticsState,
    onNavigateBack: () -> Unit
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
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(HonqSpacing.sm))
                        Text(
                            text = "Start practicing to see your progress here!",
                            color = colors.textMuted,
                            fontSize = 14.sp,
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
                    if (state.mockTestResults.isNotEmpty()) {
                        MockTestPerformanceSection(state)
                    }
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
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(HonqSpacing.md))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Completion Progress
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ProgressRing(
                    progress = state.progress.completionProgress,
                    modifier = Modifier.size(100.dp),
                    progressColor = colors.primary,
                    centerText = "${(state.progress.completionProgress * 100).toInt()}%",
                    centerSubtext = "Complete"
                )
                Spacer(modifier = Modifier.height(HonqSpacing.sm))
                Text(
                    text = "${state.progress.uniqueQuestionsAnswered}/${state.progress.totalQuestions}",
                    color = colors.textMuted,
                    fontSize = 12.sp
                )
                Text(
                    text = "Questions seen",
                    color = colors.textMuted,
                    fontSize = 11.sp
                )
            }

            // Practice Accuracy
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
                    modifier = Modifier.size(100.dp),
                    progressColor = accuracyColor,
                    centerText = "${(state.progress.practiceAccuracy * 100).toInt()}%",
                    centerSubtext = "Accuracy"
                )
                Spacer(modifier = Modifier.height(HonqSpacing.sm))
                Text(
                    text = "${state.progress.correctAnswers}/${state.progress.totalPracticed}",
                    color = colors.textMuted,
                    fontSize = 12.sp
                )
                Text(
                    text = "Correct answers",
                    color = colors.textMuted,
                    fontSize = 11.sp
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
            fontSize = 14.sp,
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

        Spacer(modifier = Modifier.height(HonqSpacing.lg))

        // Accuracy breakdown bar
        val correct = state.progress.correctAnswers
        val incorrect = state.progress.totalPracticed - state.progress.correctAnswers

        if (state.progress.totalPracticed > 0) {
            Text(
                text = "Accuracy Breakdown",
                color = colors.textMuted,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(HonqSpacing.sm))

            val barData = listOf(
                BarChartData("Correct", correct.toFloat(), state.progress.totalPracticed.toFloat()),
                BarChartData("Incorrect", incorrect.toFloat(), state.progress.totalPracticed.toFloat())
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(HonqSpacing.md)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ProgressRing(
                        progress = state.progress.practiceAccuracy,
                        modifier = Modifier.size(80.dp),
                        progressColor = colors.correct,
                        strokeWidth = 10.dp,
                        centerText = "$correct",
                        centerSubtext = "Correct"
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ProgressRing(
                        progress = 1f - state.progress.practiceAccuracy,
                        modifier = Modifier.size(80.dp),
                        progressColor = colors.incorrect,
                        strokeWidth = 10.dp,
                        centerText = "$incorrect",
                        centerSubtext = "Incorrect"
                    )
                }
            }
        }
    }
}

@Composable
private fun MockTestPerformanceSection(state: StatisticsState) {
    val colors = HonqTheme.colors

    HonqCard {
        Text(
            text = "Mock Test Performance",
            color = colors.textSecondary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(HonqSpacing.md))

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
                fontSize = 12.sp
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

            // Pass/Fail threshold line label
            Spacer(modifier = Modifier.height(HonqSpacing.xs))
            Text(
                text = "90% = passing score",
                color = colors.textMuted,
                fontSize = 10.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )
        }

        // Recent tests list
        if (state.recentMockTests.isNotEmpty()) {
            Spacer(modifier = Modifier.height(HonqSpacing.lg))

            Text(
                text = "Recent Tests",
                color = colors.textMuted,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(HonqSpacing.sm))

            state.recentMockTests.take(5).forEach { result ->
                MockTestResultItem(result)
                Spacer(modifier = Modifier.height(HonqSpacing.xs))
            }
        }
    }
}

@Composable
private fun MockTestResultItem(result: MockTestResult) {
    val colors = HonqTheme.colors

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = formatTestDateFull(result),
                color = colors.textPrimary,
                fontSize = 13.sp
            )
            Text(
                text = "${result.correctAnswers}/${result.totalQuestions} correct",
                color = colors.textMuted,
                fontSize = 11.sp
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${result.scorePercentage}%",
                color = if (result.passed) colors.correct else colors.incorrect,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (result.passed) "PASSED" else "FAILED",
                color = if (result.passed) colors.correct else colors.incorrect,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
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
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = colors.textMuted,
            fontSize = 11.sp
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
