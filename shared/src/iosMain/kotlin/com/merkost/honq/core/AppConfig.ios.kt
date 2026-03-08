package com.merkost.honq.core

import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.Platform

actual object AppConfig {
    @OptIn(ExperimentalNativeApi::class)
    actual val isDebug: Boolean = Platform.isDebugBinary
    actual val revenueCatProductionKey: String = "appl_zNpTbRRENGwsVJChhrpDLZNRTRG"
}
