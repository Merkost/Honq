package com.merkost.honq.presentation.screens.statistics

import com.merkost.honq.core.analytics.Analytics
import com.merkost.honq.core.analytics.AnalyticsEvent
import com.merkost.honq.domain.usecase.GetStatisticsUseCase
import kotlinx.coroutines.CoroutineScope
import pro.respawn.flowmvi.api.Container
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.plugins.init
import pro.respawn.flowmvi.plugins.reduce
import pro.respawn.flowmvi.plugins.whileSubscribed

class StatisticsContainer(
    private val getStatistics: GetStatisticsUseCase,
    private val analytics: Analytics,
    scope: CoroutineScope
) : Container<StatisticsState, StatisticsIntent, StatisticsAction> {

    override val store = store(StatisticsState(), scope) {
        init {
            analytics.track(AnalyticsEvent.ScreenViewed("statistics"))
        }

        whileSubscribed {
            getStatistics().collect { statistics ->
                updateState {
                    copy(
                        progress = statistics.progress,
                        mockTestResults = statistics.mockTestResults,
                        isLoading = false
                    )
                }
            }
        }

        reduce { intent ->
            when (intent) {
                StatisticsIntent.Exit -> action(StatisticsAction.NavigateBack)
            }
        }
    }
}
