package com.merkost.honq.presentation.screens.home

import com.merkost.honq.core.util.onError
import com.merkost.honq.core.util.onSuccess
import com.merkost.honq.domain.usecase.GetUserProgressUseCase
import com.merkost.honq.domain.usecase.SyncQuestionsUseCase
import kotlinx.coroutines.CoroutineScope
import pro.respawn.flowmvi.api.Container
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.plugins.reduce
import pro.respawn.flowmvi.plugins.init
import pro.respawn.flowmvi.plugins.whileSubscribed

class HomeContainer(
    private val getUserProgress: GetUserProgressUseCase,
    private val syncQuestions: SyncQuestionsUseCase,
    scope: CoroutineScope
) : Container<HomeState, HomeIntent, HomeAction> {

    override val store = store(HomeState(), scope) {
        init {
            syncInBackground()
        }

        whileSubscribed {
            getUserProgress().collect { progress ->
                updateState { copy(progress = progress) }
            }
        }

        reduce { intent ->
            when (intent) {
                HomeIntent.StartPractice -> action(HomeAction.NavigateToPractice)
                HomeIntent.StartMockTest -> action(HomeAction.NavigateToMockTest)
                HomeIntent.Retry -> syncInBackground()
            }
        }
    }

    private suspend fun PipelineContext<HomeState, HomeIntent, HomeAction>.syncInBackground() {
        updateState { copy(isSyncing = true) }
        syncQuestions()
            .onSuccess { updateState { copy(isSyncing = false, syncError = null) } }
            .onError { e -> updateState { copy(isSyncing = false, syncError = e.message) } }
    }
}
