package com.merkost.honq.core

import com.revenuecat.purchases.kmp.LogLevel
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.PurchasesConfiguration
import org.kimplify.cedar.logging.Cedar

const val REVENUECAT_ENTITLEMENT_ID = "pro"

fun initRevenueCat() {
    Cedar.tag("RevenueCat").d("isDebug=${AppConfig.isDebug}, using key prefix=${AppConfig.revenueCatApiKey.take(10)}...")
    Purchases.logLevel = if (AppConfig.isDebug) LogLevel.DEBUG else LogLevel.ERROR
    Purchases.configure(
        PurchasesConfiguration(apiKey = AppConfig.revenueCatApiKey) {
            diagnosticsEnabled = true
        }
    )
}
