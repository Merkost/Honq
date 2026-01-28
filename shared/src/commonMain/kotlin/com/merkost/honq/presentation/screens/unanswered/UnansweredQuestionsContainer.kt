package com.merkost.honq.presentation.screens.unanswered

import com.merkost.honq.core.analytics.Analytics
import com.merkost.honq.core.analytics.AnalyticsEvent
import com.merkost.honq.domain.usecase.GetUnansweredQuestionsUseCase
import kotlinx.coroutines.CoroutineScope
import pro.respawn.flowmvi.api.Container
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.plugins.init
import pro.respawn.flowmvi.plugins.reduce

class UnansweredQuestionsContainer(
    private val getUnansweredQuestions: GetUnansweredQuestionsUseCase,
    private val analytics: Analytics,
    scope: CoroutineScope
) : Container<UnansweredQuestionsState, UnansweredQuestionsIntent, UnansweredQuestionsAction> {

    override val store = store(UnansweredQuestionsState(), scope) {
        init {
            analytics.track(AnalyticsEvent.ScreenViewed("unanswered_questions"))
            val questions = getUnansweredQuestions()
            updateState { copy(questions = questions, isLoading = false) }
        }

        reduce { intent ->
            when (intent) {
                UnansweredQuestionsIntent.NavigateBack -> action(UnansweredQuestionsAction.NavigateBack)
                is UnansweredQuestionsIntent.OpenQuestion -> action(UnansweredQuestionsAction.NavigateToQuestion(intent.questionId))
            }
        }
    }
}
