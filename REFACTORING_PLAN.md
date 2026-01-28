# Offline-First Sync Refactoring + UI Improvements

## Overview

Refactor the app to be truly offline-first with version-based sync from Supabase, plus address accumulated UI issues.

---

## Part 1: Offline-First Sync Architecture

### 1.1 Supabase: Insert `data_version` row

The `app_config` table already exists (has `min_app_version`, `maintenance_mode`, `featured_state`). Insert a new row:

```sql
INSERT INTO app_config (key, value) VALUES ('data_version', '1');
```

### 1.2 New file: `AppConfigApi.kt`

**Path:** `data/remote/api/AppConfigApi.kt`

Fetches `data_version` from `app_config` table via Supabase Postgrest. Simple class with one method: `fetchDataVersion(): Int`.

### 1.3 Modify: `SyncPreferences.kt` — extend interface

Add to the existing interface:
- `getLocalDataVersion(): Int`
- `setLocalDataVersion(version: Int)`
- `hasCompletedInitialSync(): Boolean`
- `setInitialSyncCompleted(completed: Boolean)`

Update `InMemorySyncPreferences` with in-memory stubs.

### 1.4 New file: `DataStoreSyncPreferences.kt`

**Path:** `data/local/DataStoreSyncPreferences.kt`

Persistent implementation using the existing `DataStore<Preferences>` singleton. Follows `DataStoreOnboardingPreferences` pattern from `AppSettings.kt`. Stores:
- `local_data_version` (int)
- `initial_sync_completed` (boolean)
- `sync_time_{questionSetId}` (long, epoch millis)

### 1.5 New file: `DataSyncManager.kt`

**Path:** `data/repository/DataSyncManager.kt`

Central sync orchestrator with:
- `needsInitialSync(): Boolean` — checks if initial sync ever completed
- `checkForUpdates(): Result<Boolean>` — compares remote vs local data version
- `markSyncCompleted(remoteVersion: Int)` — persists version after successful sync
- `fetchRemoteVersion(): Result<Int>` — gets current remote version

### 1.6 Modify: `QuestionRepository.kt` (interface)

Add: `suspend fun fullSync(questionSetId: String? = null): Result<Unit>`

### 1.7 Modify: `QuestionRepositoryImpl.kt` — core refactoring

Make all read methods local-only (remove sync calls from read paths):

- **`getStates()`** — Remove `syncStates()` call. Just return `localDataSource.getStates()`.
- **`getRandomQuestions(questionSetId, count, categoryId)`** — Remove the `if (questions.isEmpty()) { syncQuestions(); retry }` block. Return local data only.
- **`getMockTestQuestions(questionSetId)`** — Same: remove sync fallback.
- **`resolveDefaultQuestionSetId()`** — Remove `syncStates()` fallback. Return null if empty.
- **Add `fullSync(questionSetId)`** — Calls `syncStates()` + `syncQuestions(questionSetId)`. Used by `DataSyncManager` flow.

Keep `syncStates()` and `syncQuestions()` as internal methods (they do the actual fetch+upsert work).

### 1.8 Modify: `FakeQuestionRepository.kt`

Add stub: `override suspend fun fullSync(questionSetId: String?): Result<Unit> = Result.Success(Unit)`

### 1.9 Modify: `HomeContainer.kt` — new offline-first flow

Add `DataSyncManager` as constructor dependency.

New `loadInitialData()` flow:
1. Check `dataSyncManager.needsInitialSync()`
2. **First launch:** Show loading -> fetch remote version -> `repository.fullSync(null)` -> mark completed -> load from local
3. **Returning launch:** Load from local immediately -> background `checkForUpdates()` -> if version changed, `fullSync()` in background

`syncInBackground()` updated to: fetch remote version, run `fullSync(questionSetId)`, call `markSyncCompleted(remoteVersion)`, reload local data.

### 1.10 Modify: `OnboardingContainer.kt`

Add initial sync gate to `loadData()`: if `needsInitialSync()`, run `fullSync(null)` before reading local states/license types.

### 1.11 Modify: `DataModule.kt` — DI wiring

- Replace `InMemorySyncPreferences()` with `DataStoreSyncPreferences(get())`
- Add `single { AppConfigApi(get()) }`
- Add `single { DataSyncManager(get(), get()) }`

### 1.12 Modify: `PresentationModule.kt`

Add `get()` for `DataSyncManager` in `HomeContainer` and `OnboardingContainer` factory calls.

### Sync Flows

| Scenario | Behavior |
|---|---|
| First launch | Loading screen -> fullSync -> load local -> ready |
| Returning, same version | Load local instantly -> background check -> no sync |
| Returning, new version | Load local instantly (stale but usable) -> background fullSync -> refresh UI |
| Returning, offline | Load local instantly -> background check fails silently -> works fine |
| First launch, offline | Show error "Network required for initial setup" with Retry |

---

## Part 2: Fix Empty Categories Screen

**Root cause:** Categories for a question set come from the `question_set_categories` junction table + `categories` table. These are synced during `syncStates()`. If sync hasn't completed or categories weren't fetched, the screen is empty.

**Fix:** After the offline-first refactoring (Part 1), `syncStates()` is called during `fullSync()` which fetches categories + question_set_categories. The category screen will work once data is synced.

**Additionally:** In `GetCategoriesUseCase`, add a fallback to return all active categories if question-set-specific categories are empty:

**File:** `domain/usecase/GetCategoriesUseCase.kt`
- If `getCategoriesByQuestionSet(questionSetId)` returns empty, fall back to all active categories from the local DB.

---

## Part 3: License Type Icons on Home Screen

**Current state:** Onboarding uses `LicenseTypeIcon` composable mapping `LicenseTypeId.CAR` -> `Icons.Rounded.DirectionsCar`, `LicenseTypeId.RIDER` -> `Icons.Rounded.TwoWheeler`.

**Changes:**

1. **Extract shared `LicenseTypeIcon` composable** from `OnboardingScreen.kt` to `presentation/components/base/LicenseTypeIcon.kt`

2. **Modify `SelectableChip` in `HomeScreen.kt`**:
   - Add optional `icon: ImageVector?` parameter
   - Show icon before text when provided
   - Pass `LicenseTypeId.fromId(type.id)` icon when rendering license type chips

---

## Part 4: More Prominent Syncing Indicator

**File:** `HomeScreen.kt` — `SyncIndicator` composable

**Current:** Uses `colors.surface` background which blends with card backgrounds.

**Fix:** Change to a more visible style:
- Use `colors.primarySurface` background with `colors.primary` border
- Or use `colors.primary` background with contrasting text
- Add a subtle elevation/shadow

---

## Part 5: More Appealing ProgressRings

**File:** `StatisticsScreen.kt` — `OverviewSection`

**Improvements:**
- Add animation on first appearance (animate from 0 to actual value)
- Increase ring thickness slightly for more visual weight
- Add color-based thresholds for visual feedback
- Check `ProgressRing` composable for available parameters

---

## Part 6: Save Mock Test Questions for Review

**Current state:** `MockTestResultEntity` stores `totalQuestions`, `correctAnswers`, `timeTakenSeconds`, `passed`, `completedAt`. Individual question IDs and answers are NOT saved.

**Changes:**

1. **Add new Room entity: `MockTestAnswerEntity`**
   - `id` (auto), `mockTestResultId` (FK), `questionId`, `selectedAnswerIndex`, `wasCorrect`

2. **Add `MockTestAnswerDao`** with insert and query methods

3. **Update `MockTestContainer.submitTest()`** to save individual answers

4. **Update `StatisticsScreen`** mock test result items to be clickable -> navigate to a review screen

5. **Database migration** from v1 to v2

---

## Part 7: Improve FavoriteQuestionCard

**File:** `presentation/screens/favorites/FavoritesScreen.kt` — `FavoriteQuestionCard`

**Current:** Bookmark icon is to the right of the question text (top-aligned). No question ID shown.

**Changes:**
1. Move bookmark icon button to bottom-right of the card
2. Show question ID concisely (e.g., `#Q123` as a subtle muted label) in the metadata row alongside the category pill and state pill

---

## Implementation Order

1. **Part 1** — Offline-first sync (core architecture change)
2. **Part 2** — Fix categories (directly benefits from Part 1)
3. **Part 6** — Mock test answer persistence (DB migration)
4. **Part 3** — License type icons (UI, independent)
5. **Part 4** — Syncing indicator (UI, independent)
6. **Part 5** — ProgressRings (UI, independent)
7. **Part 7** — FavoriteQuestionCard improvements (UI, independent)

---

## Key Files Modified

| File | Changes |
|---|---|
| `data/remote/api/AppConfigApi.kt` | NEW — fetch data_version |
| `data/local/SyncPreferences.kt` | Extended interface |
| `data/local/DataStoreSyncPreferences.kt` | NEW — persistent impl |
| `data/repository/DataSyncManager.kt` | NEW — sync orchestrator |
| `data/repository/QuestionRepositoryImpl.kt` | Remove sync from reads, add fullSync() |
| `domain/repository/QuestionRepository.kt` | Add fullSync() |
| `data/repository/FakeQuestionRepository.kt` | Add fullSync() stub |
| `data/di/DataModule.kt` | Wire new classes |
| `presentation/di/PresentationModule.kt` | Add DataSyncManager to containers |
| `presentation/screens/home/HomeContainer.kt` | New offline-first flow |
| `presentation/screens/onboarding/OnboardingContainer.kt` | Initial sync gate |
| `presentation/screens/home/HomeScreen.kt` | Icons in chips, syncing indicator |
| `presentation/screens/statistics/StatisticsScreen.kt` | ProgressRing improvements |
| `domain/model/LicenseType.kt` | Icon mapping |
| `data/local/entity/MockTestAnswerEntity.kt` | NEW — test answer persistence |
| `data/local/db/MockTestAnswerDao.kt` | NEW — DAO |
| `data/local/db/HonqDatabase.kt` | Add new entity + DAO, bump version |
| `domain/usecase/GetCategoriesUseCase.kt` | Fallback for empty categories |
| `presentation/screens/favorites/FavoritesScreen.kt` | Move bookmark to bottom, add question ID |

## Verification

1. Fresh install -> shows loading -> syncs -> onboarding works -> home shows data
2. Kill app, reopen -> loads instantly from local DB, no loading spinner
3. Bump `data_version` in Supabase -> reopen app -> background sync updates data
4. Turn off network, reopen -> works perfectly with cached data
5. "By Category" button -> shows categories (not empty)
6. License type chips show icons
7. Syncing indicator is visually distinct
8. Mock test results save all question answers for later review
