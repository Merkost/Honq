package com.merkost.honq.core

private const val REVENUECAT_TEST_KEY = "test_bfehbZvMHUTCQrKkwwbkLXzKIDM"

expect object AppConfig {
    val isDebug: Boolean
    val revenueCatProductionKey: String
}

val AppConfig.revenueCatApiKey: String
    get() = if (isDebug) REVENUECAT_TEST_KEY else revenueCatProductionKey
