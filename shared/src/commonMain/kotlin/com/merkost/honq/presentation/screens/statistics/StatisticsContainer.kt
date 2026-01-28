package com.merkost.honq.presentation.screens.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.merkost.honq.core.analytics.Analytics
import com.merkost.honq.core.analytics.AnalyticsEvent
import com.merkost.honq.domain.usecase.GetStatisticsUseCase
import kotlinx.coroutines.CoroutineScope
import org.kimplify.cedar.logging.Cedar
import pro.respawn.flowmvi.api.Container
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.plugins.init
import pro.respawn.flowmvi.plugins.reduce
import pro.respawn.flowmvi.plugins.whileSubscribed

class StatisticsContainer(
    private val getStatistics: GetStatisticsUseCase,
    private val analytics: Analytics,
) : Container<StatisticsState, StatisticsIntent, StatisticsAction>, ViewModel() {

    override val store = store(StatisticsState(), viewModelScope) {
        init {
            analytics.track(AnalyticsEvent.ScreenViewed("statistics"))
        }

        whileSubscribed {
            getStatistics().collect { statistics ->
                Cedar.tag("Statistics").d(
                    "Statistics updated: mockTests=${statistics.mockTestResults.size}, " +
                        "weakest=${statistics.weakestQuestionCount}, unanswered=${statistics.unansweredQuestionCount}"
                )
                updateState {
                    copy(
                        progress = statistics.progress,
                        mockTestResults = statistics.mockTestResults,
                        weakestQuestionCount = statistics.weakestQuestionCount,
                        unansweredQuestionCount = statistics.unansweredQuestionCount,
                        isLoading = false
                    )
                }
            }
        }

        reduce { intent ->
            when (intent) {
                StatisticsIntent.Exit -> action(StatisticsAction.NavigateBack)
                StatisticsIntent.OpenWeakestQuestions -> action(StatisticsAction.NavigateToWeakestQuestions)
                StatisticsIntent.OpenUnansweredQuestions -> action(StatisticsAction.NavigateToUnansweredQuestions)
                is StatisticsIntent.OpenMockTestReview -> action(StatisticsAction.NavigateToMockTestReview(intent.mockTestResultId))
            }
        }
    }
}
