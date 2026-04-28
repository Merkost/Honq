# Supabase Removal — Embed Content Offline

**Date:** 2026-04-28
**Status:** Approved (pending final review)

## Goal

Remove the Supabase dependency from the Honq KMP app. Ship the entire question bank — currently 903 questions across 4 active question sets, plus 392 question images — embedded inside the app as Compose Multiplatform resources. After this change, the app makes zero network calls for content; updates to the question bank ship as new app releases.

## Why

- Driving rules change rarely (~yearly). The question bank is small enough to ship in the binary.
- A new state is a release-worthy feature anyway — there is no value in a separate content-update channel.
- Removing Supabase eliminates: Postgrest SDK weight, build-time `BuildKonfig` secrets, a remote failure mode at first launch, and ongoing Supabase project costs.
- User-state data (favorites, mock test results, answer history) already lives in Room and is unaffected.

## Non-goals

- No CDN or remote content-update channel.
- No `app_config` migration — none of the keys (`min_app_version`, `maintenance_mode`, `featured_state`, `privacy_url`, `terms_url`) are read from Kotlin code today. Only `data_version` is consumed, and that disappears with the sync layer.
- The Supabase project itself is not modified by this change. It can be paused or deleted via the Supabase dashboard once the new app version is live in production.
- No Edge Functions, RLS, Auth, or Storage policies — none are used today.

## Current state (snapshot, 2026-04-28)

- **Supabase project ref:** `qhgpwybskpcrjykntvyo`
- **Tables in use** (rows): `states` (8), `license_types` (2), `assessment_types` (2), `question_sets` (4), `categories` (14), `question_set_categories` (51), `questions` (903), `state_resources` (16), `app_config` (6).
- **Note on `state_resources`:** unlike other content, this table is NOT cached in Room today. `GetStateResourcesUseCase` calls `QuestionApi.fetchStateResources(stateId)` directly on every Home-screen render. The migration must replace that path too.
- **Active question sets:** `nsw_car` (360), `nt_car` (357), `nt_rider` (102), `nsw_rider_rkt` (84).
- **Images:** 392 questions reference images; paths in `image_url` are relative (e.g. `questions/nt/CSB002.png`) and resolve against the public Supabase Storage bucket `questions`.
- **Local store:** Room, with content tables AND user-state tables (favorites, answer history, mock test results, mock test answers) sharing one DB.
- **Sync layer:** `DataSyncManager` polls `app_config.data_version`; `QuestionApi` / `AppConfigApi` issue Postgrest reads; `QuestionRepositoryImpl.fullSync` orchestrates a full reference-data + per-question-set fetch on first launch and version bumps.

## Design

### Architecture overview

The end state has three pieces:

1. **Bundled content resources** under `shared/src/commonMain/composeResources/files/content/v1/` — JSON files mirroring each Supabase content table, plus PNG image files preserving their relative paths.
2. **`BundledContentLoader`** — a single class in `data/local/seed/` that, on app start, checks a `BUNDLED_DATA_VERSION` constant against `SyncPreferences.localDataVersion` and, if they differ, parses the JSON resources and seeds Room via the existing `QuestionLocalDataSource.upsertAllReferenceData` and `upsertQuestions` paths. Then bumps `localDataVersion`.
3. **Read-only `QuestionRepository`** — the `syncQuestions`, `syncStates`, and `fullSync` methods are removed from the interface. All read methods continue to query Room exactly as today.
4. **`StateResourcesProvider`** — a tiny in-memory holder (no Room entity) loaded once at startup from `state_resources.json`. The rewritten `GetStateResourcesUseCase` reads from this provider instead of `QuestionApi`. We deliberately avoid adding a Room entity for this table to skip a schema migration: the data is small (16 rows), static, and only consumed by one screen.

Seeding is an app-lifecycle concern, not a repository concern. It runs once at startup from the existing app-init coroutine (the same place that initializes RevenueCat).

### Data flow (after migration)

1. **Build time:** A one-shot Kotlin script (`scripts/export-supabase-bundle.main.kts`) dumps all 7 content tables from Supabase via REST and downloads all 392 referenced images. Output lands under `shared/src/commonMain/composeResources/files/content/v1/`. Committed to the repo.
2. **App start:** `BundledContentLoader.ensureSeeded()` is invoked once. If `localDataVersion < BUNDLED_DATA_VERSION`, it reads each `*.json` resource, deserializes into the existing DTO data classes, maps to entities, and upserts into Room within the existing transactional `upsertAllReferenceData` call followed by `upsertQuestions`. On success, it sets `localDataVersion = BUNDLED_DATA_VERSION`.
3. **Reads:** Repository reads from Room exactly as today.
4. **Image rendering:** `imageUrl` keeps the same relative path (e.g. `questions/nt/CSB002.png`). A new helper `BundledImagePath.resolve(relativePath: String): String` returns a Compose-resource URI (`Res.getUri("files/content/v1/$relativePath")`). Compose call sites change from `SupabaseConfig.getStorageUrl(path)` to `BundledImagePath.resolve(path)`. Coil loads the resource via the Compose Multiplatform Coil integration.

### Bundle layout

```
shared/src/commonMain/composeResources/files/content/v1/
  states.json                     # 8 rows
  license_types.json              # 2 rows
  assessment_types.json           # 2 rows
  categories.json                 # 14 rows
  question_sets.json              # 4 rows
  question_set_categories.json    # 51 rows
  questions.json                  # 903 rows
  state_resources.json            # 16 rows
  questions/
    nsw/*.png
    nt/*.png
```

JSON shape mirrors the Postgrest response verbatim (snake_case keys; `options` is a real JSON array — the existing `QuestionDto.toEntity(json)` mapper converts it to a JSON-encoded string for the Room column, which keeps that conversion). The DTO data classes move from `data/remote/dto/` to `data/local/seed/dto/`. Mapper functions (`QuestionDtoMapper.kt`) move with them and are renamed appropriately.

**Mapper change for `imageUrl`:** today `QuestionDto.toEntity` calls `SupabaseConfig.getStorageUrl(imageUrl)` and stores the resulting *full Supabase Storage URL* in `QuestionEntity.imageUrl`. After migration, the seed mapper stores the *raw relative path* (e.g. `questions/nt/CSB002.png`); resolution to a Compose-resource URI happens at the UI layer via `BundledImagePath.resolve(...)`. Existing users have full Supabase URLs cached in their Room `questions` table; the version-bump-driven re-seed (next section) overwrites those rows with the relative-path form, which is required for the new image resolution to work.

The `v1` path segment matches `BUNDLED_DATA_VERSION`. Future bumps either rev the constant only (recommended) or rev both for cleanliness.

### Existing-user migration

Production users on the current Supabase build have `localDataVersion = 1` (the value `app_config.data_version` carries today). Setting `BUNDLED_DATA_VERSION = 2` ensures every existing install re-seeds once on first launch of the new version. Question IDs are preserved verbatim from the Supabase dump, so `favorites`, `answer_history`, `mock_test_answer` rows remain valid (they reference question IDs as strings).

### Export script

`scripts/export-supabase-bundle.main.kts` (run via `kotlin scripts/export-supabase-bundle.main.kts`):

1. Reads `supabase.url` and `supabase.key` from `local.properties`.
2. For each of the 8 content tables (the 7 above plus `state_resources`), GETs `<url>/rest/v1/<table>?select=*&order=id` with `apikey` and `Authorization: Bearer` headers; pretty-writes JSON to `composeResources/files/content/v1/<table>.json`.
3. Iterates `questions[].image_url`; for each non-null path, GETs `<url>/storage/v1/object/public/<path>`; writes the file under `composeResources/files/content/v1/<path>`.
4. Prints a summary (rows per table, image count, total image bytes).

The script is committed to the repo but is not part of the app build. Run once now to seed the bundle; re-run later when the question bank needs to refresh (and bump `BUNDLED_DATA_VERSION`).

### `BundledContentLoader` shape

```kotlin
class BundledContentLoader(
    private val localDataSource: QuestionLocalDataSource,
    private val syncPreferences: SyncPreferences,
    private val json: Json,
    private val dispatchers: AppDispatchers,
) {
    suspend fun ensureSeeded() = withContext(dispatchers.io) {
        if (syncPreferences.getLocalDataVersion() >= BUNDLED_DATA_VERSION) return@withContext
        seedFromBundle()
        syncPreferences.setLocalDataVersion(BUNDLED_DATA_VERSION)
        syncPreferences.setInitialSyncCompleted(true)
    }

    private suspend fun seedFromBundle() {
        val states = readJson<List<StateDto>>("states.json")
        val licenseTypes = readJson<List<LicenseTypeDto>>("license_types.json")
        val assessmentTypes = readJson<List<AssessmentTypeDto>>("assessment_types.json")
        val categories = readJson<List<CategoryDto>>("categories.json")
        val questionSets = readJson<List<QuestionSetDto>>("question_sets.json")
        val questionSetCategories = readJson<List<QuestionSetCategoryDto>>("question_set_categories.json")
        val questions = readJson<List<QuestionDto>>("questions.json")

        localDataSource.upsertAllReferenceData(
            states = states.map { it.toEntity() },
            licenseTypes = licenseTypes.map { it.toEntity() },
            assessmentTypes = assessmentTypes.map { it.toEntity() },
            categories = categories.map { it.toEntity() },
            questionSets = questionSets.map { it.toEntity() },
            questionSetCategories = questionSetCategories.map { it.toEntity() },
        )
        localDataSource.upsertQuestions(questions.map { it.toEntity(json) })
    }

    private suspend inline fun <reified T> readJson(name: String): T =
        json.decodeFromString(Res.readBytes("files/content/v1/$name").decodeToString())
}

const val BUNDLED_DATA_VERSION = 2
```

### Startup integration

`ensureSeeded()` is invoked once during app initialization, from the existing app-init coroutine that already configures RevenueCat and logging. The current splash/loading UI used during the Supabase initial sync stays — its trigger flips from "remote sync in progress" to "bundle seeding in progress." On a warm start (`localDataVersion >= BUNDLED_DATA_VERSION`), `ensureSeeded()` returns immediately and the splash dismisses without UI flicker.

### Image resolution

`SupabaseConfig.getStorageUrl(relativePath)` is replaced by `BundledImagePath.resolve(relativePath)`. The new helper returns a Compose-resource URI suitable for Coil's Compose Multiplatform integration. All call sites in the Compose UI layer migrate one-for-one.

## Files removed

- `shared/src/commonMain/.../data/remote/api/QuestionApi.kt`
- `shared/src/commonMain/.../data/remote/api/AppConfigApi.kt`
- `shared/src/commonMain/.../data/remote/api/SupabaseConfig.kt`
- `shared/src/commonMain/.../data/remote/dto/*.kt` (moved, not deleted — see below)
- `shared/src/commonMain/.../data/remote/mapper/QuestionDtoMapper.kt` (moved)
- `shared/src/commonMain/.../data/repository/DataSyncManager.kt`
- `shared/src/commonMain/.../data/di/FakeDataModule.kt`
- Supabase artifacts from `gradle/libs.versions.toml` and `shared/build.gradle.kts`
- `BuildKonfig.SUPABASE_URL` / `SUPABASE_KEY` declarations
- `supabase.url` / `supabase.key` lines in `local.properties` (and `local.properties.example` if present)
- `docs/SUPABASE_SCHEMA.md` is moved to `docs/archive/SUPABASE_SCHEMA.md` for history; the active path is removed from any README references.

## Files moved

- `data/remote/dto/*.kt` → `data/local/seed/dto/*.kt`
- `data/remote/mapper/QuestionDtoMapper.kt` → `data/local/seed/mapper/QuestionDtoMapper.kt`

## Files added

- `shared/src/commonMain/.../data/local/seed/BundledContentLoader.kt`
- `shared/src/commonMain/.../data/local/seed/BundledImagePath.kt`
- `shared/src/commonMain/.../data/local/seed/StateResourcesProvider.kt`
- `shared/src/commonMain/composeResources/files/content/v1/*.json` (7 files)
- `shared/src/commonMain/composeResources/files/content/v1/questions/<state>/*.png` (392 files)
- `scripts/export-supabase-bundle.main.kts`

## Files modified

- `QuestionRepositoryImpl` — drops `questionApi`, `dataSyncManager`, `syncPreferences` constructor params; the `syncQuestions`, `syncStates`, `fullSync` methods are removed.
- `QuestionRepository` interface — same methods removed.
- `DataModule` — removes `SupabaseClient`, `QuestionApi`, `AppConfigApi`, `DataSyncManager` registrations; adds `BundledContentLoader`.
- App-init code on Android (`Application.onCreate` / equivalent) and iOS (`App.kt` init coroutine) — invokes `BundledContentLoader.ensureSeeded()` once.
- Compose UI files that resolve question image URLs — swap `SupabaseConfig.getStorageUrl(...)` for `BundledImagePath.resolve(...)`.
- ViewModels / use cases that previously called `syncQuestions` or `fullSync` — call sites are deleted (now redundant once seeding is centralized).
- `GetStateResourcesUseCase` — switches from `QuestionApi.fetchStateResources(stateId)` to `StateResourcesProvider.getByState(stateId)`. Becomes synchronous in spirit; the existing `Result<List<StateResource>>` return is preserved for source compatibility.
- `README.md` — remove Supabase setup section.

## Migration steps (build-safe ordering)

Each step keeps the project compiling and runnable, so the migration can pause between steps without breaking trunk.

1. **Run export script** against current Supabase. Commit JSON + images.
2. **Add `BundledContentLoader`** alongside the existing remote code. Move DTOs and mapper from `data/remote/` to `data/local/seed/`. App still builds; remote code untouched.
3. **Wire `ensureSeeded()` into app startup**, gated to run before the existing Supabase sync. Manually verify the app boots with seeded data on Android and iOS.
4. **Bump `BUNDLED_DATA_VERSION` to 2**, swap Compose call sites to `BundledImagePath.resolve(...)`, drop `QuestionApi` / `DataSyncManager` from `QuestionRepositoryImpl`'s constructor. Smoke-test.
5. **Delete Supabase code:** remote APIs, `DataSyncManager`, `FakeDataModule`, `SupabaseConfig`. Drop SDK from `libs.versions.toml` and `shared/build.gradle.kts`. Remove `BuildKonfig` keys. Strip `supabase.*` from `local.properties`.
6. **Remove sync methods from `QuestionRepository` interface.** Compile errors point at every caller; replace each with a `BundledContentLoader.ensureSeeded()` call (rare) or just delete (most).
7. **Archive `docs/SUPABASE_SCHEMA.md`**, update `README.md`.

## Error handling

- A corrupt or missing bundled resource is a programmer error, not a runtime concern — resources ship in the binary. `ensureSeeded()` lets exceptions propagate to the existing top-level error UI. No silent fallbacks; we want the crash report rather than a blank app.
- Seeding occurs inside the existing transactional `upsertAllReferenceData` plus a single `upsertQuestions` call. Partial failure rolls back; `localDataVersion` is only bumped after the seeding block returns successfully.

## Testing

- **Unit:** `BundledContentLoader` against an in-memory Room build — verifies it parses bundled JSON, populates all 7 tables, respects FK ordering, and is idempotent (calling `ensureSeeded()` twice does not duplicate or fail). The test uses the actual production bundle to catch real-data shape regressions.
- **Manual smoke (Android + iOS, fresh install, airplane mode):** splash → home → start a mock test → answer a question that has an image → confirm the explanation and image render correctly.
- **Manual smoke (upgrade path):** install the prior Supabase build on a device, populate progress, then sideload the new build → confirm `BUNDLED_DATA_VERSION = 2` triggers a one-time re-seed, progress survives, and reads continue working without network.
- No new integration tests for Supabase (the deleted code has none today).

## Out-of-scope reminders

- The Supabase project remains live until the new app version reaches the App Store / Play Store. Pausing the project before that breaks current users.
- `app_config` keys are dropped because none are consumed in code; if a future feature needs runtime config, it should be added as a local constant or a separate, deliberate channel — not revived through this migration.
