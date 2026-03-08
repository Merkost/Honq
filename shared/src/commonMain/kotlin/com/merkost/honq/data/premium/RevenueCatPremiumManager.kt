package com.merkost.honq.data.premium

import com.merkost.honq.core.REVENUECAT_ENTITLEMENT_ID
import com.merkost.honq.data.local.FREE_MOCK_TEST_LIMIT
import com.merkost.honq.data.local.PremiumPreferences
import com.merkost.honq.domain.premium.PremiumManager
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.PurchasesDelegate
import com.revenuecat.purchases.kmp.ktx.awaitCustomerInfo
import com.revenuecat.purchases.kmp.ktx.awaitOfferings
import com.revenuecat.purchases.kmp.ktx.awaitPurchase
import com.revenuecat.purchases.kmp.ktx.awaitRestore
import com.revenuecat.purchases.kmp.models.CustomerInfo
import com.revenuecat.purchases.kmp.models.PurchasesError
import com.revenuecat.purchases.kmp.models.StoreProduct
import com.revenuecat.purchases.kmp.models.StoreTransaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.kimplify.cedar.logging.Cedar

class RevenueCatPremiumManager(
    private val premiumPreferences: PremiumPreferences
) : PremiumManager {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    override val isPremium: StateFlow<Boolean> = premiumPreferences.isPremiumPurchased
        .stateIn(scope, SharingStarted.Eagerly, false)

    override val freeTrialMockTestsRemaining: StateFlow<Int> =
        premiumPreferences.freeMockTestsUsed
            .map { used -> (FREE_MOCK_TEST_LIMIT - used).coerceAtLeast(0) }
            .stateIn(scope, SharingStarted.Eagerly, FREE_MOCK_TEST_LIMIT)

    init {
        setupDelegate()
        scope.launch { syncEntitlementStatus() }
    }

    private fun setupDelegate() {
        Purchases.sharedInstance.delegate = object : PurchasesDelegate {
            override fun onPurchasePromoProduct(
                product: StoreProduct,
                startPurchase: (
                    onError: (error: PurchasesError, userCancelled: Boolean) -> Unit,
                    onSuccess: (storeTransaction: StoreTransaction, customerInfo: CustomerInfo) -> Unit
                ) -> Unit
            ) {
                startPurchase(
                    { error, _ -> Cedar.tag("Premium").e("Promo purchase failed: ${error.message}") },
                    { _, customerInfo -> onCustomerInfoUpdated(customerInfo) }
                )
            }

            override fun onCustomerInfoUpdated(customerInfo: CustomerInfo) {
                val isPro = customerInfo.entitlements[REVENUECAT_ENTITLEMENT_ID]?.isActive == true
                Cedar.tag("Premium").d("onCustomerInfoUpdated: isPro=$isPro")
                scope.launch { premiumPreferences.setPremiumPurchased(isPro) }
            }
        }
    }

    override suspend fun consumeFreeMockTest() {
        premiumPreferences.incrementFreeMockTestsUsed()
    }

    override suspend fun restorePurchase(): Result<Boolean> {
        return try {
            val customerInfo = Purchases.sharedInstance.awaitRestore()
            val isPro = customerInfo.entitlements[REVENUECAT_ENTITLEMENT_ID]?.isActive == true
            premiumPreferences.setPremiumPurchased(isPro)
            Cedar.tag("Premium").d("restorePurchase: isPro=$isPro")
            Result.success(isPro)
        } catch (e: Exception) {
            Cedar.tag("Premium").e("restorePurchase failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun purchasePro(): Result<Boolean> {
        return try {
            val offerings = Purchases.sharedInstance.awaitOfferings()
            val pkg = offerings.current?.lifetime
                ?: offerings.current?.availablePackages?.firstOrNull()
                ?: return Result.failure(Exception("No offering available"))

            val purchaseResult = Purchases.sharedInstance.awaitPurchase(packageToPurchase = pkg)
            val isPro = purchaseResult.customerInfo.entitlements[REVENUECAT_ENTITLEMENT_ID]?.isActive == true
            premiumPreferences.setPremiumPurchased(isPro)
            Cedar.tag("Premium").d("purchasePro: isPro=$isPro")
            Result.success(isPro)
        } catch (e: Exception) {
            Cedar.tag("Premium").e("purchasePro failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun syncPremiumStatus() {
        syncEntitlementStatus()
    }

    private suspend fun syncEntitlementStatus() {
        try {
            val customerInfo = Purchases.sharedInstance.awaitCustomerInfo()
            val isPro = customerInfo.entitlements[REVENUECAT_ENTITLEMENT_ID]?.isActive == true
            premiumPreferences.setPremiumPurchased(isPro)
            Cedar.tag("Premium").d("syncEntitlementStatus: isPro=$isPro")
        } catch (e: Exception) {
            Cedar.tag("Premium").e("syncEntitlementStatus failed: ${e.message}", e)
        }
    }
}
