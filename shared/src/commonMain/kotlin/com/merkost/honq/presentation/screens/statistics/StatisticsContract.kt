package com.merkost.honq.presentation.screens.statistics

import com.merkost.honq.domain.model.MockTestResult
import com.merkost.honq.domain.model.UserProgress
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState

data class StatisticsState(
    val progress: UserProgress = UserProgress.EMPTY,
    val mockTestResults: List<MockTestResult> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
) : MVIState {
    val hasData: Boolean
        get() = progress.totalPracticed > 0 || mockTestResults.isNotEmpty()

    val recentMockTests: List<MockTestResult>
        get() = mockTestResults.take(10)

    val averageMockTestScore: Int
        get() = if (mockTestResults.isEmpty()) 0
        else mockTestResults.sumOf { it.scorePercentage } / mockTestResults.size
}

sealed interface StatisticsIntent : MVIIntent {
    data object Exit : StatisticsIntent
}

sealed interface StatisticsAction : MVIAction {
    data object NavigateBack : StatisticsAction
}
