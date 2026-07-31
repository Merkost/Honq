package com.merkost.honq.core.logging

import com.merkost.honq.core.AppConfig
import org.kimplify.cedar.logging.Cedar
import org.kimplify.cedar.logging.trees.platformLogTree

fun initCedar() {
    Cedar.plant(CrashReportingTree())
    if (AppConfig.isDebug) {
        Cedar.plant(platformLogTree())
    }
}
