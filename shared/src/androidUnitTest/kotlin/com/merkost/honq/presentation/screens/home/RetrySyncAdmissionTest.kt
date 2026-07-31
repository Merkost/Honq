package com.merkost.honq.presentation.screens.home

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.plugins.reduce

class RetrySyncAdmissionTest {

    @Test
    fun duplicate_retry_intents_are_rejected_while_the_first_retry_is_suspended() = runBlocking {
        val admission = RetrySyncAdmission()
        val firstRetryStarted = CompletableDeferred<Unit>()
        val releaseFirstRetry = CompletableDeferred<Unit>()
        val firstRetryFinished = CompletableDeferred<Unit>()
        var admittedRetries = 0

        val store = store<HomeState, HomeIntent, HomeAction>(HomeState(), this) {
            reduce { intent ->
                if (intent == HomeIntent.RetrySync) {
                    admittedRetries += 1
                    firstRetryStarted.complete(Unit)
                    releaseFirstRetry.await()
                    admission.release()
                    firstRetryFinished.complete(Unit)
                }
            }
        }

        fun submitRetry() {
            if (admission.tryAdmit()) {
                store.intent(HomeIntent.RetrySync)
            }
        }

        submitRetry()
        firstRetryStarted.await()

        submitRetry()
        assertEquals(1, admittedRetries)
        assertFalse(admission.tryAdmit())

        releaseFirstRetry.complete(Unit)
        firstRetryFinished.await()

        assertTrue(admission.tryAdmit())
        admission.release()
        store.closeAndWait()
    }
}
