package com.merkost.honq.presentation.screens.onboarding

import com.merkost.honq.core.analytics.Analytics
import com.merkost.honq.core.analytics.AnalyticsEvent
import com.merkost.honq.core.util.Result
import com.merkost.honq.data.local.InMemoryOnboardingPreferences
import com.merkost.honq.data.local.InMemoryQuestionSetSelectionRepository
import com.merkost.honq.data.repository.FakeQuestionRepository
import com.merkost.honq.domain.model.QuestionSet
import com.merkost.honq.domain.repository.QuestionRepository
import com.merkost.honq.domain.usecase.GetQuestionSetsByStateUseCase
import com.merkost.honq.domain.usecase.SetSelectedQuestionSetUseCase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.plugins.reduce
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class OnboardingCompletionTest {

    @Test
    fun duplicate_completion_is_rejected_while_first_lookup_is_suspended_and_fails() = runBlocking {
        val admission = OnboardingCompletionAdmission()
        val firstLookupStarted = CompletableDeferred<Unit>()
        val releaseFirstLookup = CompletableDeferred<Unit>()
        val firstAttemptFinished = CompletableDeferred<Unit>()
        val explicitRetryFinished = CompletableDeferred<Unit>()
        var completionAttempts = 0
        var failedAttempts = 0

        val store = store<OnboardingState, OnboardingIntent, OnboardingAction>(OnboardingState(), this) {
            reduce { intent ->
                if (
                    intent == OnboardingIntent.CompleteOnboarding ||
                    intent == OnboardingIntent.RetryCompletion
                ) {
                    try {
                        completionAttempts += 1
                        if (completionAttempts == 1) {
                            firstLookupStarted.complete(Unit)
                            releaseFirstLookup.await()
                            failedAttempts += 1
                        }
                    } finally {
                        admission.release()
                        if (completionAttempts == 1) {
                            firstAttemptFinished.complete(Unit)
                        } else {
                            explicitRetryFinished.complete(Unit)
                        }
                    }
                }
            }
        }

        fun submitCompletion(intent: OnboardingIntent) {
            if (admission.tryAdmit()) {
                store.intent(intent)
            }
        }

        submitCompletion(OnboardingIntent.CompleteOnboarding)
        firstLookupStarted.await()

        submitCompletion(OnboardingIntent.RetryCompletion)
        assertEquals(1, completionAttempts)
        assertFalse(admission.tryAdmit())

        releaseFirstLookup.complete(Unit)
        firstAttemptFinished.await()

        assertEquals(1, completionAttempts)
        assertEquals(1, failedAttempts)

        submitCompletion(OnboardingIntent.RetryCompletion)
        explicitRetryFinished.await()

        assertEquals(2, completionAttempts)
        assertTrue(admission.tryAdmit())
        admission.release()
        store.closeAndWait()
    }

    @Test
    fun failed_lookup_then_success_tracks_onboarding_completed_exactly_once() = runBlocking {
        val questionSet = QuestionSet(
            id = "nsw_car",
            stateId = "nsw",
            licenseTypeId = "car",
            assessmentTypeId = "knowledge_test",
            mockTestQuestionCount = 45,
            mockTestTimeLimitMinutes = 45,
            mockTestPassPercentage = 80,
        )
        val repository = SequencedQuestionRepository(
            listOf(
                Result.Error(IllegalStateException("offline")),
                Result.Success(listOf(questionSet)),
            )
        )
        val selectionRepository = InMemoryQuestionSetSelectionRepository()
        val preferences = InMemoryOnboardingPreferences()
        val analytics = RecordingAnalytics()
        val handler = OnboardingCompletionHandler(
            getQuestionSetsByState = GetQuestionSetsByStateUseCase(repository),
            setSelectedQuestionSet = SetSelectedQuestionSetUseCase(selectionRepository),
            onboardingPreferences = preferences,
            analytics = analytics,
        )

        val failedResult = handler.complete(stateId = "nsw", licenseTypeId = "car")

        assertTrue(failedResult is Result.Error)
        assertEquals("nsw", preferences.getSelectedStateId())
        assertEquals("car", preferences.getSelectedLicenseTypeId())
        assertNull(preferences.isOnboardingCompleted.value)
        assertEquals(
            emptyList(),
            analytics.events.filterIsInstance<AnalyticsEvent.OnboardingCompleted>(),
        )

        val successfulResult = handler.complete(stateId = "nsw", licenseTypeId = "car")

        assertTrue(successfulResult is Result.Success<*>)
        assertEquals("nsw_car", successfulResult.data)
        assertEquals("nsw_car", selectionRepository.selectedQuestionSetId.value)
        assertEquals(true, preferences.isOnboardingCompleted.value)
        assertEquals(
            listOf(AnalyticsEvent.OnboardingCompleted(stateId = "nsw", licenseTypeId = "car")),
            analytics.events.filterIsInstance<AnalyticsEvent.OnboardingCompleted>(),
        )
    }
}

private class SequencedQuestionRepository(
    results: List<Result<List<QuestionSet>>>,
) : QuestionRepository by FakeQuestionRepository() {
    private val results = results.iterator()

    override suspend fun getQuestionSetsByState(stateId: String): Result<List<QuestionSet>> =
        results.next()
}

private class RecordingAnalytics : Analytics {
    val events = mutableListOf<AnalyticsEvent>()

    override fun track(event: AnalyticsEvent) {
        events += event
    }

    override fun setUserId(userId: String?) = Unit

    override fun setUserProperty(name: String, value: String) = Unit
}
