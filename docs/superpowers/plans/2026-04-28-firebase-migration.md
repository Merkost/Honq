# Firebase Migration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace Supabase (Postgrest + Storage) with Firebase (Firestore for tabular data + Firebase Hosting for question images) as Honq's content backend, preserving the existing `QuestionRepository` interface, Room offline cache, and delta-sync semantics. Drop the Supabase SDK and credentials entirely. Stay on Firebase Spark (free) plan — Firebase Storage is *not* used because it now requires Blaze.

**Architecture:** New `FirestoreContentApi` and `FirebaseAppConfigApi` (same public surface as `QuestionApi` / `AppConfigApi`) read from Firestore collections that mirror the existing 9 Supabase tables. A new `HostedImageUrlBuilder` rewrites relative `image_url` values to point at Firebase Hosting (`https://honq-ac8e4.web.app/questions/<filename>`) in place of `SupabaseConfig.getStorageUrl`. A one-shot Python export script copies all Supabase tables into Firestore and writes all PNGs into the repo's `public/questions/` directory; images are then published with `firebase deploy --only hosting`. DI swaps `SupabaseClient` for `FirebaseFirestore`; Supabase deps and `BuildKonfig` fields are deleted at the end. Existing Crashlytics / Firebase init points are reused — no platform setup beyond adding one SDK.

**Tech Stack:** Kotlin Multiplatform, `dev.gitlive:firebase-firestore` (`gitlive-firebase = "2.4.0"`, matching the existing Crashlytics dep) — Firestore SDK on Android (transitive via gitlive) and `FirebaseFirestore` SPM product on iOS. Firebase Hosting (free Spark tier: 10 GB stored, 360 MB/day transfer; `Cache-Control: public, max-age=31536000, immutable` on `/questions/**`). Python 3.11 with `supabase` and `firebase-admin` libraries for the Firestore part of the one-shot export; the Firebase CLI for the Hosting part. Existing Room database and `QuestionRepositoryImpl` sync logic stay unchanged.

---

## Pre-flight: completed before plan execution started

These were resolved in the working session before Task 2 began. Captured here so anyone reading the plan in the future has the context.

1. **Firebase project:** `honq-ac8e4` (display name "Honq"), authenticated as `kosta0212@gmail.com`. Both Android (`com.merkost.honq`) and iOS (`com.merkost.honq`) apps are registered. `androidApp/google-services.json` and `iosApp/iosApp/GoogleService-Info.plist` already point at this project.
2. **Firestore:** `(default)` database created in `australia-southeast1`. **This region cannot be changed.** Security rules deployed (see `firestore.rules`) — public read of `is_active = true` rows, no client writes.
3. **Storage:** Firebase Storage is **not used.** Project remains on Spark (free) plan; Storage now requires Blaze. Question images are hosted via **Firebase Hosting** instead — same project, same free tier, single deploy command. Hosting site: `https://honq-ac8e4.web.app`.
4. **Supabase credentials for the export script:** still required at Task 11 — supply `SUPABASE_URL` and `SUPABASE_SERVICE_ROLE_KEY` (Project Settings → API → `service_role` in the Supabase dashboard). Service role bypasses RLS so the export captures inactive rows too. Anon key works as a fallback but silently skips `is_active = false` rows (the client filters those out anyway, so the loss is benign).

---

## File Structure

**Create:**
- `shared/src/commonMain/kotlin/com/merkost/honq/data/remote/api/FirestoreContentApi.kt` — replaces `QuestionApi`
- `shared/src/commonMain/kotlin/com/merkost/honq/data/remote/api/FirebaseAppConfigApi.kt` — replaces `AppConfigApi`
- `shared/src/commonMain/kotlin/com/merkost/honq/data/remote/api/HostedImageUrlBuilder.kt` — replaces `SupabaseConfig.getStorageUrl`; concatenates the Firebase Hosting base URL with the per-question relative path
- `shared/src/commonTest/kotlin/com/merkost/honq/data/remote/api/HostedImageUrlBuilderTest.kt` — unit tests for the URL builder
- `scripts/export-supabase-to-firestore.py` — one-shot export (Firestore + Hosting image staging)
- `scripts/requirements.txt` — Python deps for the script
- `docs/FIRESTORE_SCHEMA.md` — replaces `docs/SUPABASE_SCHEMA.md`
- `public/questions/.gitkeep` — created in pre-flight so the Hosting public dir exists in git; PNGs are added by the export script (and gitignored — see Task 2)

**Already in place from pre-flight (committed at the docs-baseline commit):**
- `firebase.json` — Firestore + Hosting config (Firestore points at `firestore.rules`, Hosting publishes from `public/` with year-long immutable cache on `/questions/**`)
- `firestore.rules` — security rules (already deployed via `firebase deploy --only firestore:rules`)
- `public/.gitkeep` — placeholder

**Modify:**
- `gradle/libs.versions.toml` — add `firebase-firestore` lib; drop `supabase` version + libs (preserve crashlytics, plugins)
- `shared/build.gradle.kts` — swap Supabase deps for Firestore; replace `SUPABASE_URL`/`SUPABASE_KEY` BuildKonfig fields with `FIREBASE_HOSTING_BASE_URL`
- `shared/src/commonMain/kotlin/com/merkost/honq/data/di/DataModule.kt` — swap `SupabaseClient` provider for `FirebaseFirestore`; provide new APIs
- `shared/src/commonMain/kotlin/com/merkost/honq/data/remote/mapper/QuestionDtoMapper.kt` — replace `SupabaseConfig.getStorageUrl(...)` calls with `HostedImageUrlBuilder.buildUrl(...)`
- `shared/src/commonMain/kotlin/com/merkost/honq/data/repository/QuestionRepositoryImpl.kt` — change constructor parameter type from `QuestionApi` to `FirestoreContentApi` (rename only, signatures stay the same)
- `shared/src/commonMain/kotlin/com/merkost/honq/data/repository/DataSyncManager.kt` — change constructor parameter type from `AppConfigApi` to `FirebaseAppConfigApi` (rename only)
- `iosApp/HonqApp.xcodeproj/project.pbxproj` — add `FirebaseFirestore` SPM product reference next to the existing `FirebaseCrashlytics` entry
- `local.properties` (untracked) — add `firebase.hosting.base.url=https://honq-ac8e4.web.app`; remove `supabase.url` / `supabase.key` lines
- `.gitignore` — add `public/questions/*.png` (images are deployed from local repo state but not committed — keeps the repo small; redeploy by re-running the export script + `firebase deploy --only hosting`)

**Delete:**
- `shared/src/commonMain/kotlin/com/merkost/honq/data/remote/api/SupabaseConfig.kt`
- `shared/src/commonMain/kotlin/com/merkost/honq/data/remote/api/QuestionApi.kt`
- `shared/src/commonMain/kotlin/com/merkost/honq/data/remote/api/AppConfigApi.kt`
- `docs/SUPABASE_SCHEMA.md` (after `docs/FIRESTORE_SCHEMA.md` is written)

DTOs (`QuestionDto`, `StateDto`, etc.) and entities are **kept as-is** — they describe shape, not source. We write Firestore documents using the same snake_case field names so the existing `@SerialName` annotations work directly with the gitlive Firestore decoder.

---

## Task 1: Pre-flight (already complete)

Resolved before plan execution started — see the "Pre-flight" section above. Firebase project `honq-ac8e4` confirmed, Firestore `(default)` database created in `australia-southeast1`, security rules deployed, Hosting selected over Storage to stay on Spark plan. Skip.

---

## Task 2: Commit Firebase configuration baseline and update `.gitignore`

**Files:**
- Modify: `.gitignore` — ignore `public/questions/*.png` (images are deployed from local repo state, not committed)
- Already on disk (uncommitted, from pre-flight): `firebase.json`, `firestore.rules`, `public/.gitkeep`

These files were authored during the pre-flight session and are already deployed for Firestore. This task commits them as the baseline so subsequent tasks have a clean starting point.

- [ ] **Step 1: Add the image-ignore line to `.gitignore`**

Append to `.gitignore`:
```
# Question images are deployed via Firebase Hosting from public/questions/.
# They're populated by scripts/export-supabase-to-firestore.py before deploy.
public/questions/*.png
```

- [ ] **Step 2: Confirm `firebase.json` matches expected shape**

Run:
```bash
cat firebase.json
```
Expected output:
```json
{
  "firestore": {
    "database": "(default)",
    "location": "australia-southeast1",
    "rules": "firestore.rules",
    "indexes": "firestore.indexes.json"
  },
  "hosting": {
    "public": "public",
    "ignore": [
      "firebase.json",
      "**/.*",
      "**/node_modules/**"
    ],
    "headers": [
      {
        "source": "/questions/**",
        "headers": [
          { "key": "Cache-Control", "value": "public, max-age=31536000, immutable" }
        ]
      }
    ]
  }
}
```

- [ ] **Step 3: Confirm Firestore rules are deployed**

Run:
```bash
firebase deploy --only firestore:rules --project honq-ac8e4 2>&1 | tail -5
```
Expected: `Deploy complete!` (idempotent — re-deploying the same rules is a no-op).

- [ ] **Step 4: Commit the baseline**

```bash
git add firebase.json firestore.rules public/.gitkeep .gitignore \
        docs/superpowers/research/2026-04-28-firebase-migration-assessment.md \
        docs/superpowers/plans/2026-04-28-firebase-migration.md
git commit -m "chore: add firebase config baseline for migration

- firebase.json: firestore (default) in australia-southeast1, hosting from public/
- firestore.rules: public read of active content, no client writes
- assessment + plan docs as the migration's source of truth"
```

---

## Task 3: Add Firebase Firestore dependency (Android side)

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `shared/build.gradle.kts`

**Note:** This task adds Firestore but does NOT yet remove Supabase deps — that happens in Task 14 once the migration is verified. The two backends temporarily coexist on the build path. We do NOT add `firebase-storage` because the project stays on Spark plan and uses Hosting for images.

- [ ] **Step 1: Add the Firestore entry to `libs.versions.toml`**

Modify `gradle/libs.versions.toml`. Under `[libraries]`, after the existing `firebase-crashlytics` line (currently line 102), add:

```toml
firebase-firestore = { module = "dev.gitlive:firebase-firestore", version.ref = "gitlive-firebase" }
```

No new `[versions]` entry needed — reuses `gitlive-firebase = "2.4.0"`.

- [ ] **Step 2: Add the dep to `shared/build.gradle.kts`**

Modify `shared/build.gradle.kts`. In the `commonMain.dependencies` block, after the existing `api(libs.firebase.crashlytics)` line (currently line 102), add:

```kotlin
            api(libs.firebase.firestore)
```

Use `api` (not `implementation`) to mirror the existing `firebase-crashlytics` pattern — needed so iOS interop sees the symbols.

- [ ] **Step 3: Build to confirm Android resolution**

Run:
```bash
./gradlew :shared:compileKotlinAndroid -q
```
Expected: BUILD SUCCESSFUL. Any "could not find" error means the module name or version is off — check `https://search.maven.org/search?q=g:dev.gitlive%20a:firebase-firestore`.

- [ ] **Step 4: Commit**

```bash
git add gradle/libs.versions.toml shared/build.gradle.kts
git commit -m "chore: add firebase firestore SDK dependency"
```

---

## Task 4: Add FirebaseFirestore SPM product to iOS

**Files:**
- Modify: `iosApp/HonqApp.xcodeproj/project.pbxproj`

The existing project already pulls `FirebaseCrashlytics` from the `firebase-ios-sdk` SPM package. Adding Firestore means adding one more product reference from the same package — Xcode does this with two clicks and edits `project.pbxproj` automatically. Doing it by hand is error-prone; use Xcode. We do NOT add `FirebaseStorage` because the project stays on Spark plan and uses Hosting for images.

- [ ] **Step 1: Open the project in Xcode**

Run:
```bash
open iosApp/HonqApp.xcodeproj
```

- [ ] **Step 2: Add the FirebaseFirestore product**

In Xcode:
1. Select the `iosApp` project in the navigator.
2. Select the `iosApp` target.
3. Open the **General** tab → **Frameworks, Libraries, and Embedded Content** section.
4. Click **+** → **firebase-ios-sdk** package → tick `FirebaseFirestore` → click **Add**.

- [ ] **Step 3: Verify project.pbxproj was edited correctly**

Run:
```bash
grep -E "FirebaseFirestore" iosApp/HonqApp.xcodeproj/project.pbxproj | head -10
```
Expected: at least 4 lines mentioning `FirebaseFirestore`, mirroring the structure of the existing `FirebaseCrashlytics` entries (a `PBXBuildFile`, a Frameworks group entry, a `XCSwiftPackageProductDependency`, and a `packageProductDependencies` reference).

- [ ] **Step 4: Build the iOS framework via the KMP wrapper to confirm linkage**

Run:
```bash
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64 -q
```
Expected: BUILD SUCCESSFUL. (This builds the KMP framework; Xcode-side linking is exercised in Task 13.)

- [ ] **Step 5: Commit**

```bash
git add iosApp/HonqApp.xcodeproj/project.pbxproj
git commit -m "chore: add FirebaseFirestore SPM product on iOS"
```

---

## Task 5: Implement `HostedImageUrlBuilder` (TDD)

**Files:**
- Create: `shared/src/commonMain/kotlin/com/merkost/honq/data/remote/api/HostedImageUrlBuilder.kt`
- Create: `shared/src/commonTest/kotlin/com/merkost/honq/data/remote/api/HostedImageUrlBuilderTest.kt`

This is a pure transform (relative path → public Hosting URL). TDD-able and worth covering since it's called for every question.

URL pattern is a simple concatenation:
```
https://honq-ac8e4.web.app/<relative path>
```

The base URL is read from `BuildKonfig.FIREBASE_HOSTING_BASE_URL` (added in Task 7). Filenames currently stored in `image_url` look like `questions/nsw-001.png`. Firebase Hosting serves the `public/` directory at the site root, so a file at `public/questions/nsw-001.png` becomes `https://honq-ac8e4.web.app/questions/nsw-001.png` — no transformation needed beyond stripping a leading slash. Filenames are well-formed (alphanumeric, hyphens, dots) so no URL-encoding is required.

- [ ] **Step 1: Write the failing tests**

Create `shared/src/commonTest/kotlin/com/merkost/honq/data/remote/api/HostedImageUrlBuilderTest.kt`:

```kotlin
package com.merkost.honq.data.remote.api

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class HostedImageUrlBuilderTest {

    private val builder = HostedImageUrlBuilder(baseUrl = "https://honq-ac8e4.web.app")

    @Test
    fun returns_null_for_null_input() {
        assertNull(builder.buildUrl(null))
    }

    @Test
    fun returns_null_for_blank_input() {
        assertNull(builder.buildUrl(""))
        assertNull(builder.buildUrl("   "))
    }

    @Test
    fun passes_through_absolute_https_url() {
        val abs = "https://example.com/img.png"
        assertEquals(abs, builder.buildUrl(abs))
    }

    @Test
    fun passes_through_absolute_http_url() {
        val abs = "http://example.com/img.png"
        assertEquals(abs, builder.buildUrl(abs))
    }

    @Test
    fun builds_url_for_simple_path() {
        assertEquals(
            "https://honq-ac8e4.web.app/questions/nsw-001.png",
            builder.buildUrl("questions/nsw-001.png")
        )
    }

    @Test
    fun strips_leading_slash_from_path() {
        assertEquals(
            "https://honq-ac8e4.web.app/questions/nsw-001.png",
            builder.buildUrl("/questions/nsw-001.png")
        )
    }

    @Test
    fun strips_trailing_slash_from_base_url() {
        val builderWithSlash = HostedImageUrlBuilder(baseUrl = "https://honq-ac8e4.web.app/")
        assertEquals(
            "https://honq-ac8e4.web.app/questions/nsw-001.png",
            builderWithSlash.buildUrl("questions/nsw-001.png")
        )
    }
}
```

- [ ] **Step 2: Run the tests to confirm they fail with "unresolved reference"**

Run:
```bash
./gradlew :shared:compileTestKotlinAndroidUnitTest -q 2>&1 | tail -20
```
Expected: compile failure mentioning `HostedImageUrlBuilder`.

- [ ] **Step 3: Implement `HostedImageUrlBuilder`**

Create `shared/src/commonMain/kotlin/com/merkost/honq/data/remote/api/HostedImageUrlBuilder.kt`:

```kotlin
package com.merkost.honq.data.remote.api

class HostedImageUrlBuilder(baseUrl: String) {

    private val base: String = baseUrl.trimEnd('/')

    fun buildUrl(relativePath: String?): String? {
        if (relativePath.isNullOrBlank()) return null
        if (relativePath.startsWith("http://") || relativePath.startsWith("https://")) {
            return relativePath
        }
        return "$base/${relativePath.trimStart('/')}"
    }
}
```

- [ ] **Step 4: Run the tests to confirm they pass**

Run:
```bash
./gradlew :shared:testDebugUnitTest --tests "*HostedImageUrlBuilderTest*" -q
```
Expected: all 7 tests pass.

- [ ] **Step 5: Commit**

```bash
git add shared/src/commonMain/kotlin/com/merkost/honq/data/remote/api/HostedImageUrlBuilder.kt \
        shared/src/commonTest/kotlin/com/merkost/honq/data/remote/api/HostedImageUrlBuilderTest.kt
git commit -m "feat: add HostedImageUrlBuilder for Firebase Hosting image URLs"
```

---

## Task 6: Wire `HostedImageUrlBuilder` through the mapper

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/merkost/honq/data/remote/mapper/QuestionDtoMapper.kt`

The mapper currently calls `SupabaseConfig.getStorageUrl(imageUrl)` (lines 35 and 50). Replace with a call into the new `HostedImageUrlBuilder`. The mapper file uses top-level functions, so we keep that style with a single `private val` builder constructed from `BuildKonfig`.

- [ ] **Step 1: Swap imports and add the lazy builder**

Modify `QuestionDtoMapper.kt`. Replace:

```kotlin
import com.merkost.honq.data.remote.api.SupabaseConfig
```

with:

```kotlin
import com.merkost.honq.BuildKonfig
import com.merkost.honq.data.remote.api.HostedImageUrlBuilder

private val imageUrlBuilder by lazy { HostedImageUrlBuilder(BuildKonfig.FIREBASE_HOSTING_BASE_URL) }
```

- [ ] **Step 2: Replace the two call sites**

In `QuestionDto.toDomain()` (line 35), change:
```kotlin
    imageUrl = SupabaseConfig.getStorageUrl(imageUrl),
```
to:
```kotlin
    imageUrl = imageUrlBuilder.buildUrl(imageUrl),
```

In `QuestionDto.toEntity()` (line 50), make the same change.

- [ ] **Step 3: Verify the mapper still compiles**

Run:
```bash
./gradlew :shared:compileKotlinAndroid -q
```
Expected: BUILD SUCCESSFUL. (Note: at this point `BuildKonfig.FIREBASE_HOSTING_BASE_URL` doesn't exist yet — see Task 7. If this step fails with "unresolved reference: FIREBASE_HOSTING_BASE_URL", proceed to Task 7 and come back.)

- [ ] **Step 4: Commit**

```bash
git add shared/src/commonMain/kotlin/com/merkost/honq/data/remote/mapper/QuestionDtoMapper.kt
git commit -m "refactor: route question image URLs through HostedImageUrlBuilder"
```

---

## Task 7: Add `FIREBASE_HOSTING_BASE_URL` to BuildKonfig

**Files:**
- Modify: `shared/build.gradle.kts`
- Modify: `local.properties` (untracked)

Mirrors the existing `SUPABASE_URL` pattern — read from `local.properties`, expose via `BuildKonfig`. We do NOT remove the Supabase fields yet; they stay until Task 14.

We also use `https://honq-ac8e4.web.app` as the default value baked into `defaultConfigs` so the app builds even if `local.properties` is missing the line — that URL is public anyway, so there's no leak risk.

- [ ] **Step 1: Add the BuildKonfig field**

Modify `shared/build.gradle.kts`. In the `defaultConfigs { ... }` block (around line 150), after the existing `SUPABASE_KEY` line, add:

```kotlin
        buildConfigField(STRING, "FIREBASE_HOSTING_BASE_URL", getLocalProperty("firebase.hosting.base.url", "https://honq-ac8e4.web.app"))
```

- [ ] **Step 2: Add the value to `local.properties`**

In `local.properties`, add:

```properties
firebase.hosting.base.url=https://honq-ac8e4.web.app
```

(Optional — the default in `build.gradle.kts` covers this if omitted. Override here only if you set up a custom domain later.)

- [ ] **Step 3: Build to confirm BuildKonfig regenerates**

Run:
```bash
./gradlew :shared:generateBuildKonfig :shared:compileKotlinAndroid -q
```
Expected: BUILD SUCCESSFUL. The mapper from Task 6 now resolves `BuildKonfig.FIREBASE_HOSTING_BASE_URL`.

- [ ] **Step 4: Commit**

```bash
git add shared/build.gradle.kts
git commit -m "chore: expose firebase.hosting.base.url via BuildKonfig"
```

---

## Task 8: Implement `FirebaseAppConfigApi`

**Files:**
- Create: `shared/src/commonMain/kotlin/com/merkost/honq/data/remote/api/FirebaseAppConfigApi.kt`

Public surface to match `AppConfigApi` exactly:
```kotlin
suspend fun fetchDataVersion(): Int
suspend fun fetchString(key: String): String?
```

Firestore layout: a single collection `app_config` where each document is keyed by `key` (matching the Supabase row shape) and has a single `value` field of mixed type (number / string / boolean). This is the most direct port — keep the same data shape, just move the storage.

- [ ] **Step 1: Implement the API**

Create `shared/src/commonMain/kotlin/com/merkost/honq/data/remote/api/FirebaseAppConfigApi.kt`:

```kotlin
package com.merkost.honq.data.remote.api

import dev.gitlive.firebase.firestore.FirebaseFirestore
import org.kimplify.cedar.logging.Cedar

class FirebaseAppConfigApi(
    private val firestore: FirebaseFirestore
) {
    suspend fun fetchDataVersion(): Int = try {
        Cedar.tag("FbAppConfigApi").d("Fetching data_version from app_config...")
        val v = fetchInt("data_version") ?: 0
        Cedar.tag("FbAppConfigApi").d("Fetched data_version=$v")
        v
    } catch (e: Exception) {
        Cedar.tag("FbAppConfigApi").e("fetchDataVersion failed, returning 0: ${e.message}", e)
        0
    }

    suspend fun fetchString(key: String): String? = try {
        Cedar.tag("FbAppConfigApi").d("Fetching $key from app_config...")
        val snap = firestore.collection("app_config").document(key).get()
        val value = snap.get<String?>("value")
        Cedar.tag("FbAppConfigApi").d("Fetched $key=$value")
        value
    } catch (e: Exception) {
        Cedar.tag("FbAppConfigApi").e("fetchString($key) failed: ${e.message}", e)
        null
    }

    private suspend fun fetchInt(key: String): Int? {
        val snap = firestore.collection("app_config").document(key).get()
        // Firestore returns numbers as Long via the gitlive wrapper; tolerate both.
        return snap.get<Long?>("value")?.toInt()
    }
}
```

- [ ] **Step 2: Verify it compiles**

Run:
```bash
./gradlew :shared:compileKotlinAndroid -q
```
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add shared/src/commonMain/kotlin/com/merkost/honq/data/remote/api/FirebaseAppConfigApi.kt
git commit -m "feat: add FirebaseAppConfigApi to read app_config from Firestore"
```

---

## Task 9: Implement `FirestoreContentApi`

**Files:**
- Create: `shared/src/commonMain/kotlin/com/merkost/honq/data/remote/api/FirestoreContentApi.kt`

Replicates the public surface of `QuestionApi` (10 methods). The DTOs already use snake_case `@SerialName` annotations; the gitlive Firestore decoder honors kotlinx-serialization annotations, so DTOs decode unchanged.

Firestore-vs-Postgres differences to handle:
- **`gt` (greater-than)** for delta sync: Firestore supports `whereGreaterThan(field, value)` directly.
- **`is_active = true`** filter: Firestore supports `whereEqualTo`.
- **Multi-field equality** (e.g. `state_id == X AND is_active == true`): two `whereEqualTo` calls. Firestore requires a composite index for combined inequality + equality, but plain equality + equality on different fields does NOT need an index.
- **Empty results on optional fetches** (the existing `fetchQuestionSetCategories` swallows errors): keep the same try/catch around the sub-fetch.

- [ ] **Step 1: Implement the API**

Create `shared/src/commonMain/kotlin/com/merkost/honq/data/remote/api/FirestoreContentApi.kt`:

```kotlin
package com.merkost.honq.data.remote.api

import com.merkost.honq.data.remote.dto.AssessmentTypeDto
import com.merkost.honq.data.remote.dto.CategoryDto
import com.merkost.honq.data.remote.dto.LicenseTypeDto
import com.merkost.honq.data.remote.dto.QuestionDto
import com.merkost.honq.data.remote.dto.QuestionSetCategoryDto
import com.merkost.honq.data.remote.dto.QuestionSetDto
import com.merkost.honq.data.remote.dto.StateDto
import com.merkost.honq.data.remote.dto.StateResourceDto
import dev.gitlive.firebase.firestore.FirebaseFirestore
import org.kimplify.cedar.logging.Cedar

class FirestoreContentApi(
    private val firestore: FirebaseFirestore
) {
    suspend fun fetchAllQuestions(): List<QuestionDto> {
        Cedar.tag("FsContentApi").d("Fetching all questions...")
        val result = firestore.collection("questions").get().documents
            .map { it.data<QuestionDto>() }
        Cedar.tag("FsContentApi").d("Fetched ${result.size} questions")
        return result
    }

    suspend fun fetchQuestionsByQuestionSet(questionSetId: String): List<QuestionDto> {
        Cedar.tag("FsContentApi").d("Fetching questions for questionSet=$questionSetId...")
        val result = firestore.collection("questions")
            .where { ("question_set_id" equalTo questionSetId) and ("is_active" equalTo true) }
            .get().documents
            .map { it.data<QuestionDto>() }
        Cedar.tag("FsContentApi").d("Fetched ${result.size} questions for questionSet=$questionSetId")
        return result
    }

    suspend fun fetchUpdatedQuestions(questionSetId: String, since: String): List<QuestionDto> {
        Cedar.tag("FsContentApi").d("Fetching updated questions for questionSet=$questionSetId since=$since...")
        val result = firestore.collection("questions")
            .where { ("question_set_id" equalTo questionSetId) and ("updated_at" greaterThan since) }
            .get().documents
            .map { it.data<QuestionDto>() }
        Cedar.tag("FsContentApi").d("Fetched ${result.size} updated questions for questionSet=$questionSetId")
        return result
    }

    suspend fun fetchStates(includeInactive: Boolean = false): List<StateDto> {
        Cedar.tag("FsContentApi").d("Fetching states (includeInactive=$includeInactive)...")
        val coll = firestore.collection("states")
        val q = if (includeInactive) coll.get() else coll.where { "is_active" equalTo true }.get()
        val result = q.documents.map { it.data<StateDto>() }
        Cedar.tag("FsContentApi").d("Fetched ${result.size} states")
        return result
    }

    suspend fun fetchLicenseTypes(includeInactive: Boolean = false): List<LicenseTypeDto> {
        Cedar.tag("FsContentApi").d("Fetching license types (includeInactive=$includeInactive)...")
        val coll = firestore.collection("license_types")
        val q = if (includeInactive) coll.get() else coll.where { "is_active" equalTo true }.get()
        val result = q.documents.map { it.data<LicenseTypeDto>() }
        Cedar.tag("FsContentApi").d("Fetched ${result.size} license types")
        return result
    }

    suspend fun fetchAssessmentTypes(includeInactive: Boolean = false): List<AssessmentTypeDto> {
        Cedar.tag("FsContentApi").d("Fetching assessment types (includeInactive=$includeInactive)...")
        val coll = firestore.collection("assessment_types")
        val q = if (includeInactive) coll.get() else coll.where { "is_active" equalTo true }.get()
        val result = q.documents.map { it.data<AssessmentTypeDto>() }
        Cedar.tag("FsContentApi").d("Fetched ${result.size} assessment types")
        return result
    }

    suspend fun fetchQuestionSets(
        stateId: String? = null,
        includeInactive: Boolean = false
    ): List<QuestionSetDto> {
        Cedar.tag("FsContentApi").d("Fetching question sets (stateId=$stateId, includeInactive=$includeInactive)...")
        val docs = firestore.collection("question_sets")
            .where {
                val parts = mutableListOf<dev.gitlive.firebase.firestore.Filter>()
                if (stateId != null) parts += "state_id" equalTo stateId
                if (!includeInactive) parts += "is_active" equalTo true
                if (parts.isEmpty()) all() else parts.reduce { acc, f -> acc and f }
            }
            .get().documents
        val result = docs.map { it.data<QuestionSetDto>() }
        Cedar.tag("FsContentApi").d("Fetched ${result.size} question sets")
        return result
    }

    suspend fun fetchQuestionSetCategories(
        questionSetId: String? = null,
        includeInactive: Boolean = false
    ): List<QuestionSetCategoryDto> {
        Cedar.tag("FsContentApi").d("Fetching question set categories (questionSetId=$questionSetId)...")
        val docs = firestore.collection("question_set_categories")
            .where {
                val parts = mutableListOf<dev.gitlive.firebase.firestore.Filter>()
                if (questionSetId != null) parts += "question_set_id" equalTo questionSetId
                if (!includeInactive) parts += "is_active" equalTo true
                if (parts.isEmpty()) all() else parts.reduce { acc, f -> acc and f }
            }
            .get().documents
        val result = docs.map { it.data<QuestionSetCategoryDto>() }
        Cedar.tag("FsContentApi").d("Fetched ${result.size} question set categories")
        return result
    }

    suspend fun fetchCategories(includeInactive: Boolean = false): List<CategoryDto> {
        Cedar.tag("FsContentApi").d("Fetching categories (includeInactive=$includeInactive)...")
        val coll = firestore.collection("categories")
        val q = if (includeInactive) coll.get() else coll.where { "is_active" equalTo true }.get()
        val result = q.documents.map { it.data<CategoryDto>() }
        Cedar.tag("FsContentApi").d("Fetched ${result.size} categories")
        return result
    }

    suspend fun fetchStateResources(stateId: String): List<StateResourceDto> {
        Cedar.tag("FsContentApi").d("Fetching state resources for stateId=$stateId...")
        val result = firestore.collection("state_resources")
            .where { ("state_id" equalTo stateId) and ("is_active" equalTo true) }
            .get().documents
            .map { it.data<StateResourceDto>() }
        Cedar.tag("FsContentApi").d("Fetched ${result.size} state resources for stateId=$stateId")
        return result
    }
}
```

> **gitlive query DSL note:** The gitlive wrapper (`dev.gitlive:firebase-firestore` 2.4.0) exposes a Kotlin `Filter` DSL where you write `"field" equalTo value` and combine with `and` / `or`. If the API in your installed version differs (the DSL has evolved across minor versions), check `https://github.com/GitLiveApp/firebase-kotlin-sdk/blob/master/firebase-firestore/README.md` and adjust. The semantics are identical — a `WHERE field = value` clause — only the surface syntax changes. The fallback is the older flat form: `firestore.collection("...").whereEqualTo("field", value).get()`.

- [ ] **Step 2: Verify compilation**

Run:
```bash
./gradlew :shared:compileKotlinAndroid -q
```
Expected: BUILD SUCCESSFUL. If you hit "unresolved reference: equalTo" / "and" / "Filter", fall back to the non-DSL form (e.g. `coll.whereEqualTo("question_set_id", questionSetId).whereEqualTo("is_active", true)`) — same semantics.

- [ ] **Step 3: Commit**

```bash
git add shared/src/commonMain/kotlin/com/merkost/honq/data/remote/api/FirestoreContentApi.kt
git commit -m "feat: add FirestoreContentApi mirroring QuestionApi surface"
```

---

## Task 10: Wire DI to use Firestore APIs

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/merkost/honq/data/di/DataModule.kt`
- Modify: `shared/src/commonMain/kotlin/com/merkost/honq/data/repository/QuestionRepositoryImpl.kt`
- Modify: `shared/src/commonMain/kotlin/com/merkost/honq/data/repository/DataSyncManager.kt`

After this task, the app reads from Firestore. Supabase client + APIs still exist in the codebase but are unreachable — they're cleaned up in Task 12.

- [ ] **Step 1: Swap repository constructor parameter types**

In `QuestionRepositoryImpl.kt` (line 27):
```kotlin
    private val questionApi: QuestionApi,
```
becomes:
```kotlin
    private val questionApi: FirestoreContentApi,
```

Update the import at the top:
```kotlin
import com.merkost.honq.data.remote.api.QuestionApi
```
becomes:
```kotlin
import com.merkost.honq.data.remote.api.FirestoreContentApi
```

No method-body changes — `FirestoreContentApi`'s public surface matches `QuestionApi`.

- [ ] **Step 2: Swap `DataSyncManager` parameter type**

In `DataSyncManager.kt` (line 4 and constructor):
```kotlin
import com.merkost.honq.data.remote.api.AppConfigApi
```
becomes:
```kotlin
import com.merkost.honq.data.remote.api.FirebaseAppConfigApi
```

And:
```kotlin
class DataSyncManager(
    private val appConfigApi: AppConfigApi,
    private val syncPreferences: SyncPreferences
)
```
becomes:
```kotlin
class DataSyncManager(
    private val appConfigApi: FirebaseAppConfigApi,
    private val syncPreferences: SyncPreferences
)
```

No method-body changes.

- [ ] **Step 3: Replace the DI bindings**

In `DataModule.kt`, replace lines 47–49:
```kotlin
    single<SupabaseClient> { SupabaseConfig.createClient() }
    single { QuestionApi(get()) }
    single { AppConfigApi(get()) }
```
with:
```kotlin
    single { dev.gitlive.firebase.Firebase.firestore }
    single { FirestoreContentApi(get()) }
    single { FirebaseAppConfigApi(get()) }
```

Update imports at the top of `DataModule.kt`:
- Remove: `import com.merkost.honq.data.remote.api.AppConfigApi`
- Remove: `import com.merkost.honq.data.remote.api.QuestionApi`
- Remove: `import com.merkost.honq.data.remote.api.SupabaseConfig`
- Remove: `import io.github.jan.supabase.SupabaseClient`
- Add: `import com.merkost.honq.data.remote.api.FirebaseAppConfigApi`
- Add: `import com.merkost.honq.data.remote.api.FirestoreContentApi`
- Add: `import dev.gitlive.firebase.firestore.firestore`

(`Firebase.firestore` is the gitlive accessor, analogous to `Firebase.crashlytics` already used in `CrashReportingTree.kt`.)

- [ ] **Step 4: Build common + Android**

Run:
```bash
./gradlew :shared:compileKotlinAndroid -q
```
Expected: BUILD SUCCESSFUL. If you see references to `QuestionApi` or `AppConfigApi` from other files (test fakes, etc.), grep for them — at the time of writing, only `DataModule.kt`, `QuestionRepositoryImpl.kt`, and `DataSyncManager.kt` reference them.

- [ ] **Step 5: Commit**

```bash
git add shared/src/commonMain/kotlin/com/merkost/honq/data/di/DataModule.kt \
        shared/src/commonMain/kotlin/com/merkost/honq/data/repository/QuestionRepositoryImpl.kt \
        shared/src/commonMain/kotlin/com/merkost/honq/data/repository/DataSyncManager.kt
git commit -m "refactor: wire DataModule to FirestoreContentApi and FirebaseAppConfigApi"
```

---

## Task 11: Write the one-shot Supabase → Firebase export script and run it

**Files:**
- Create: `scripts/export-supabase-to-firestore.py`
- Create: `scripts/requirements.txt`
- Modify: `.gitignore` — ensure `.venv*/` and Firebase service-account JSON files are ignored

This is a Python script run **once** by the developer (not in CI). It reads from Supabase Postgres + Storage and writes Firestore docs + Hosting-staged PNGs (in `public/questions/`). It is idempotent — re-running it overwrites the same documents and re-stages the same image files. After the script finishes, a separate `firebase deploy --only hosting` command publishes the images.

- [ ] **Step 1: Create `scripts/requirements.txt`**

Create `scripts/requirements.txt`:

```
supabase==2.7.4
firebase-admin==6.5.0
```

- [ ] **Step 2: Create the export script**

Create `scripts/export-supabase-to-firestore.py`:

```python
#!/usr/bin/env python3
"""
One-shot exporter: Supabase Postgres + Storage → Firestore + Firebase Hosting (public/questions).

Reads credentials from local.properties (same file the app uses):
  supabase.url, supabase.service.role.key, firebase.project.id

Requires GOOGLE_APPLICATION_CREDENTIALS env var pointing at a Firebase service-account JSON
(download from Firebase Console → Project Settings → Service accounts → Generate new private key).
The service account is used ONLY for Firestore writes; image staging is plain filesystem writes
into public/questions/ and the actual Hosting deploy is done separately via the firebase CLI
(which uses your interactive login, not this service account).

Usage:
  GOOGLE_APPLICATION_CREDENTIALS=/path/to/sa.json python scripts/export-supabase-to-firestore.py

After the script completes, publish the images:
  firebase deploy --only hosting --project <firebase.project.id>

Idempotent: re-running overwrites the same docs and re-stages the same image files.
"""

import os
import sys
from pathlib import Path

import firebase_admin
from firebase_admin import credentials, firestore
from supabase import Client, create_client

REPO_ROOT = Path(__file__).resolve().parent.parent
LOCAL_PROPERTIES = REPO_ROOT / "local.properties"
HOSTING_QUESTIONS_DIR = REPO_ROOT / "public" / "questions"

TABLES = [
    "states",
    "license_types",
    "assessment_types",
    "categories",
    "question_sets",
    "question_set_categories",
    "questions",
    "state_resources",
    "app_config",
]
SUPABASE_BUCKET = "questions"


def load_local_properties() -> dict[str, str]:
    if not LOCAL_PROPERTIES.exists():
        sys.exit(f"local.properties not found at {LOCAL_PROPERTIES}")
    out: dict[str, str] = {}
    for raw in LOCAL_PROPERTIES.read_text().splitlines():
        line = raw.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        k, v = line.split("=", 1)
        out[k.strip()] = v.strip()
    return out


def doc_id_for(table: str, row: dict) -> str:
    # app_config keyed by "key"; question_set_categories has a composite key; everything else by "id".
    if table == "app_config":
        return str(row["key"])
    if table == "question_set_categories":
        return f"{row['question_set_id']}__{row['category_id']}"
    return str(row["id"])


def export_tables(supabase: Client, fs: firestore.Client) -> None:
    for table in TABLES:
        print(f"[tables] exporting {table}...")
        offset = 0
        page = 1000
        total = 0
        while True:
            resp = supabase.table(table).select("*").range(offset, offset + page - 1).execute()
            rows = resp.data or []
            if not rows:
                break
            batch = fs.batch()
            for row in rows:
                doc_ref = fs.collection(table).document(doc_id_for(table, row))
                batch.set(doc_ref, row)
            batch.commit()
            total += len(rows)
            if len(rows) < page:
                break
            offset += page
        print(f"[tables] {table}: {total} docs written")


def stage_images(supabase: Client) -> None:
    print(f"[hosting] listing supabase bucket '{SUPABASE_BUCKET}'...")
    listing = supabase.storage.from_(SUPABASE_BUCKET).list(path="", options={"limit": 5000})
    if not listing:
        print("[hosting] empty bucket — skipping")
        return
    HOSTING_QUESTIONS_DIR.mkdir(parents=True, exist_ok=True)
    count = 0
    for entry in listing:
        name = entry["name"]
        if name.endswith("/"):
            continue
        print(f"[hosting] staging {name}...")
        blob_bytes = supabase.storage.from_(SUPABASE_BUCKET).download(name)
        out_path = HOSTING_QUESTIONS_DIR / name
        out_path.parent.mkdir(parents=True, exist_ok=True)
        out_path.write_bytes(blob_bytes)
        count += 1
    print(f"[hosting] staged {count} files in {HOSTING_QUESTIONS_DIR}")
    print(f"[hosting] now run: firebase deploy --only hosting --project <firebase.project.id>")


def main() -> None:
    props = load_local_properties()
    supabase_url = props.get("supabase.url")
    supabase_key = props.get("supabase.service.role.key") or props.get("supabase.key")
    fb_project_id = props.get("firebase.project.id")

    missing = [k for k, v in {
        "supabase.url": supabase_url,
        "supabase.service.role.key (or supabase.key)": supabase_key,
        "firebase.project.id": fb_project_id,
    }.items() if not v]
    if missing:
        sys.exit("Missing required local.properties keys: " + ", ".join(missing))

    if "GOOGLE_APPLICATION_CREDENTIALS" not in os.environ:
        sys.exit("Set GOOGLE_APPLICATION_CREDENTIALS to a Firebase service-account JSON path")

    print(f"Source: {supabase_url}")
    print(f"Dest:   firestore project={fb_project_id}; images → {HOSTING_QUESTIONS_DIR}")

    supabase = create_client(supabase_url, supabase_key)
    cred = credentials.ApplicationDefault()
    firebase_admin.initialize_app(cred, {"projectId": fb_project_id})
    fs = firestore.client()

    export_tables(supabase, fs)
    stage_images(supabase)
    print("Done. Run `firebase deploy --only hosting` to publish images.")


if __name__ == "__main__":
    main()
```

- [ ] **Step 3: Update `.gitignore` for the venv and service-account JSON**

Append to `.gitignore` (if not already present):
```
.venv*/
*-firebase-sa*.json
firebase-adminsdk-*.json
```

- [ ] **Step 4: Add `firebase.project.id` to `local.properties`**

Confirm or add:
```properties
firebase.project.id=honq-ac8e4
supabase.service.role.key=<paste from Supabase dashboard → Project Settings → API>
```

- [ ] **Step 5: Install Python deps and run the export**

```bash
python3 -m venv .venv-export
source .venv-export/bin/activate
pip install -r scripts/requirements.txt
GOOGLE_APPLICATION_CREDENTIALS=/path/to/firebase-sa.json python scripts/export-supabase-to-firestore.py
deactivate
```

Expected stdout: `[tables] <name>: N docs written` for each of 9 tables, then `[hosting] staged N files in .../public/questions`.

- [ ] **Step 6: Verify Firestore data in the Firebase Console**

Manual check (Firebase Console → Firestore Database):
1. 9 collections present with expected doc counts (`questions` should have hundreds; smaller tables 1–10 each).
2. Open one question doc and one state doc; confirm field names are snake_case (`question_set_id`, `is_active`, etc.) — matches the DTO `@SerialName` annotations.

- [ ] **Step 7: Deploy the staged images via Firebase Hosting**

```bash
firebase deploy --only hosting --project honq-ac8e4
```

Expected: `Deploy complete!` and a `Hosting URL` line. Verify by opening one image in a browser:
```
https://honq-ac8e4.web.app/questions/<one-of-the-filenames>.png
```
Expected: image renders. If 404, the file path under `public/` doesn't match the URL — confirm `public/questions/<name>.png` exists locally.

- [ ] **Step 8: Commit the script and gitignore changes**

```bash
git add scripts/export-supabase-to-firestore.py scripts/requirements.txt .gitignore
git commit -m "chore: add one-shot supabase to firebase export script"
```

(The `.venv-export/` and the service-account JSON are gitignored. The staged PNGs in `public/questions/` are also gitignored — they live on disk during the migration so `firebase deploy --only hosting` can publish them, but never enter git history.)

---

## Task 12: Smoke test the app on Android

**Files:** none (verification task)

- [ ] **Step 1: Wipe the local Room database to force a fresh sync**

On a connected Android emulator/device:
```bash
adb shell pm clear com.merkost.honq
```
(Replace package name if applicationId differs — check `androidApp/build.gradle.kts`.)

- [ ] **Step 2: Build and install**

Run:
```bash
./gradlew :androidApp:installDebug
```
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Launch the app and walk the golden path**

Manual:
1. Cold-start the app.
2. Confirm the splash / sync screen completes without a network error.
3. Open a category list — confirm questions load.
4. Open a single question — confirm the **image** loads (this exercises `FirebaseImageUrlBuilder` end-to-end).
5. Open the mock test — confirm 45 questions load.
6. Filter by `app_config.min_app_version` if surfaced anywhere — confirm `FirebaseAppConfigApi` works.

- [ ] **Step 4: Inspect logcat for errors**

Run:
```bash
adb logcat -d | grep -E "FsContentApi|FbAppConfigApi|QuestionRepo|DataSync" | tail -100
```
Expected: `Fetched N` log lines, no exceptions. If you see `PERMISSION_DENIED`, the security rules deploy in Task 11 Step 5 did not land — re-run it.

- [ ] **Step 5: No commit (verification step)**

Stop here and address any issues before proceeding. Common issues:
- **Image fails to load:** open the URL in a browser. If 403, security rules are wrong; if 404, the export missed the file or the path encoding differs. Add the failing path to a unit test in `FirebaseImageUrlBuilderTest`, fix, re-run Task 5 Step 5, then re-test.
- **`Failed to decode QuestionDto`:** field name mismatch — Firestore got camelCase but DTO expects snake_case. Check the export script wrote `question_set_id` not `questionSetId`.

---

## Task 13: Smoke test the app on iOS

**Files:** none (verification task)

- [ ] **Step 1: Build and run on simulator**

Run:
```bash
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
open iosApp/HonqApp.xcodeproj
```
In Xcode: select the iOS Simulator (e.g. iPhone 15) and press ⌘R.

- [ ] **Step 2: Walk the golden path (same as Task 12 Step 3)**

- [ ] **Step 3: Inspect Xcode console for Firestore errors**

Filter Xcode console for `FIRFirestore` and `Firestore`. Expected: no `PERMISSION_DENIED`, no `Decoding error`.

- [ ] **Step 4: No commit**

Address issues; the most likely iOS-specific failure is missing `FirebaseFirestore` / `FirebaseStorage` SPM products (verified in Task 4 Step 3) or a missing `GoogleService-Info.plist` in the app bundle.

---

## Task 14: Remove Supabase code, deps, and BuildKonfig fields

**Files:**
- Delete: `shared/src/commonMain/kotlin/com/merkost/honq/data/remote/api/SupabaseConfig.kt`
- Delete: `shared/src/commonMain/kotlin/com/merkost/honq/data/remote/api/QuestionApi.kt`
- Delete: `shared/src/commonMain/kotlin/com/merkost/honq/data/remote/api/AppConfigApi.kt`
- Modify: `gradle/libs.versions.toml`
- Modify: `shared/build.gradle.kts`
- Modify: `local.properties` (untracked)

Only do this **after both platforms smoke test green** in Tasks 12 and 13. Once committed, rolling back means reverting commits — there is no runtime fallback.

- [ ] **Step 1: Delete the Supabase Kotlin files**

Run:
```bash
git rm shared/src/commonMain/kotlin/com/merkost/honq/data/remote/api/SupabaseConfig.kt
git rm shared/src/commonMain/kotlin/com/merkost/honq/data/remote/api/QuestionApi.kt
git rm shared/src/commonMain/kotlin/com/merkost/honq/data/remote/api/AppConfigApi.kt
```

- [ ] **Step 2: Remove Supabase libs from `libs.versions.toml`**

In `gradle/libs.versions.toml`:
- Delete the line: `supabase = "3.4.1"` (under `[versions]`)
- Delete the lines (under `[libraries]`):
  ```
  supabase-postgrest = { module = "io.github.jan-tennert.supabase:postgrest-kt", version.ref = "supabase" }
  supabase-functions = { module = "io.github.jan-tennert.supabase:functions-kt", version.ref = "supabase" }
  ```

- [ ] **Step 3: Remove Supabase deps from `shared/build.gradle.kts`**

In `shared/build.gradle.kts`, delete from the `commonMain.dependencies` block:
```kotlin
            implementation(libs.supabase.postgrest)
            implementation(libs.supabase.functions)
```

In the `buildkonfig { defaultConfigs { ... } }` block, delete:
```kotlin
        buildConfigField(STRING, "SUPABASE_URL", getLocalProperty("supabase.url", ""))
        buildConfigField(STRING, "SUPABASE_KEY", getLocalProperty("supabase.key", ""))
```

- [ ] **Step 4: Build everything**

Run:
```bash
./gradlew :shared:assemble :androidApp:assembleDebug -q
```
Expected: BUILD SUCCESSFUL on Android. If anything still references `SupabaseClient`, `Postgrest`, `BuildKonfig.SUPABASE_*`, fix it.

```bash
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64 -q
```
Expected: BUILD SUCCESSFUL for iOS framework.

- [ ] **Step 5: Re-run the full smoke test on both platforms**

Repeat Task 12 Steps 1–4 and Task 13 Steps 1–3 to confirm nothing regressed.

- [ ] **Step 6: Clean `local.properties` (manual)**

In your local-only `local.properties`, delete:
```
supabase.url=...
supabase.key=...
supabase.service.role.key=...
```
(Keep `firebase.project.id` and `firebase.hosting.base.url`.)

- [ ] **Step 7: Commit**

```bash
git add gradle/libs.versions.toml shared/build.gradle.kts
git commit -m "chore: drop supabase SDK and credentials"
```

---

## Task 15: Replace `docs/SUPABASE_SCHEMA.md` with `docs/FIRESTORE_SCHEMA.md`



**Files:**
- Create: `docs/FIRESTORE_SCHEMA.md`
- Delete: `docs/SUPABASE_SCHEMA.md`

- [ ] **Step 1: Write `docs/FIRESTORE_SCHEMA.md`**

Create the file with this exact content:

````markdown
# Firestore Content Schema

Honq's content backend lives in Firestore. Documents mirror the shape of the
former Supabase tables; field names are snake_case so existing DTOs (which use
`@SerialName`) decode unchanged.

## Collections

| Collection | Doc ID | Notes |
|---|---|---|
| `states` | state ID (e.g. `nsw`) | Public read. |
| `license_types` | license ID (e.g. `car`) | Public read of `is_active = true`. |
| `assessment_types` | assessment ID (e.g. `knowledge_test`) | Public read of `is_active = true`. |
| `categories` | category ID (e.g. `road_rules`) | Public read. |
| `question_sets` | set ID (e.g. `nsw_car`) | Public read of `is_active = true`. |
| `question_set_categories` | `<question_set_id>__<category_id>` (composite) | Public read. |
| `questions` | question ID (e.g. `nsw-001`) | Public read of `is_active = true`. |
| `state_resources` | resource ID | Public read of `is_active = true`. |
| `app_config` | config key (e.g. `data_version`) | Public read; one `value` field. |

All collections are read-only from clients. Writes happen via the
`scripts/export-supabase-to-firestore.py` script (run from a service account)
or manual edits in the Firebase Console.

## Image Hosting

Question images live in **Firebase Hosting** (the Spark-plan-friendly choice;
Firebase Storage now requires Blaze). Files are deployed from the repo's
`public/questions/` directory and served at the Hosting site root with a
year-long immutable cache (see `firebase.json` `headers` block).

The URL is constructed by `HostedImageUrlBuilder`:

```
https://honq-ac8e4.web.app/questions/<filename>
```

The base URL is read from `BuildKonfig.FIREBASE_HOSTING_BASE_URL` (defaults to
`https://honq-ac8e4.web.app`; override in `local.properties` if a custom domain
is set up).

To refresh images: re-run `scripts/export-supabase-to-firestore.py` (which
re-stages PNGs in `public/questions/`), then `firebase deploy --only hosting`.

## Security Rules

See `firebase/firestore.rules` and `firebase/storage.rules`. Rules enforce
public read of active rows and reject all client writes.

## Indexes

Current query shape uses only equality + the `updated_at > since` range filter
on the `questions` collection. Firestore creates a single-field index for
`updated_at` automatically, and equality-only multi-field queries do not
require composite indexes. If we ever need `is_active = true AND updated_at > X`
on the same query, that requires a composite index — add it via
`firebase/firestore.indexes.json` and `firebase deploy --only firestore:indexes`.
````

- [ ] **Step 2: Delete the Supabase schema doc**

Run:
```bash
git rm docs/SUPABASE_SCHEMA.md
```

- [ ] **Step 3: Commit**

```bash
git add docs/FIRESTORE_SCHEMA.md
git commit -m "docs: replace SUPABASE_SCHEMA with FIRESTORE_SCHEMA"
```

---

## Task 16: Final verification and cleanup

**Files:** none

- [ ] **Step 1: Search for stale references to Supabase**

Run:
```bash
grep -rni "supabase" --include="*.kt" --include="*.kts" --include="*.toml" --include="*.swift" --include="*.md" . | grep -v "^./docs/superpowers/" | grep -v "^./.git/"
```
Expected: only references in `docs/superpowers/research/...` (history docs) and `scripts/export-supabase-to-firestore.py` (the export script — kept for re-runnability if Supabase is recreated for a one-off correction). No production-code references.

- [ ] **Step 2: Run all unit tests**

Run:
```bash
./gradlew :shared:testDebugUnitTest -q
```
Expected: BUILD SUCCESSFUL, all tests pass (including the new `FirebaseImageUrlBuilderTest`).

- [ ] **Step 3: Build release variants on both platforms**

Run:
```bash
./gradlew :androidApp:assembleRelease -q
./gradlew :shared:linkReleaseFrameworkIosArm64 -q
```
Expected: both BUILD SUCCESSFUL. Release build catches issues debug builds miss (R8/proguard, optimization).

- [ ] **Step 4: Pause the Supabase project (manual)**

In the Supabase Dashboard → Project Settings → Pause project. Do NOT delete yet — keep as a 30-day rollback safety net.

Add a calendar reminder for **30 days from today** to delete the paused project once telemetry confirms no client is hitting it. (Or `/schedule` an agent to do the deletion check — see the schedule skill.)

- [ ] **Step 5: No commit**

Migration complete. Open the PR.

---

## Self-review notes

**Spec coverage:** Each surface from the assessment (`docs/superpowers/research/2026-04-28-firebase-migration-assessment.md` § "Migration shape") is addressed:

| Migration surface from assessment | Tasks |
|---|---|
| Replace Postgrest reads | Tasks 9, 10 |
| Replace `app_config` reads | Tasks 8, 10 |
| Replace image URL builder (Hosting, not Storage) | Tasks 5, 6 |
| One-shot data export + image staging | Task 11 |
| Add Firestore SDK dep (Android) | Task 3 |
| Add Firestore SDK dep (iOS SPM) | Task 4 |
| Drop Supabase deps + BuildKonfig | Task 14 |
| Drop dead `Functions` install | Task 14 (deleted with `SupabaseConfig.kt`) |
| Update docs | Task 15 |
| Smoke test both platforms | Tasks 12, 13 |
| Pause/retire Supabase project | Task 16 Step 4 |

The assessment recommended Remote Config for `min_app_version`. **This plan keeps `app_config` as a Firestore collection** — the simplest direct port, preserving the existing `AppConfigApi` semantics. Migrating to Remote Config is a worthwhile follow-up but would touch app initialization timing (Remote Config has its own fetch-and-activate flow) and is out of scope here. Track as future work.

**Open assumption to revisit if Task 12/13 fail:** the gitlive `firebase-firestore` 2.4.0 query DSL syntax in Task 9 Step 1 is the most fragile piece. The fallback (`whereEqualTo` flat form) is documented inline.
