package com.merkost.honq.core

import com.merkost.honq.BuildKonfig

actual object AppConfig {
    actual val isDebug: Boolean = BuildKonfig.IS_DEBUG
    actual val revenueCatProductionKey: String = "goog_IKnFzbNFzzsbJgxPbzspOoORlhe"
}
