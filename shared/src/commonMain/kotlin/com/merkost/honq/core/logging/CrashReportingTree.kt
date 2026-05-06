package com.merkost.honq.core.logging

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.crashlytics.crashlytics
import org.kimplify.cedar.logging.LogPriority
import org.kimplify.cedar.logging.LogTree

class CrashReportingTree : LogTree {
    override fun log(
        priority: LogPriority,
        tag: String,
        message: String,
        throwable: Throwable?
    ) {
        if (priority == LogPriority.VERBOSE || priority == LogPriority.DEBUG) return

        val crashlytics = Firebase.crashlytics
        when (priority) {
            LogPriority.INFO -> crashlytics.log("I/$tag: $message")
            LogPriority.WARNING -> {
                if (throwable != null) crashlytics.recordException(throwable)
                else crashlytics.log("W/$tag: $message")
            }
            LogPriority.ERROR -> {
                if (throwable != null) crashlytics.recordException(throwable)
                else crashlytics.log("E/$tag: $message")
            }
            else -> Unit // VERBOSE/DEBUG already returned above
        }
    }
}
