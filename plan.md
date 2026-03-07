# Freemium Implementation Plan + Code Fixes

## Overview

Convert Honq from a paid app to **freemium** with a one-time $4.99 "Honq Pro" unlock.

### Free Tier
- **Random practice only** — unlimited, no category filtering
- **1 free mock test** — lets users experience the full test flow
- **Basic stats** on home screen (accuracy, mock tests passed) — already visible
- **Search & Favorites** — unrestricted

### Premium ($4.99 one-time — "Honq Pro")
- **Unlimited mock tests**
- **Practice by category** — choose which topic to focus on
- **Smart Practice** (spaced repetition)
- **Full Statistics dashboard** (detailed stats, history, category breakdown)
- **Weakest Questions** view
- **Unanswered Questions** view

---

## Implementation Steps

### Phase 1: Premium State Management (shared/commonMain)

**Step 1.1: Create `PremiumManager` interface**
- File: `shared/.../domain/premium/PremiumManager.kt`
- Interface with:
  - `val isPremium: StateFlow<Boolean>` — reactive premium status
  - `val freeTrialMockTestsRemaining: StateFlow<Int>` — tracks free mock test quota (starts at 1)
  - `suspend fun consumeFreeMockTest()` — decrements the counter after a free test
  - `suspend fun restorePurchase()` — for restore functionality
- This lives in `domain` layer, no platform deps

**Step 1.2: Create `PremiumPreferences` for local persistence**
- File: `shared/.../data/local/PremiumPreferences.kt`
- DataStore-backed preferences storing:
  - `isPremiumPurchased: Boolean` (persists purchase state)
  - `freeMockTestsUsed: Int` (tracks how many free tests taken)
- Similar pattern to existing `OnboardingPreferences`, `ThemePreferences`

**Step 1.3: Create `PremiumManagerImpl`**
- File: `shared/.../data/premium/PremiumManagerImpl.kt`
- Implements `PremiumManager`
- Combines DataStore state with platform billing verification
- Logic: `isPremium = isPremiumPurchased || platformBillingConfirmed`
- Free mock test logic: user gets 1 free, after that `freeTrialMockTestsRemaining = 0`

**Step 1.4: Register in DI**
- Add `PremiumPreferences` to `DataModule.kt`
- Add `PremiumManager` singleton to `DataModule.kt`

### Phase 2: Platform Billing Integration

**Step 2.1: Create `expect/actual BillingService`**
- File: `shared/.../data/premium/BillingService.kt` (expect)
- Interface:
  - `suspend fun purchase(productId: String): PurchaseResult`
  - `suspend fun restorePurchases(): Boolean`
  - `fun isAvailable(): Boolean`
- Product ID constant: `"honq_pro_lifetime"`

**Step 2.2: Android actual — Google Play Billing**
- File: `shared/src/androidMain/.../data/premium/BillingService.android.kt`
- Add dependency: `com.android.billingclient:billing-ktx:7.1.1` to `shared/build.gradle.kts`
- Implement using `BillingClient`:
  - Connect to Play Store
  - Query product details for `"honq_pro_lifetime"` (in-app product, not subscription)
  - Launch purchase flow
  - Acknowledge purchase
  - Query existing purchases for restore

**Step 2.3: iOS actual — StoreKit 2**
- File: `shared/src/iosMain/.../data/premium/BillingService.ios.kt`
- Use StoreKit 2 via Kotlin/Native interop
- Implement purchase, restore, and entitlement check
- Non-consumable product type

### Phase 3: Feature Gating

**Step 3.1: Gate Mock Tests**
- In `HomeScreen.kt`, the "Take Mock Test" button:
  - Check `premiumManager.isPremium` and `freeTrialMockTestsRemaining`
  - If not premium AND no free tests remaining → show paywall bottom sheet
  - If free test available → proceed normally, consume free test on completion (in MockTestContainer)

**Step 3.2: Gate Category Selection (Practice by Category)**
- In `HomeScreen.kt`, the "Start Practice" button currently navigates to `CategorySelectionScreen`
  - Free users: navigate directly to `Screen.Practice` (random practice, skip category selection)
  - Premium users: navigate to `Screen.CategorySelection` as before
- Alternatively, show paywall bottom sheet if free user tries category selection

**Step 3.3: Gate Smart Practice**
- In `HomeScreen.kt`, the "Smart Practice" button:
  - Check `premiumManager.isPremium`
  - If not premium → show paywall bottom sheet
- Add a "PRO" badge on the Smart Practice button when not premium

**Step 3.4: Gate Statistics Features**
- In `HomeScreen.kt`, stats cards → on click:
  - Check premium state
  - If not premium → show paywall bottom sheet
- Keep basic accuracy & mock test count visible on home screen (already there)
- "Stats" detail link → gated

**Step 3.5: Gate Weakest/Unanswered Questions**
- These are accessed from Statistics screen, which is already gated
- Double-gate in NavGraph for direct navigation safety

### Phase 4: Paywall Bottom Sheet

**Step 4.1: Create `ProPaywallBottomSheet` composable**
- File: `shared/.../presentation/screens/paywall/ProPaywallBottomSheet.kt`
- Uses `ModalBottomSheet` from Material3
- Clean, compelling UI:
  - Drag handle at top
  - "Unlock Honq Pro" heading with app icon
  - Feature list with checkmarks (using HonqTheme colors):
    - ✓ Unlimited mock tests
    - ✓ Practice by category
    - ✓ Smart Practice (spaced repetition)
    - ✓ Detailed statistics & analytics
    - ✓ Weakest questions review
    - ✓ Track unanswered questions
  - Primary CTA button: "Unlock for $4.99" (full-width HonqButton)
  - Subtitle: "One-time purchase · No subscription"
  - "Restore Purchase" text link below
- Follows existing design system (HonqTheme, HonqCard, HonqButton, etc.)
- Dismissible by swiping down or tapping outside

**Step 4.2: Create `PaywallContract.kt`**
- File: `shared/.../presentation/screens/paywall/PaywallContract.kt`
- State: `isPurchasing: Boolean`, `error: String?`, `purchaseSuccess: Boolean`
- Intent: `Purchase`, `Restore`, `Dismiss`
- Action: `DismissPaywall`, `PurchaseComplete`

**Step 4.3: Integrate into HomeScreen**
- Add `var showPaywall by remember { mutableStateOf(false) }` to HomeScreen
- When any gated feature is tapped → `showPaywall = true`
- Render `ProPaywallBottomSheet` when `showPaywall == true`
- On purchase success → dismiss sheet + navigate to the originally requested feature
- On dismiss → just close the sheet

**Step 4.4: No new Screen route needed**
- Bottom sheet is overlay, not a navigation destination
- No `Screen.Paywall` needed — simpler navigation graph

### Phase 5: UI Polish

**Step 5.1: "PRO" badges on gated features**
- HomeScreen: Add small "PRO" badge on Smart Practice button
- HomeScreen: Add "PRO" badge on Stats card links
- HomeScreen: Add "PRO" badge on Mock Test button (after free test used)
- HomeScreen: "Start Practice" button text changes: free → "Random Practice", premium → "Start Practice" (with categories)
- Use a reusable `ProBadge` composable: small rounded chip with primary background

**Step 5.2: Mock test free trial indicator**
- On home screen, when user hasn't used their free test:
  - Show "1 Free Test" subtitle/badge on the mock test button
- After using: show "PRO" badge

**Step 5.3: About screen — "Restore Purchase" option**
- Add a "Restore Purchase" row in the About screen settings section
- Calls `PremiumManager.restorePurchase()`
- Also add a "Honq Pro" status indicator (shows "Active" if premium)

### Phase 6: Code Fixes

**Step 6.1: Fix `navigationBarsPadding` placement**
- `HomeScreen.kt:182` — `navigationBarsPadding()` is applied to `BottomActionBar`'s modifier but should be inside the padding of the bar itself to avoid layout issues on different devices

**Step 6.2: Remove commented-out code**
- `HomeScreen.kt:672-680` — Remove commented-out "What is Smart Practice?" text block

**Step 6.3: Fix empty `params` usage in DI**
- `PresentationModule.kt:84` — `factory { params -> FavoritesContainer(...) }` — `params` is unused, should be `factory { _ -> ... }` like SearchContainer on line 102
- `PresentationModule.kt:85` — Same for FavoriteQuestionContainer lambda style

---

## File Impact Summary

### New Files (7)
1. `shared/.../domain/premium/PremiumManager.kt` — interface
2. `shared/.../data/local/PremiumPreferences.kt` — DataStore persistence
3. `shared/.../data/premium/PremiumManagerImpl.kt` — implementation
4. `shared/.../data/premium/BillingService.kt` — expect declaration
5. `shared/src/androidMain/.../data/premium/BillingService.android.kt` — Google Play Billing
6. `shared/src/iosMain/.../data/premium/BillingService.ios.kt` — StoreKit 2
7. `shared/.../presentation/screens/paywall/ProPaywallBottomSheet.kt` — paywall UI + contract

### Modified Files (6)
1. `shared/build.gradle.kts` — add billing dependency
2. `shared/.../data/di/DataModule.kt` — register PremiumPreferences, PremiumManager, BillingService
3. `shared/.../presentation/di/PresentationModule.kt` — code fixes (unused params)
4. `shared/.../presentation/screens/home/HomeScreen.kt` — PRO badges, gating logic, bottom sheet, code fixes
5. `shared/.../presentation/screens/mocktest/MockTestContainer.kt` — consume free test on completion
6. `shared/.../presentation/screens/about/AboutScreen.kt` — add "Restore Purchase" + Pro status

---

## Testing Checklist
- [ ] Free user can do unlimited random practice (no category filter)
- [ ] Free user sees paywall bottom sheet when tapping "Start Practice" (category selection)
- [ ] Free user can take exactly 1 mock test
- [ ] Free user sees paywall bottom sheet on 2nd mock test attempt
- [ ] Free user sees paywall bottom sheet on Smart Practice tap
- [ ] Free user sees paywall bottom sheet on Statistics card tap
- [ ] Bottom sheet dismisses properly (swipe down, tap outside, "Continue Free")
- [ ] Purchase flow works on Android (Google Play Billing)
- [ ] Purchase flow works on iOS (StoreKit 2)
- [ ] Restore purchase works from About screen
- [ ] Premium user has no restrictions — all features unlocked
- [ ] Premium state persists across app restarts
- [ ] PRO badges visible for free users, hidden for premium users
- [ ] Free mock test counter persists across app restarts
- [ ] After purchase in bottom sheet, user is navigated to the feature they wanted
