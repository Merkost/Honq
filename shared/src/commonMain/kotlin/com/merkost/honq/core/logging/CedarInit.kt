package com.merkost.honq.core.logging

import com.merkost.honq.BuildKonfig
import org.kimplify.cedar.logging.Cedar
import org.kimplify.cedar.logging.trees.PlatformLogTree

fun initCedar() {
    if (BuildKonfig.IS_DEBUG) {
        Cedar.plant(PlatformLogTree())
    } else {
        Cedar.plant(CrashReportingTree())
    }
}
