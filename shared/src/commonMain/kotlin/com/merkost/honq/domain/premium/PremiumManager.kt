package com.merkost.honq.domain.premium

import kotlinx.coroutines.flow.StateFlow

interface PremiumManager {
    val isPremium: StateFlow<Boolean>
    val freeTrialMockTestsRemaining: StateFlow<Int>

    suspend fun consumeFreeMockTest()
    suspend fun restorePurchase(): Result<Boolean>
    suspend fun purchasePro(): Result<Boolean>
    suspend fun syncPremiumStatus()
}
