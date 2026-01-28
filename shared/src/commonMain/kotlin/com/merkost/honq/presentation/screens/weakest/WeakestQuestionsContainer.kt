package com.merkost.honq.presentation.screens.weakest

import com.merkost.honq.core.analytics.Analytics
import com.merkost.honq.core.analytics.AnalyticsEvent
import com.merkost.honq.domain.usecase.GetWeakestQuestionsUseCase
import kotlinx.coroutines.CoroutineScope
import pro.respawn.flowmvi.api.Container
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.plugins.init
import pro.respawn.flowmvi.plugins.reduce

class WeakestQuestionsContainer(
    private val getWeakestQuestions: GetWeakestQuestionsUseCase,
    private val analytics: Analytics,
    scope: CoroutineScope
) : Container<WeakestQuestionsState, WeakestQuestionsIntent, WeakestQuestionsAction> {

    override val store = store(WeakestQuestionsState(), scope) {
        init {
            analytics.track(AnalyticsEvent.ScreenViewed("weakest_questions"))
            val questions = getWeakestQuestions()
            updateState { copy(questions = questions, isLoading = false) }
        }

        reduce { intent ->
            when (intent) {
                WeakestQuestionsIntent.NavigateBack -> action(WeakestQuestionsAction.NavigateBack)
                is WeakestQuestionsIntent.OpenQuestion -> action(WeakestQuestionsAction.NavigateToQuestion(intent.questionId))
            }
        }
    }
}
