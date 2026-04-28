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
        if (priority == LogPriority.VERBOSE || priority == LogPriority.DEBUG || priority == LogPriority.INFO) {
            return
        }
        if (priority == LogPriority.ERROR || priority == LogPriority.WARNING) {
            val crashlytics = Firebase.crashlytics
            if (throwable != null) {
                crashlytics.recordException(throwable)
            } else {
                val category = when (priority) {
                    LogPriority.ERROR -> "E"
                    LogPriority.WARNING -> "W"
                }
                crashlytics.log("$category/$tag: $message")
            }
        }
    }
}
