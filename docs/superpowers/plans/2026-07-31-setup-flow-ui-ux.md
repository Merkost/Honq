# Setup Flow and Readiness UI/UX Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make onboarding, home setup changes, and the readiness card feel faster, clearer, and more dependable without changing question-bank, persistence, or repository contracts.

**Architecture:** Keep state and callbacks hoisted through `OnboardingContainer` and `HomeContainer`. Add small stateless Compose components for the setup summary and sync feedback, plus pure presentation helpers for license availability and setup-summary data. Move the home setup affordance outside the clickable readiness card so each interaction has one clear target.

**Tech Stack:** Kotlin Multiplatform `commonMain`, Compose Multiplatform, Material 3, FlowMVI containers, Compose resources, `kotlin.test` common tests, Android emulator smoke testing.

## Global Constraints

- Keep shared logic in `shared/src/commonMain`; do not add platform-specific UI code.
- Keep state and callbacks hoisted through the existing onboarding and home containers.
- Reuse existing Honq theme colors, spacing, shapes, motion, and minimum touch-target tokens.
- Keep state and licence choices exposed as radio-button semantics with selected and disabled states.
- Put new user-facing copy in `shared/src/commonMain/composeResources/values/strings.xml`; do not add raw UI literals in modified composables.
- Do not change question-bank formats, Firestore schemas, repository contracts, persistence models, or navigation.
- Preserve existing content, persistence, analytics, and external-link behavior.
- Preserve unrelated working-tree changes, especially question-bank and import files; stage only files belonging to the task when committing.
- Verify Android and iOS common-source compatibility separately; do not treat static inspection as runtime proof.

---

## File map

| File | Responsibility |
|---|---|
| `shared/src/commonMain/kotlin/com/merkost/honq/presentation/components/home/LicenseChoiceAvailability.kt` | Pure mapping from sync/question-set state to Updating, Available, or Unavailable. |
| `shared/src/commonMain/kotlin/com/merkost/honq/presentation/screens/onboarding/OnboardingSetupSummary.kt` | Pure setup-summary model/helper and stateless summary card with preview. |
| `shared/src/commonMain/kotlin/com/merkost/honq/presentation/screens/onboarding/OnboardingContract.kt` | Completion loading/error state and retry intent. |
| `shared/src/commonMain/kotlin/com/merkost/honq/presentation/screens/onboarding/OnboardingContainer.kt` | Completion state transitions and retry behavior. |
| `shared/src/commonMain/kotlin/com/merkost/honq/presentation/screens/onboarding/OnboardingScreen.kt` | Uses the summary and exposes completion feedback. |
| `shared/src/commonMain/kotlin/com/merkost/honq/presentation/screens/home/HomeContract.kt` | Adds a sync-retry intent. |
| `shared/src/commonMain/kotlin/com/merkost/honq/presentation/screens/home/HomeContainer.kt` | Retries the current question-set sync while preserving setup selections. |
| `shared/src/commonMain/kotlin/com/merkost/honq/presentation/screens/home/HomeScreen.kt` | Renders setup context outside the readiness card and adds sync feedback. |
| `shared/src/commonMain/kotlin/com/merkost/honq/presentation/components/home/SetupSyncFeedback.kt` | Stateless retry banner with preview. |
| `shared/src/commonMain/kotlin/com/merkost/honq/presentation/components/home/StateLicenseSheet.kt` | Availability grouping and state-chip semantics. |
| `shared/src/commonMain/kotlin/com/merkost/honq/presentation/components/home/ReadinessCard.kt` | Removes nested setup interaction and keeps the readiness hierarchy. |
| `shared/src/commonMain/composeResources/values/strings.xml` | Shared setup, retry, availability, context, and error copy. |
| `shared/src/commonTest/kotlin/com/merkost/honq/presentation/components/home/LicenseChoiceAvailabilityTest.kt` | Pure availability mapping tests. |
| `shared/src/commonTest/kotlin/com/merkost/honq/presentation/screens/onboarding/OnboardingSetupSummaryTest.kt` | Summary visibility/content tests. |
| `shared/src/commonTest/kotlin/com/merkost/honq/presentation/components/home/ReadinessCardLayoutTest.kt` | Existing responsive header tests. |

## Task 1: Add pure presentation helpers and tests first

**Files:**

- Create: `shared/src/commonMain/kotlin/com/merkost/honq/presentation/components/home/LicenseChoiceAvailability.kt`
- Test: `shared/src/commonTest/kotlin/com/merkost/honq/presentation/components/home/LicenseChoiceAvailabilityTest.kt`
- Create: `shared/src/commonMain/kotlin/com/merkost/honq/presentation/screens/onboarding/OnboardingSetupSummary.kt`
- Test: `shared/src/commonTest/kotlin/com/merkost/honq/presentation/screens/onboarding/OnboardingSetupSummaryTest.kt`

**Interfaces:**

- `LicenseChoiceAvailability` produces the exact display state used by `StateLicenseSheet`.
- `createSetupSummary(state: State?, licenseType: LicenseType?): SetupSummary?` returns null until both selections exist.

- [ ] **Step 1: Write the failing availability tests.**

```kotlin
class LicenseChoiceAvailabilityTest {
    @Test
    fun empty_question_sets_while_syncing_is_updating() {
        assertEquals(
            LicenseChoiceAvailability.Updating,
            licenseChoiceAvailability(true, questionSetCount = 0, hasQuestionSet = false),
        )
    }

    @Test
    fun matching_question_set_is_available_even_while_background_sync_runs() {
        assertEquals(
            LicenseChoiceAvailability.Available,
            licenseChoiceAvailability(true, questionSetCount = 2, hasQuestionSet = true),
        )
    }

    @Test
    fun missing_question_set_is_unavailable_after_loading() {
        assertEquals(
            LicenseChoiceAvailability.Unavailable,
            licenseChoiceAvailability(false, questionSetCount = 2, hasQuestionSet = false),
        )
    }
}
```

- [ ] **Step 2: Run the availability test and verify the expected missing-symbol failure.**

```bash
./gradlew :shared:testDebugUnitTest --tests 'com.merkost.honq.presentation.components.home.LicenseChoiceAvailabilityTest' --no-configuration-cache --console=plain
```

Expected: compilation fails because `LicenseChoiceAvailability` and `licenseChoiceAvailability` do not exist.

- [ ] **Step 3: Implement the minimal availability helper.**

```kotlin
internal enum class LicenseChoiceAvailability {
    Updating,
    Available,
    Unavailable,
}

internal fun licenseChoiceAvailability(
    isSyncing: Boolean,
    questionSetCount: Int,
    hasQuestionSet: Boolean,
): LicenseChoiceAvailability = when {
    isSyncing && questionSetCount == 0 -> LicenseChoiceAvailability.Updating
    hasQuestionSet -> LicenseChoiceAvailability.Available
    else -> LicenseChoiceAvailability.Unavailable
}
```

- [ ] **Step 4: Write the failing setup-summary tests and concrete fixtures.**

```kotlin
class OnboardingSetupSummaryTest {
    @Test
    fun summary_is_hidden_until_both_choices_exist() {
        assertNull(createSetupSummary(null, null))
        assertNull(createSetupSummary(sampleState(), null))
        assertNull(createSetupSummary(null, sampleLicenseType()))
    }

    @Test
    fun summary_contains_state_and_license_labels() {
        assertEquals(
            SetupSummary("New South Wales", "NSW", "Car", "C"),
            createSetupSummary(sampleState(), sampleLicenseType()),
        )
    }
}

private fun sampleState() = State("nsw", "New South Wales", "NSW")
private fun sampleLicenseType() = LicenseType("car", "Car", "C")
```

- [ ] **Step 5: Run the summary test and verify the expected missing-symbol failure.**

```bash
./gradlew :shared:testDebugUnitTest --tests 'com.merkost.honq.presentation.screens.onboarding.OnboardingSetupSummaryTest' --no-configuration-cache --console=plain
```

Expected: compilation fails because `SetupSummary` and `createSetupSummary` do not exist.

- [ ] **Step 6: Implement the pure summary model/helper and stateless summary card.**

```kotlin
internal data class SetupSummary(
    val stateName: String,
    val stateCode: String,
    val licenseName: String,
    val licenseCode: String,
)

internal fun createSetupSummary(
    state: State?,
    licenseType: LicenseType?,
): SetupSummary? = if (state == null || licenseType == null) {
    null
} else {
    SetupSummary(state.name, state.shortName, licenseType.name, licenseType.shortName)
}
```

Implement `@Composable internal fun OnboardingSetupSummaryCard(summary: SetupSummary, modifier: Modifier = Modifier)` in the same file. Render a compact “Your study setup” card with labeled state and licence rows, existing surface/border/spacing tokens, and a `@Preview` using NSW/Car data.

- [ ] **Step 7: Run both focused tests and commit only the helper slice.**

```bash
./gradlew :shared:testDebugUnitTest --tests 'com.merkost.honq.presentation.components.home.LicenseChoiceAvailabilityTest' --tests 'com.merkost.honq.presentation.screens.onboarding.OnboardingSetupSummaryTest' --no-configuration-cache --console=plain
git add shared/src/commonMain/kotlin/com/merkost/honq/presentation/components/home/LicenseChoiceAvailability.kt shared/src/commonTest/kotlin/com/merkost/honq/presentation/components/home/LicenseChoiceAvailabilityTest.kt shared/src/commonMain/kotlin/com/merkost/honq/presentation/screens/onboarding/OnboardingSetupSummary.kt shared/src/commonTest/kotlin/com/merkost/honq/presentation/screens/onboarding/OnboardingSetupSummaryTest.kt
git commit -m "feat: add setup presentation state helpers"
```

Expected: both focused test classes pass.

## Task 2: Add onboarding setup summary and completion feedback

**Files:**

- Modify: `shared/src/commonMain/kotlin/com/merkost/honq/presentation/screens/onboarding/OnboardingContract.kt:14-31`
- Modify: `shared/src/commonMain/kotlin/com/merkost/honq/presentation/screens/onboarding/OnboardingContainer.kt:57-207`
- Modify: `shared/src/commonMain/kotlin/com/merkost/honq/presentation/screens/onboarding/OnboardingScreen.kt:119-512`
- Modify: `shared/src/commonMain/composeResources/values/strings.xml:57-75`

**Interfaces:**

- `OnboardingState.isCompleting: Boolean` controls the final-button spinner and prevents duplicate completion taps.
- `OnboardingState.completionError: String?` controls inline retry while retaining both selections.
- `OnboardingIntent.RetryCompletion` re-enters the existing `completeOnboarding()` path.

- [ ] **Step 1: Extend the onboarding contract.**

Add the two state fields:

```kotlin
val isCompleting: Boolean = false,
val completionError: String? = null,
```

Add the intent:

```kotlin
data object RetryCompletion : OnboardingIntent
```

- [ ] **Step 2: Add completion state transitions in the container.**

Handle `RetryCompletion` by calling `completeOnboarding()`. Before the question-set lookup, snapshot the selected IDs and set `isCompleting = true`:

```kotlin
var selectedStateId: String? = null
var selectedLicenseTypeId: String? = null
withState {
    selectedStateId = this.selectedStateId
    selectedLicenseTypeId = this.selectedLicenseTypeId
}
if (selectedStateId == null || selectedLicenseTypeId == null) return
updateState { copy(isCompleting = true, completionError = null) }
```

Retain the existing preference, analytics, selected-question-set, and navigation behavior on success. On question-set lookup failure, set `isCompleting = false` and `completionError = SYNC_ERROR_MESSAGE` without navigating away. Leave the initial-load `Retry` path unchanged.

- [ ] **Step 3: Add exact onboarding feedback copy.**

Add these English resources:

```xml
<string name="onboarding_setup_title">Your study setup</string>
<string name="onboarding_completion_error_title">Couldn’t finish setting up</string>
<string name="onboarding_completion_error_subtitle">Your choices are saved. Check your connection and try again.</string>
<string name="onboarding_completion_retry">Try again</string>
<string name="onboarding_back">Back</string>
<string name="onboarding_selected">Selected</string>
```

- [ ] **Step 4: Render the summary after a licence is selected.**

Pass `state.selectedState` and `state.selectedLicenseType` into `LicenseTypeSelectionStep`. Inside its scrollable content, compute `createSetupSummary(selectedState, selectedLicenseType)` and render `OnboardingSetupSummaryCard` before the licence rows when non-null. Keep the footer outside the scroll container so Start Learning remains anchored above navigation insets.

- [ ] **Step 5: Add completion loading and inline retry UI.**

Update `OnboardingFooter` to accept `loading: Boolean = false` and pass it to `HonqButton(loading = loading)`. Pass `isCompleting` from the licence step. Render a compact inline error panel below the licence rows and above the footer when `completionError != null`, with the new title/subtitle and a retry action wired to `RetryCompletion`. Use `enabled = canComplete && !isCompleting`.

- [ ] **Step 6: Replace modified raw onboarding content descriptions.**

Use `onboarding_back` for the back icon and `onboarding_selected` for selected indicators. Do not change unrelated existing user-facing strings.

- [ ] **Step 7: Run the Android compile/test gate and commit the onboarding slice.**

```bash
./gradlew :shared:compileDebugKotlinAndroid :shared:testDebugUnitTest --no-configuration-cache --console=plain
git add shared/src/commonMain/kotlin/com/merkost/honq/presentation/screens/onboarding/OnboardingContract.kt shared/src/commonMain/kotlin/com/merkost/honq/presentation/screens/onboarding/OnboardingContainer.kt shared/src/commonMain/kotlin/com/merkost/honq/presentation/screens/onboarding/OnboardingScreen.kt shared/src/commonMain/composeResources/values/strings.xml
git commit -m "feat: improve onboarding setup feedback"
```

Expected: Android shared compilation and all Android unit tests pass.

## Task 3: Improve home setup availability and sync recovery

**Files:**

- Modify: `shared/src/commonMain/kotlin/com/merkost/honq/presentation/screens/home/HomeContract.kt:34-41`
- Modify: `shared/src/commonMain/kotlin/com/merkost/honq/presentation/screens/home/HomeContainer.kt:55-75,255-287`
- Modify: `shared/src/commonMain/kotlin/com/merkost/honq/presentation/screens/home/HomeScreen.kt:175-202,284-462`
- Modify: `shared/src/commonMain/kotlin/com/merkost/honq/presentation/components/home/StateLicenseSheet.kt:59-360`
- Create: `shared/src/commonMain/kotlin/com/merkost/honq/presentation/components/home/SetupSyncFeedback.kt`
- Modify: `shared/src/commonMain/composeResources/values/strings.xml`

**Interfaces:**

- `HomeIntent.RetrySync` retries only the currently selected question-set sync.
- `SetupSyncFeedback(modifier: Modifier = Modifier, onRetry: () -> Unit)` is stateless.
- `StateLicenseSheet` keeps its existing `HomeState` and selection callbacks; no repository or data-model changes are introduced.

- [ ] **Step 1: Add the retry intent and container action.**

Add:

```kotlin
data object RetrySync : HomeIntent
```

Handle it with a local guard and reuse `syncInBackground()`:

```kotlin
private suspend fun PipelineContext<HomeState, HomeIntent, HomeAction>.retrySync() {
    var canRetry = false
    withState {
        canRetry = selectedQuestionSet != null && !isSyncing
    }
    if (canRetry) syncInBackground()
}
```

The existing sync method remains the only owner of `isSyncing`, `syncError`, pending-version handling, and repository calls.

- [ ] **Step 2: Add exact home feedback and grouping copy.**

```xml
<string name="home_change_setup">Change setup</string>
<string name="home_sync_error_title">Question bank update failed</string>
<string name="home_sync_error_subtitle">Your setup is saved. Retry the update when you’re ready.</string>
<string name="home_sync_error_retry">Retry update</string>
<string name="home_available_licenses">Available to study</string>
<string name="home_unavailable_licenses">Not available yet</string>
```

- [ ] **Step 3: Add the stateless sync-feedback component and preview.**

Create `SetupSyncFeedback.kt` with a compact bordered surface containing the error title, subtitle, and Material 3 `TextButton` retry action. Use existing warning/error theme tokens, expose button semantics through `TextButton`, and add a `@Preview` in the same file.

- [ ] **Step 4: Render feedback without hiding valid home content.**

Add `onRetrySync` to `HomeContent`, wire it from the root screen as `HomeIntent.RetrySync`, and render `SetupSyncFeedback` inside the scrollable home column when `state.syncError != null`. Keep readiness and practice content visible after a background sync failure.

- [ ] **Step 5: Use the availability helper in the setup sheet.**

For each licence type, calculate:

```kotlin
val hasQuestionSet = state.questionSets.any { it.licenseTypeId == type.id }
val availability = licenseChoiceAvailability(
    isSyncing = state.isSyncing,
    questionSetCount = state.questionSets.size,
    hasQuestionSet = hasQuestionSet,
)
```

Use the result for enabled state and supporting copy. Render `home_available_licenses` before available rows and `home_unavailable_licenses` before unavailable rows. While every row is `Updating`, keep the existing progress indicator and avoid showing stale availability. Preserve the external-practice link below empty selections.

- [ ] **Step 6: Improve state-chip semantics and target size.**

Keep the three-column state grid, add the full `stateOption.name` to each chip’s semantics/content description while keeping the visible short code, and preserve the 48dp minimum target. Use `home_change_setup` for the home context row instead of the raw `Tap to change` literal.

- [ ] **Step 7: Run focused sheet tests and Android tests, then commit.**

```bash
./gradlew :shared:testDebugUnitTest --tests 'com.merkost.honq.presentation.components.home.LicenseChoiceAvailabilityTest' --no-configuration-cache --console=plain
./gradlew :shared:compileDebugKotlinAndroid :shared:testDebugUnitTest --no-configuration-cache --console=plain
git add shared/src/commonMain/kotlin/com/merkost/honq/presentation/screens/home/HomeContract.kt shared/src/commonMain/kotlin/com/merkost/honq/presentation/screens/home/HomeContainer.kt shared/src/commonMain/kotlin/com/merkost/honq/presentation/screens/home/HomeScreen.kt shared/src/commonMain/kotlin/com/merkost/honq/presentation/components/home/StateLicenseSheet.kt shared/src/commonMain/kotlin/com/merkost/honq/presentation/components/home/SetupSyncFeedback.kt shared/src/commonMain/composeResources/values/strings.xml
git commit -m "feat: improve setup sync feedback"
```

Expected: the availability test and full Android unit-test task pass. Commit only the home contract/container/screen/sheet/banner/resource files.

## Task 4: Separate setup context from the readiness card

**Files:**

- Modify: `shared/src/commonMain/kotlin/com/merkost/honq/presentation/screens/home/HomeScreen.kt:379-420,464-500`
- Modify: `shared/src/commonMain/kotlin/com/merkost/honq/presentation/components/home/ReadinessCard.kt:55-353`
- Modify: `shared/src/commonTest/kotlin/com/merkost/honq/presentation/components/home/ReadinessCardLayoutTest.kt` only if the header helper changes

**Interfaces:**

```kotlin
@Composable
fun ReadinessCard(
    progress: UserProgress,
    passMark: Int,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
)
```

`HomeContextChipRow` owns setup changes and receives `stateCode`, `licenseTypeId`, `licenseCode`, `licenseName`, and `onClick`.

- [ ] **Step 1: Move the context row outside the readiness card.**

Render `HomeContextChipRow` before `ReadinessCard` for in-app states and reuse the same row for external-only states. Pass `selectedLicenseType?.name` so the row reads as plate/code plus licence name where width permits.

- [ ] **Step 2: Remove nested setup interaction from `ReadinessCard`.**

Remove `stateCode`, `licenseTypeId`, `licenseCode`, and `onContextClick` from the public card parameters. Make the card header render `ReadinessTitle` and `StatusPill` only. Keep the optional card `onClick` for the existing statistics destination; it is no longer nested with a setup click target.

- [ ] **Step 3: Preserve responsive no-wrap behavior.**

Keep `BoxWithConstraints`, `READINESS_HEADER_STACK_BREAKPOINT`, `StatusPill(maxLines = 1, softWrap = false)`, and the existing `ReadinessHeaderLayout` helper. Update previews to call the new signature and continue covering on-track, zero, passed, and no-context states.

- [ ] **Step 4: Make the external/home context row accessible and stable.**

Give the row a 48dp minimum target, use `home_change_setup` as its visible/action label, expose it as a button-like interaction, and constrain the licence-name text to one line with ellipsis so it cannot push the row off-screen.

- [ ] **Step 5: Update stagger indexes and compile call sites.**

Because the in-app context row becomes a separate item, update `itemCount` and `staggeredEntrance` indices so the order is context → readiness → practice CTA → mode list → official resources. Keep external-only order as context → external resources.

- [ ] **Step 6: Run the focused layout test and inspect the diff.**

```bash
./gradlew :shared:testDebugUnitTest --tests 'com.merkost.honq.presentation.components.home.ReadinessCardLayoutTest' --no-configuration-cache --console=plain
git diff --check
git add shared/src/commonMain/kotlin/com/merkost/honq/presentation/screens/home/HomeScreen.kt shared/src/commonMain/kotlin/com/merkost/honq/presentation/components/home/ReadinessCard.kt shared/src/commonTest/kotlin/com/merkost/honq/presentation/components/home/ReadinessCardLayoutTest.kt
git commit -m "feat: simplify readiness setup hierarchy"
```

Expected: both commands pass with no whitespace errors. Commit only `HomeScreen.kt`, `ReadinessCard.kt`, and the focused test if changed.

## Task 5: Final verification and manual UX pass

**Files:**

- No new source files; verify all files from Tasks 1–4.

- [ ] **Step 1: Run the full Android shared verification.**

```bash
./gradlew :shared:compileDebugKotlinAndroid :shared:testDebugUnitTest --no-configuration-cache --console=plain
```

Expected: `BUILD SUCCESSFUL`; existing AGP/KMP deprecation warnings may remain but must not become errors.

- [ ] **Step 2: Compile the iOS simulator target.**

```bash
./gradlew :shared:compileKotlinIosSimulatorArm64 --no-configuration-cache --console=plain
```

Expected: `BUILD SUCCESSFUL`, verifying shared Compose/resource/source-set compatibility.

- [ ] **Step 3: Assemble the Android debug APK.**

```bash
./gradlew :androidApp:assembleDebug --no-configuration-cache --console=plain
```

Expected: `BUILD SUCCESSFUL` and a refreshed debug APK.

- [ ] **Step 4: Run the emulator walkthrough.**

Verify first-launch onboarding, selected/inactive state cards, setup summary, completion spinner/retry, home context outside the card, non-wrapping `KEEP STUDYING`, grouped sheet availability, sync feedback retry, small-viewport scrolling, and external-only practice links.

- [ ] **Step 5: Run final source/diff checks.**

```bash
git diff --check
git status --short
```

Confirm only intended setup/readiness files and focused test/resource files are part of the implementation commits. Report any broader suite that is not run or cannot complete separately from passing gates.
