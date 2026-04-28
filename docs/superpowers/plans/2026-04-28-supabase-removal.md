# Supabase Removal — Embed Content Offline — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Remove the Supabase Kotlin SDK from the Honq KMP app and ship the entire content bank (903 questions across 4 sets, 8 reference tables, 392 question images) embedded as Compose Multiplatform resources. After this change, the app makes zero network calls for content.

**Architecture:** A one-shot Kotlin script dumps Supabase tables and images into `composeResources/files/content/v1/`. On startup, a new `BundledContentLoader` seeds Room from the JSON bundle once per `BUNDLED_DATA_VERSION` bump. Image URLs become Compose-resource URIs via a new `BundledImagePath` helper. `state_resources` (16 rows, only used in one screen) is held in-memory via `StateResourcesProvider` to avoid a Room schema migration. All `data/remote/` code, the `DataSyncManager`, and the Supabase SDK are deleted.

**Tech Stack:** Kotlin Multiplatform · Compose Multiplatform · Room (KMP) · kotlinx.serialization · Koin · Ktor (kept for RevenueCat/etc., but not for content). The export script uses `kotlin scripts/export-supabase-bundle.main.kts` (Kotlin scripting, no Gradle dependency).

**Spec:** `docs/superpowers/specs/2026-04-28-supabase-removal-design.md`

---

## File Structure

```
scripts/
  export-supabase-bundle.main.kts                    [CREATE]

shared/src/commonMain/composeResources/files/content/v1/
  states.json                                        [CREATE - generated]
  license_types.json                                 [CREATE - generated]
  assessment_types.json                              [CREATE - generated]
  categories.json                                    [CREATE - generated]
  question_sets.json                                 [CREATE - generated]
  question_set_categories.json                       [CREATE - generated]
  questions.json                                     [CREATE - generated]
  state_resources.json                               [CREATE - generated]
  questions/<state>/*.png                            [CREATE - generated, ~392 files]

shared/src/commonMain/kotlin/com/merkost/honq/data/local/seed/
  BundledContentLoader.kt                            [CREATE]
  BundledImagePath.kt                                [CREATE]
  StateResourcesProvider.kt                          [CREATE]
  dto/QuestionDto.kt                                 [MOVE from data/remote/dto/]
  dto/StateDto.kt                                    [MOVE]
  dto/LicenseTypeDto.kt                              [MOVE]
  dto/AssessmentTypeDto.kt                           [MOVE]
  dto/CategoryDto.kt                                 [MOVE]
  dto/QuestionSetDto.kt                              [MOVE]
  dto/QuestionSetCategoryDto.kt                      [MOVE]
  dto/StateResourceDto.kt                            [MOVE]
  mapper/SeedDtoMapper.kt                            [MOVE+RENAME from data/remote/mapper/QuestionDtoMapper.kt]

shared/src/androidUnitTest/kotlin/com/merkost/honq/data/local/seed/
  StateResourcesProviderTest.kt                      [CREATE]
  BundledImagePathTest.kt                            [CREATE]
  SeedDtoMapperTest.kt                               [CREATE]

shared/src/commonMain/kotlin/com/merkost/honq/data/remote/   [DELETE entire dir]
shared/src/commonMain/kotlin/com/merkost/honq/data/repository/DataSyncManager.kt   [DELETE]
shared/src/commonMain/kotlin/com/merkost/honq/data/di/FakeDataModule.kt   [DELETE]
shared/src/commonMain/kotlin/com/merkost/honq/domain/usecase/SyncQuestionsUseCase.kt   [DELETE]

shared/src/commonMain/kotlin/com/merkost/honq/data/repository/QuestionRepositoryImpl.kt   [MODIFY]
shared/src/commonMain/kotlin/com/merkost/honq/domain/repository/QuestionRepository.kt   [MODIFY]
shared/src/commonMain/kotlin/com/merkost/honq/domain/usecase/GetStateResourcesUseCase.kt   [MODIFY]
shared/src/commonMain/kotlin/com/merkost/honq/data/di/DataModule.kt   [MODIFY]
shared/src/commonMain/kotlin/com/merkost/honq/domain/di/DomainModule.kt   [MODIFY]
shared/src/commonMain/kotlin/com/merkost/honq/presentation/screens/home/HomeContainer.kt   [MODIFY]
shared/src/commonMain/kotlin/com/merkost/honq/presentation/screens/onboarding/OnboardingContainer.kt   [MODIFY]
shared/build.gradle.kts                              [MODIFY]
gradle/libs.versions.toml                            [MODIFY]
local.properties                                     [MODIFY]
README.md                                            [MODIFY]
docs/SUPABASE_SCHEMA.md                              [MOVE to docs/archive/]
```

---

## Task 1: Create the export script and generate the bundle

**Files:**
- Create: `scripts/export-supabase-bundle.py`
- Create (output): `shared/src/commonMain/composeResources/files/content/v1/*.json` (8 files)
- Create (output): `shared/src/commonMain/composeResources/files/content/v1/questions/<state>/*.png` (~392 files)

The script reads the existing `local.properties` for Supabase URL/key, dumps each of the 8 content tables to pretty-printed JSON, then downloads every `image_url` referenced in `questions.json` from the public Storage bucket. It is a build-time tool — never executed by the app.

Implemented in Python (stdlib only — no `pip install` required) so no new toolchain is added to the repo. macOS has Python 3 by default at `/usr/bin/python3`.

- [ ] **Step 1: Write the script**

Create `scripts/export-supabase-bundle.py`:

```python
#!/usr/bin/env python3
"""One-shot exporter that dumps Supabase content tables and question images
into shared/src/commonMain/composeResources/files/content/v1/ for offline
embedding. See docs/superpowers/specs/2026-04-28-supabase-removal-design.md.
"""

from __future__ import annotations

import json
import sys
import urllib.error
import urllib.parse
import urllib.request
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parent.parent
LOCAL_PROPS = REPO_ROOT / "local.properties"
OUT_DIR = REPO_ROOT / "shared/src/commonMain/composeResources/files/content/v1"

TABLES = [
    "states",
    "license_types",
    "assessment_types",
    "categories",
    "question_sets",
    "question_set_categories",
    "questions",
    "state_resources",
]


def read_local_properties() -> dict[str, str]:
    if not LOCAL_PROPS.exists():
        sys.exit(f"local.properties not found at {LOCAL_PROPS}")
    props: dict[str, str] = {}
    for line in LOCAL_PROPS.read_text().splitlines():
        line = line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, _, value = line.partition("=")
        props[key.strip()] = value.strip()
    return props


def http_get(url: str, headers: dict[str, str]) -> bytes:
    req = urllib.request.Request(url, headers=headers)
    try:
        with urllib.request.urlopen(req, timeout=60) as resp:
            return resp.read()
    except urllib.error.HTTPError as e:
        sys.exit(f"HTTP {e.code} for {url}: {e.read().decode(errors='replace')}")


def fetch_table(base_url: str, key: str, table: str) -> list[dict]:
    headers = {"apikey": key, "Authorization": f"Bearer {key}"}
    query = urllib.parse.urlencode({"select": "*", "order": "id"})
    body = http_get(f"{base_url}/rest/v1/{table}?{query}", headers)
    return json.loads(body.decode("utf-8"))


def main() -> None:
    props = read_local_properties()
    supabase_url = props.get("supabase.url", "").rstrip("/")
    supabase_key = props.get("supabase.key", "")
    if not supabase_url or not supabase_key:
        sys.exit("supabase.url or supabase.key missing from local.properties")

    OUT_DIR.mkdir(parents=True, exist_ok=True)

    table_data: dict[str, list[dict]] = {}
    for table in TABLES:
        print(f"Fetching {table}... ", end="", flush=True)
        rows = fetch_table(supabase_url, supabase_key, table)
        table_data[table] = rows
        out_file = OUT_DIR / f"{table}.json"
        out_file.write_text(json.dumps(rows, indent=2, ensure_ascii=False) + "\n")
        print(f"{len(rows)} rows")

    image_paths = sorted({
        row["image_url"]
        for row in table_data["questions"]
        if isinstance(row.get("image_url"), str) and row["image_url"].strip()
    })
    print(f"Downloading {len(image_paths)} images...")

    headers = {"apikey": supabase_key}
    total_bytes = 0
    for rel_path in image_paths:
        body = http_get(
            f"{supabase_url}/storage/v1/object/public/{rel_path}",
            headers,
        )
        out_file = OUT_DIR / rel_path
        out_file.parent.mkdir(parents=True, exist_ok=True)
        out_file.write_bytes(body)
        total_bytes += len(body)
    print(f"Wrote {len(image_paths)} images, {total_bytes // 1024} KB total")
    print(f"Done. Bundle written to {OUT_DIR}")


if __name__ == "__main__":
    main()
```

Make it executable:
```bash
chmod +x scripts/export-supabase-bundle.py
```

- [ ] **Step 2: Run the script and verify output**

Run from the repo root:
```bash
cd /Users/merkost/StudioProjects/Honq
python3 scripts/export-supabase-bundle.py
```

Expected output:
```
Fetching states... 8 rows
Fetching license_types... 2 rows
Fetching assessment_types... 2 rows
Fetching categories... 14 rows
Fetching question_sets... 4 rows
Fetching question_set_categories... 51 rows
Fetching questions... 903 rows
Fetching state_resources... 16 rows
Downloading 392 images...
Wrote 392 images, ~XXXX KB total
Done. Bundle written to .../composeResources/files/content/v1
```

Verify:
```bash
ls shared/src/commonMain/composeResources/files/content/v1/*.json | wc -l
# Expected: 8

find shared/src/commonMain/composeResources/files/content/v1/questions -name "*.png" | wc -l
# Expected: 392
```

- [ ] **Step 3: Commit the script and bundle**

```bash
git add scripts/export-supabase-bundle.py \
        shared/src/commonMain/composeResources/files/content/v1
git commit -m "feat: add Supabase bundle exporter and embed content offline

Generated 8 JSON tables (903 questions across 4 sets) and 392 question
images. Script is build-time tooling, not part of the app build."
```

---

## Task 2: Move DTOs and mapper into the seed package

**Files:**
- Move: `shared/src/commonMain/kotlin/com/merkost/honq/data/remote/dto/*.kt` → `shared/src/commonMain/kotlin/com/merkost/honq/data/local/seed/dto/*.kt`
- Move+rename: `shared/src/commonMain/kotlin/com/merkost/honq/data/remote/mapper/QuestionDtoMapper.kt` → `shared/src/commonMain/kotlin/com/merkost/honq/data/local/seed/mapper/SeedDtoMapper.kt`
- Modify (just imports): every file that imports from `data.remote.dto` or `data.remote.mapper`

The app must still compile after this task — `QuestionApi.kt`, `AppConfigApi.kt`, `DataSyncManager.kt`, `QuestionRepositoryImpl.kt`, etc. all import these types. We're only changing the package, not deleting anything yet.

The mapper also drops its dependency on `SupabaseConfig.getStorageUrl` — `QuestionEntity.imageUrl` now stores the raw relative path (e.g. `questions/nt/CSB002.png`), and resolution to a renderable URI happens at the UI layer (Task 4 introduces the helper).

- [ ] **Step 1: Move DTO files**

```bash
cd /Users/merkost/StudioProjects/Honq
mkdir -p shared/src/commonMain/kotlin/com/merkost/honq/data/local/seed/dto
git mv shared/src/commonMain/kotlin/com/merkost/honq/data/remote/dto/*.kt \
       shared/src/commonMain/kotlin/com/merkost/honq/data/local/seed/dto/
```

- [ ] **Step 2: Update package declaration in each moved DTO**

For each file in `shared/src/commonMain/kotlin/com/merkost/honq/data/local/seed/dto/*.kt`, change the first line from `package com.merkost.honq.data.remote.dto` to `package com.merkost.honq.data.local.seed.dto`.

(Run this once across all files in the dir):
```bash
sed -i '' 's|^package com\.merkost\.honq\.data\.remote\.dto$|package com.merkost.honq.data.local.seed.dto|' \
    shared/src/commonMain/kotlin/com/merkost/honq/data/local/seed/dto/*.kt
```

Verify:
```bash
grep -L 'package com.merkost.honq.data.local.seed.dto' \
    shared/src/commonMain/kotlin/com/merkost/honq/data/local/seed/dto/*.kt
# Expected: empty (all files updated)
```

- [ ] **Step 3: Move and rename the mapper**

```bash
mkdir -p shared/src/commonMain/kotlin/com/merkost/honq/data/local/seed/mapper
git mv shared/src/commonMain/kotlin/com/merkost/honq/data/remote/mapper/QuestionDtoMapper.kt \
       shared/src/commonMain/kotlin/com/merkost/honq/data/local/seed/mapper/SeedDtoMapper.kt
```

- [ ] **Step 4: Rewrite the mapper without `SupabaseConfig`**

Replace the entire content of `shared/src/commonMain/kotlin/com/merkost/honq/data/local/seed/mapper/SeedDtoMapper.kt` with:

```kotlin
package com.merkost.honq.data.local.seed.mapper

import com.merkost.honq.data.local.entity.AssessmentTypeEntity
import com.merkost.honq.data.local.entity.CategoryEntity
import com.merkost.honq.data.local.entity.LicenseTypeEntity
import com.merkost.honq.data.local.entity.QuestionEntity
import com.merkost.honq.data.local.entity.QuestionSetCategoryEntity
import com.merkost.honq.data.local.entity.QuestionSetEntity
import com.merkost.honq.data.local.entity.StateEntity
import com.merkost.honq.data.local.seed.dto.AssessmentTypeDto
import com.merkost.honq.data.local.seed.dto.CategoryDto
import com.merkost.honq.data.local.seed.dto.LicenseTypeDto
import com.merkost.honq.data.local.seed.dto.QuestionDto
import com.merkost.honq.data.local.seed.dto.QuestionSetCategoryDto
import com.merkost.honq.data.local.seed.dto.QuestionSetDto
import com.merkost.honq.data.local.seed.dto.StateDto
import com.merkost.honq.data.local.seed.dto.StateResourceDto
import com.merkost.honq.domain.model.Difficulty
import com.merkost.honq.domain.model.ResourceType
import com.merkost.honq.domain.model.StateResource
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun QuestionDto.toEntity(json: Json): QuestionEntity = QuestionEntity(
    id = id,
    code = code,
    text = text,
    imageUrl = imageUrl,                          // raw relative path; resolved at UI layer
    options = json.encodeToString(options),
    correctIndex = correctIndex,
    explanation = explanation.orEmpty(),
    categoryId = category.lowercase(),
    questionSetId = questionSetId,
    updatedAt = updatedAt,
    stateId = stateId.lowercase(),
    difficulty = difficulty ?: Difficulty.MEDIUM.value,
    isActive = isActive,
    version = version,
    source = source,
    createdAt = createdAt
)

fun StateDto.toEntity(): StateEntity = StateEntity(
    id = id,
    name = name,
    shortName = shortName,
    externalPracticeUrl = externalPracticeUrl,
    handbookUrl = handbookUrl,
    isActive = isActive,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun CategoryDto.toEntity(): CategoryEntity = CategoryEntity(
    id = id,
    name = name,
    description = description.orEmpty(),
    iconName = iconName.orEmpty(),
    displayOrder = displayOrder,
    isActive = isActive,
    createdAt = createdAt.orEmpty(),
    updatedAt = updatedAt.orEmpty()
)

fun LicenseTypeDto.toEntity(): LicenseTypeEntity = LicenseTypeEntity(
    id = id,
    name = name,
    shortName = shortName,
    isActive = isActive,
    displayOrder = displayOrder,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun AssessmentTypeDto.toEntity(): AssessmentTypeEntity = AssessmentTypeEntity(
    id = id,
    name = name,
    shortName = shortName,
    isActive = isActive,
    displayOrder = displayOrder,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun QuestionSetDto.toEntity(): QuestionSetEntity = QuestionSetEntity(
    id = id,
    stateId = stateId.lowercase(),
    licenseTypeId = licenseTypeId.lowercase(),
    assessmentTypeId = assessmentTypeId.lowercase(),
    mockTestQuestionCount = mockTestQuestionCount,
    mockTestTimeLimitMinutes = mockTestTimeLimitMinutes,
    mockTestPassPercentage = mockTestPassPercentage,
    isActive = isActive,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun QuestionSetCategoryDto.toEntity(): QuestionSetCategoryEntity = QuestionSetCategoryEntity(
    questionSetId = questionSetId,
    categoryId = categoryId,
    displayOrder = displayOrder,
    isActive = isActive
)

fun StateResourceDto.toDomain(): StateResource = StateResource(
    id = id,
    stateId = stateId.lowercase(),
    title = title,
    url = url,
    resourceType = when (resourceType.lowercase()) {
        "practice_test" -> ResourceType.PRACTICE_TEST
        "pdf" -> ResourceType.PDF
        "handbook" -> ResourceType.HANDBOOK
        else -> ResourceType.OTHER
    },
    licenseType = licenseType?.lowercase(),
    displayOrder = displayOrder
)
```

Note: the `QuestionDto.toDomain()` and `CategoryDto.toDomain()` mappers from the old file are dropped — they were only used by the now-doomed `QuestionApi` path. Domain conversion now goes Entity → Domain via `data/local/mapper/QuestionEntityMapper.kt`, which already exists.

- [ ] **Step 5: Update import sites**

```bash
cd /Users/merkost/StudioProjects/Honq
grep -rl 'com\.merkost\.honq\.data\.remote\.dto' \
    shared/src/commonMain/kotlin androidApp/src 2>/dev/null \
  | xargs sed -i '' 's|com\.merkost\.honq\.data\.remote\.dto|com.merkost.honq.data.local.seed.dto|g'
grep -rl 'com\.merkost\.honq\.data\.remote\.mapper' \
    shared/src/commonMain/kotlin androidApp/src 2>/dev/null \
  | xargs sed -i '' 's|com\.merkost\.honq\.data\.remote\.mapper|com.merkost.honq.data.local.seed.mapper|g'
```

Then in `QuestionRepositoryImpl.kt` and any other site that referenced `data.remote.dto.QuestionDto` (etc.), confirm imports look clean.

- [ ] **Step 6: Build and verify**

```bash
./gradlew :shared:compileKotlinMetadata :androidApp:compileDebugKotlinAndroid 2>&1 | tail -40
```

Expected: BUILD SUCCESSFUL. If there are compile errors about a missing `QuestionDto.toDomain` or `CategoryDto.toDomain`, those call sites are inside the soon-to-be-deleted `data/remote/api/QuestionApi.kt` — leave them broken for now if Gradle tolerates it, or delete that one specific function call temporarily; otherwise just keep the missing-mapper symbol commented out. (Re-check: the dropped `toDomain` mappers from the old file are NOT referenced from outside `data/remote/`, so this should compile cleanly.)

- [ ] **Step 7: Commit**

```bash
git add -A
git commit -m "refactor: move Supabase DTOs and mapper into data/local/seed

Renames the package to reflect their new role (seeding Room from a
bundled JSON resource rather than from a remote response). Drops
SupabaseConfig.getStorageUrl from the mapper — questions.imageUrl now
stores the raw relative path; UI resolves to a Compose-resource URI."
```

---

## Task 3: Add `BundledImagePath` helper

**Files:**
- Create: `shared/src/commonMain/kotlin/com/merkost/honq/data/local/seed/BundledImagePath.kt`
- Test: `shared/src/androidUnitTest/kotlin/com/merkost/honq/data/local/seed/BundledImagePathTest.kt`

`BundledImagePath.resolve(relativePath)` produces the URI string passed to Coil for a question image. Today's call sites use `SupabaseConfig.getStorageUrl(relativePath)` returning an HTTPS URL; the replacement returns a Compose-resource URI of the form `file:///android_asset/composeResources/...` on Android and `compose-resource://...` on iOS — but because `Res.getUri` handles platform differences for us, the helper is one-liner-thin.

- [ ] **Step 1: Write the failing test**

Create `shared/src/androidUnitTest/kotlin/com/merkost/honq/data/local/seed/BundledImagePathTest.kt`:

```kotlin
package com.merkost.honq.data.local.seed

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class BundledImagePathTest {
    @Test
    fun `resolve null returns null`() {
        assertNull(BundledImagePath.resolve(null))
    }

    @Test
    fun `resolve blank returns null`() {
        assertNull(BundledImagePath.resolve(""))
        assertNull(BundledImagePath.resolve("   "))
    }

    @Test
    fun `resolve returns canonical resource path`() {
        val path = BundledImagePath.resolve("questions/nt/CSB002.png")
        assertEquals("files/content/v1/questions/nt/CSB002.png", path)
    }

    @Test
    fun `resolve trims leading slash`() {
        val path = BundledImagePath.resolve("/questions/nt/CSB002.png")
        assertEquals("files/content/v1/questions/nt/CSB002.png", path)
    }
}
```

Note: This test exercises the path-construction logic only (a pure-string transformation). Coil's actual asset loading is platform-integration-tested manually in Task 14.

- [ ] **Step 2: Run test to verify it fails**

```bash
./gradlew :shared:testDebugUnitTest --tests '*BundledImagePathTest*'
```

Expected: FAIL with "Unresolved reference: BundledImagePath".

- [ ] **Step 3: Implement `BundledImagePath`**

Create `shared/src/commonMain/kotlin/com/merkost/honq/data/local/seed/BundledImagePath.kt`:

```kotlin
package com.merkost.honq.data.local.seed

object BundledImagePath {
    private const val ROOT = "files/content/v1/"

    /**
     * Returns a Compose-resource path suitable for Coil + Compose Multiplatform's resource loader.
     * Returns `null` for null/blank inputs so callers can pass directly into image components.
     */
    fun resolve(relativePath: String?): String? {
        if (relativePath.isNullOrBlank()) return null
        val trimmed = relativePath.trim().trimStart('/')
        return "$ROOT$trimmed"
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

```bash
./gradlew :shared:testDebugUnitTest --tests '*BundledImagePathTest*'
```

Expected: 4 tests PASS.

- [ ] **Step 5: Commit**

```bash
git add shared/src/commonMain/kotlin/com/merkost/honq/data/local/seed/BundledImagePath.kt \
        shared/src/androidUnitTest/kotlin/com/merkost/honq/data/local/seed/BundledImagePathTest.kt
git commit -m "feat: add BundledImagePath helper for resolving question image URIs"
```

---

## Task 4: Add `StateResourcesProvider`

**Files:**
- Create: `shared/src/commonMain/kotlin/com/merkost/honq/data/local/seed/StateResourcesProvider.kt`
- Test: `shared/src/androidUnitTest/kotlin/com/merkost/honq/data/local/seed/StateResourcesProviderTest.kt`

A small singleton that reads `state_resources.json` from Compose resources once, parses the 16 rows into domain `StateResource` objects, and exposes a `getByState(stateId)` lookup. `GetStateResourcesUseCase` (Task 8) reads from this instead of `QuestionApi`.

This avoids adding a Room entity for a 16-row table that's only consumed by one screen. If the data ever needs to grow to thousands of rows or be queried in complex ways, it can be promoted to Room with a proper migration; not yet.

- [ ] **Step 1: Write the failing test**

Create `shared/src/androidUnitTest/kotlin/com/merkost/honq/data/local/seed/StateResourcesProviderTest.kt`:

```kotlin
package com.merkost.honq.data.local.seed

import com.merkost.honq.data.local.seed.dto.StateResourceDto
import com.merkost.honq.domain.model.ResourceType
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StateResourcesProviderTest {
    private val json = Json { ignoreUnknownKeys = true }

    private val sampleJson = """
        [
          {
            "id": 1,
            "state_id": "NSW",
            "title": "Road User Handbook",
            "url": "https://example.com/handbook.pdf",
            "resource_type": "pdf",
            "license_type": "car",
            "display_order": 1,
            "is_active": true
          },
          {
            "id": 2,
            "state_id": "nt",
            "title": "Practice Test",
            "url": "https://example.com/practice",
            "resource_type": "practice_test",
            "license_type": null,
            "display_order": 2,
            "is_active": true
          }
        ]
    """.trimIndent()

    @Test
    fun `getByState returns matching rows lowercased`() {
        val provider = StateResourcesProvider(json) { sampleJson.encodeToByteArray() }
        provider.ensureLoaded()

        val nsw = provider.getByState("nsw")
        assertEquals(1, nsw.size)
        assertEquals(ResourceType.PDF, nsw.first().resourceType)
        assertEquals("car", nsw.first().licenseType)

        val nt = provider.getByState("NT")
        assertEquals(1, nt.size)
        assertEquals(ResourceType.PRACTICE_TEST, nt.first().resourceType)
        assertEquals(null, nt.first().licenseType)
    }

    @Test
    fun `getByState unknown returns empty`() {
        val provider = StateResourcesProvider(json) { sampleJson.encodeToByteArray() }
        provider.ensureLoaded()
        assertTrue(provider.getByState("vic").isEmpty())
    }

    @Test
    fun `ensureLoaded is idempotent`() {
        var reads = 0
        val provider = StateResourcesProvider(json) {
            reads++
            sampleJson.encodeToByteArray()
        }
        provider.ensureLoaded()
        provider.ensureLoaded()
        provider.ensureLoaded()
        assertEquals(1, reads)
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
./gradlew :shared:testDebugUnitTest --tests '*StateResourcesProviderTest*'
```

Expected: FAIL with "Unresolved reference: StateResourcesProvider".

- [ ] **Step 3: Implement `StateResourcesProvider`**

Create `shared/src/commonMain/kotlin/com/merkost/honq/data/local/seed/StateResourcesProvider.kt`:

```kotlin
package com.merkost.honq.data.local.seed

import com.merkost.honq.data.local.seed.dto.StateResourceDto
import com.merkost.honq.data.local.seed.mapper.toDomain
import com.merkost.honq.domain.model.StateResource
import kotlinx.serialization.json.Json
import org.kimplify.cedar.logging.Cedar

/**
 * In-memory holder for state_resources data, loaded once from a bundled JSON resource.
 *
 * State resources are small (~16 rows), static, and only consumed by one screen, so we
 * deliberately skip Room (which would require a schema migration). Promote to Room only
 * if the data set or query pattern grows.
 */
class StateResourcesProvider(
    private val json: Json,
    private val readBundle: suspend () -> ByteArray,
) {
    private var cached: List<StateResource>? = null

    suspend fun ensureLoaded() {
        if (cached != null) return
        val bytes = readBundle()
        val dtos = json.decodeFromString<List<StateResourceDto>>(bytes.decodeToString())
        cached = dtos.map { it.toDomain() }
        Cedar.tag("StateResources").d("ensureLoaded: cached ${cached!!.size} rows")
    }

    fun getByState(stateId: String): List<StateResource> {
        val all = cached
            ?: error("StateResourcesProvider.ensureLoaded() must be called before getByState")
        val key = stateId.lowercase()
        return all.filter { it.stateId == key }.sortedBy { it.displayOrder }
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

```bash
./gradlew :shared:testDebugUnitTest --tests '*StateResourcesProviderTest*'
```

Expected: 3 tests PASS.

- [ ] **Step 5: Commit**

```bash
git add shared/src/commonMain/kotlin/com/merkost/honq/data/local/seed/StateResourcesProvider.kt \
        shared/src/androidUnitTest/kotlin/com/merkost/honq/data/local/seed/StateResourcesProviderTest.kt
git commit -m "feat: add StateResourcesProvider for in-memory state_resources lookup"
```

---

## Task 5: Add `BundledContentLoader`

**Files:**
- Create: `shared/src/commonMain/kotlin/com/merkost/honq/data/local/seed/BundledContentLoader.kt`
- Test: `shared/src/androidUnitTest/kotlin/com/merkost/honq/data/local/seed/SeedDtoMapperTest.kt`

`BundledContentLoader.ensureSeeded()` is the central piece. It checks `SyncPreferences.localDataVersion` against a `BUNDLED_DATA_VERSION` constant; on mismatch, it parses each of the 7 reference/content JSON files, maps to entities, and seeds Room via existing `QuestionLocalDataSource` upsert paths. `state_resources.json` is loaded by `StateResourcesProvider` separately — `ensureSeeded` triggers that too.

`BUNDLED_DATA_VERSION = 2` is one greater than the current production `app_config.data_version = 1`, so every existing Supabase-era install re-seeds exactly once on first launch of the new app. Question IDs are preserved verbatim from the dump, so `favorites`/`answer_history`/`mock_test_*` rows continue to reference valid IDs.

The test in this task is for the seed mapper (string-pure, no Room). An end-to-end Room-backed integration test would require Robolectric + a fixture JSON; we rely on manual smoke (Task 14) instead, since the mapper's correctness is the only seed-time risk worth covering with automated tests.

- [ ] **Step 1: Write the failing mapper test**

Create `shared/src/androidUnitTest/kotlin/com/merkost/honq/data/local/seed/SeedDtoMapperTest.kt`:

```kotlin
package com.merkost.honq.data.local.seed

import com.merkost.honq.data.local.seed.dto.QuestionDto
import com.merkost.honq.data.local.seed.dto.StateDto
import com.merkost.honq.data.local.seed.mapper.toEntity
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeedDtoMapperTest {
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }

    @Test
    fun `QuestionDto toEntity preserves raw imageUrl path`() {
        val dto = QuestionDto(
            id = "nsw-001",
            code = "RR001",
            text = "When approaching a roundabout, you must:",
            imageUrl = "questions/nsw/RR001.png",
            options = listOf("A", "B", "C", "D"),
            correctIndex = 1,
            explanation = "Give way.",
            category = "ROAD_RULES",
            questionSetId = "nsw_car",
            updatedAt = "2026-01-01T00:00:00Z",
            stateId = "NSW",
            difficulty = 2,
            isActive = true,
            version = 1,
            source = "manual",
            createdAt = "2026-01-01T00:00:00Z",
        )
        val entity = dto.toEntity(json)
        assertEquals("questions/nsw/RR001.png", entity.imageUrl)
        assertEquals("nsw", entity.stateId)              // lowercased
        assertEquals("road_rules", entity.categoryId)    // lowercased
        assertEquals("""["A","B","C","D"]""", entity.options)
    }

    @Test
    fun `QuestionDto toEntity tolerates null imageUrl`() {
        val dto = QuestionDto(
            id = "nsw-002",
            code = "RR002",
            text = "Q",
            imageUrl = null,
            options = listOf("X", "Y"),
            correctIndex = 0,
            explanation = null,
            category = "SAFETY",
            questionSetId = "nsw_car",
        )
        val entity = dto.toEntity(json)
        assertEquals(null, entity.imageUrl)
        assertEquals("", entity.explanation)             // explanation defaults to empty
    }

    @Test
    fun `StateDto toEntity preserves all fields`() {
        val dto = StateDto(
            id = "nsw",
            name = "New South Wales",
            shortName = "NSW",
            externalPracticeUrl = null,
            handbookUrl = null,
            isActive = true,
            createdAt = "2026-01-01T00:00:00Z",
            updatedAt = "2026-01-01T00:00:00Z",
        )
        val entity = dto.toEntity()
        assertEquals("nsw", entity.id)
        assertTrue(entity.isActive)
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
./gradlew :shared:testDebugUnitTest --tests '*SeedDtoMapperTest*'
```

Expected: PASS — the mapper already exists from Task 2. (If it FAILs, the mapper rewrite in Task 2 has a bug; fix it.)

- [ ] **Step 3: Implement `BundledContentLoader`**

Create `shared/src/commonMain/kotlin/com/merkost/honq/data/local/seed/BundledContentLoader.kt`:

```kotlin
package com.merkost.honq.data.local.seed

import com.merkost.honq.core.util.AppDispatchers
import com.merkost.honq.data.local.SyncPreferences
import com.merkost.honq.data.local.datasource.QuestionLocalDataSource
import com.merkost.honq.data.local.seed.dto.AssessmentTypeDto
import com.merkost.honq.data.local.seed.dto.CategoryDto
import com.merkost.honq.data.local.seed.dto.LicenseTypeDto
import com.merkost.honq.data.local.seed.dto.QuestionDto
import com.merkost.honq.data.local.seed.dto.QuestionSetCategoryDto
import com.merkost.honq.data.local.seed.dto.QuestionSetDto
import com.merkost.honq.data.local.seed.dto.StateDto
import com.merkost.honq.data.local.seed.mapper.toEntity
import honq.shared.generated.resources.Res
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.kimplify.cedar.logging.Cedar

const val BUNDLED_DATA_VERSION = 2

@OptIn(ExperimentalResourceApi::class)
class BundledContentLoader(
    private val localDataSource: QuestionLocalDataSource,
    private val stateResourcesProvider: StateResourcesProvider,
    private val syncPreferences: SyncPreferences,
    private val json: Json,
    private val dispatchers: AppDispatchers,
) {
    suspend fun ensureSeeded() = withContext(dispatchers.io) {
        // state_resources is independent of the Room seed and cheap to load —
        // do it unconditionally so any caller of getByState() works.
        stateResourcesProvider.ensureLoaded()

        val localVersion = syncPreferences.getLocalDataVersion()
        if (localVersion >= BUNDLED_DATA_VERSION) {
            Cedar.tag("Seed").d("ensureSeeded: localVersion=$localVersion >= bundle=$BUNDLED_DATA_VERSION, skipping")
            return@withContext
        }
        Cedar.tag("Seed").d("ensureSeeded: seeding from bundle (local=$localVersion, bundle=$BUNDLED_DATA_VERSION)")
        seedFromBundle()
        syncPreferences.setLocalDataVersion(BUNDLED_DATA_VERSION)
        syncPreferences.setInitialSyncCompleted(true)
        Cedar.tag("Seed").d("ensureSeeded: complete")
    }

    private suspend fun seedFromBundle() {
        val states = readJson<List<StateDto>>("states.json")
        val licenseTypes = readJson<List<LicenseTypeDto>>("license_types.json")
        val assessmentTypes = readJson<List<AssessmentTypeDto>>("assessment_types.json")
        val categories = readJson<List<CategoryDto>>("categories.json")
        val questionSets = readJson<List<QuestionSetDto>>("question_sets.json")
        val questionSetCategories = readJson<List<QuestionSetCategoryDto>>("question_set_categories.json")
        val questions = readJson<List<QuestionDto>>("questions.json")

        Cedar.tag("Seed").d(
            "seedFromBundle: states=${states.size}, licenseTypes=${licenseTypes.size}, " +
                "assessmentTypes=${assessmentTypes.size}, categories=${categories.size}, " +
                "questionSets=${questionSets.size}, questionSetCategories=${questionSetCategories.size}, " +
                "questions=${questions.size}"
        )

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

    private suspend inline fun <reified T> readJson(name: String): T {
        val bytes = Res.readBytes("files/content/v1/$name")
        return json.decodeFromString(bytes.decodeToString())
    }
}
```

Note: `honq.shared.generated.resources.Res` is the import path Compose Multiplatform's Resources plugin generates. If your generated package differs (check `composeResources` block in `shared/build.gradle.kts`), substitute accordingly.

- [ ] **Step 4: Build and verify**

```bash
./gradlew :shared:compileKotlinMetadata 2>&1 | tail -20
```

Expected: BUILD SUCCESSFUL. If `Res.readBytes` is unresolved, run `./gradlew :shared:generateComposeResClass` first to refresh the generated `Res` class.

- [ ] **Step 5: Commit**

```bash
git add shared/src/commonMain/kotlin/com/merkost/honq/data/local/seed/BundledContentLoader.kt \
        shared/src/androidUnitTest/kotlin/com/merkost/honq/data/local/seed/SeedDtoMapperTest.kt
git commit -m "feat: add BundledContentLoader for one-shot Room seeding from resources

Reads JSON files from composeResources/files/content/v1/, maps via the
existing seed DTOs, and upserts into Room when the local data version
trails BUNDLED_DATA_VERSION. Sits alongside the existing Supabase sync
path; wired up in the next task."
```

---

## Task 6: Wire seed components into Koin DI

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/merkost/honq/data/di/DataModule.kt`

Add three Koin singletons: `BundledContentLoader`, `BundledImagePath`, `StateResourcesProvider`. Keep all existing Supabase-related singletons in place — they're not deleted until Task 11. This task makes the new components injectable so subsequent tasks can wire them into call sites.

- [ ] **Step 1: Add Koin singletons**

In `shared/src/commonMain/kotlin/com/merkost/honq/data/di/DataModule.kt`, after the existing `SupabaseClient` registration (around line 47), add:

```kotlin
// Bundled-content seeding (replacing Supabase sync over the next several commits)
single {
    com.merkost.honq.data.local.seed.StateResourcesProvider(
        json = get(),
        readBundle = {
            @OptIn(org.jetbrains.compose.resources.ExperimentalResourceApi::class)
            honq.shared.generated.resources.Res.readBytes("files/content/v1/state_resources.json")
        },
    )
}
single {
    com.merkost.honq.data.local.seed.BundledContentLoader(
        localDataSource = get(),
        stateResourcesProvider = get(),
        syncPreferences = get(),
        json = get(),
        dispatchers = get(),
    )
}
```

(Adjust the resource package path if it differs from `honq.shared.generated.resources` — match what Task 5 used.)

Confirm `Json` and `AppDispatchers` are already provided in this module or another loaded module. Grep:

```bash
grep -rn 'single<Json>\|single { Json\|kotlinx\.serialization\.json\.Json' \
    shared/src/commonMain/kotlin/com/merkost/honq/**/di/*.kt
grep -rn 'AppDispatchers' shared/src/commonMain/kotlin/com/merkost/honq/**/di/*.kt
```

If `Json` is not provided, add: `single { Json { ignoreUnknownKeys = true } }` to `DataModule`.

- [ ] **Step 2: Build**

```bash
./gradlew :shared:compileKotlinMetadata :androidApp:compileDebugKotlinAndroid 2>&1 | tail -20
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add shared/src/commonMain/kotlin/com/merkost/honq/data/di/DataModule.kt
git commit -m "chore: register BundledContentLoader and StateResourcesProvider in Koin"
```

---

## Task 7: Switch startup flow to seed before any sync

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/merkost/honq/presentation/screens/onboarding/OnboardingContainer.kt`
- Modify: `shared/src/commonMain/kotlin/com/merkost/honq/presentation/screens/home/HomeContainer.kt`

Both containers currently call `dataSyncManager.needsInitialSync()` and `repository.fullSync(...)` to populate Room from Supabase. Replace those calls with a single `bundledContentLoader.ensureSeeded()` invocation. The remaining `dataSyncManager` references (`checkIfSyncNeeded`, `fetchRemoteVersion`, `markSyncCompleted`) are dropped from the call sites in this task; the manager itself is deleted in Task 11.

After this task the bundle is the only seeding path. Smoke test (manual) before proceeding.

- [ ] **Step 1: Modify `OnboardingContainer.loadData`**

Open `shared/src/commonMain/kotlin/com/merkost/honq/presentation/screens/onboarding/OnboardingContainer.kt`. Replace the block currently around line 65–75:

```kotlin
private suspend fun PipelineContext<OnboardingState, OnboardingIntent, OnboardingAction>.loadData() {
    Cedar.tag("Onboarding").d("loadData: starting...")
    updateState { copy(isLoading = true, error = null) }

    if (dataSyncManager.needsInitialSync()) {
        Cedar.tag("Onboarding").d("loadData: first launch, running full sync")
        val remoteVersion = dataSyncManager.fetchRemoteVersion().getOrDefault(0)
        repository.fullSync(null)
        dataSyncManager.markSyncCompleted(remoteVersion)
    }
    // ... existing getStates() / getLicenseTypes() calls
}
```

With:

```kotlin
private suspend fun PipelineContext<OnboardingState, OnboardingIntent, OnboardingAction>.loadData() {
    Cedar.tag("Onboarding").d("loadData: starting...")
    updateState { copy(isLoading = true, error = null) }

    bundledContentLoader.ensureSeeded()

    // ... existing getStates() / getLicenseTypes() calls
}
```

In the constructor of `OnboardingContainer`, replace the `dataSyncManager: DataSyncManager` and `repository: QuestionRepository` parameters used solely for sync with a `bundledContentLoader: BundledContentLoader` parameter. (Keep `repository` if it's used elsewhere in the file.) Remove the `import com.merkost.honq.data.repository.DataSyncManager` line. Add `import com.merkost.honq.data.local.seed.BundledContentLoader`.

- [ ] **Step 2: Modify `HomeContainer.loadInitialData`**

Open `shared/src/commonMain/kotlin/com/merkost/honq/presentation/screens/home/HomeContainer.kt`. Replace the block currently around line 80–97:

```kotlin
val dbEmpty = !dataSyncManager.needsInitialSync() && repository.isDatabaseEmpty()
if (dbEmpty) {
    Cedar.tag("Home").d("loadInitialData: DB empty but sync flag set, resetting sync state")
    dataSyncManager.resetAllSyncData()
}
if (dataSyncManager.needsInitialSync()) {
    Cedar.tag("Home").d("loadInitialData: first launch, running full sync")
    val remoteVersion = dataSyncManager.fetchRemoteVersion().getOrDefault(0)
    repository.fullSync(questionSetId = null, remoteVersion = remoteVersion)
} else {
    val check = dataSyncManager.checkIfSyncNeeded()
    if (check.needsSync) {
        Cedar.tag("Home").d("loadInitialData: data version changed, syncing metadata version=${check.remoteVersion}")
        repository.fullSync(questionSetId = null, remoteVersion = check.remoteVersion)
        pendingSyncVersion = check.remoteVersion
    }
}
```

With:

```kotlin
bundledContentLoader.ensureSeeded()
```

Also remove the `private var pendingSyncVersion: Int? = null` field and any reference to it later in the file (search for `pendingSyncVersion`); since we no longer track a remote version, the variable has no consumer.

In the constructor, remove `private val syncQuestions: SyncQuestionsUseCase` and `private val dataSyncManager: DataSyncManager`; add `private val bundledContentLoader: BundledContentLoader`. Update imports: remove `SyncQuestionsUseCase` and `DataSyncManager` imports; add `BundledContentLoader`.

Also locate the line `syncQuestions()` (around line 263) — delete that whole call. With bundle-based seeding there is nothing to refresh per state-change; reads come from Room.

- [ ] **Step 3: Update Koin registration for these containers**

In `shared/src/commonMain/kotlin/com/merkost/honq/presentation/di/PresentationModule.kt` (or wherever `HomeContainer` and `OnboardingContainer` are registered — grep `HomeContainer\|OnboardingContainer` under `presentation/di/`), update the constructor parameter list to match. Remove `get<DataSyncManager>()` / `get<SyncQuestionsUseCase>()` arguments and add `get<BundledContentLoader>()`.

- [ ] **Step 4: Build**

```bash
./gradlew :androidApp:assembleDebug 2>&1 | tail -30
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 5: Manual smoke (Android)**

```bash
./gradlew :androidApp:installDebug
adb shell am start -n com.merkost.honq/.MainActivity
```

Verify in the app:
- App launches without splash hang.
- Onboarding shows the list of states.
- Home shows question sets and category tiles.
- A practice session loads questions; a question with an image renders the image (this still works because old `QuestionEntity.imageUrl` rows hold full Supabase HTTPS URLs that Coil can load — image migration happens in Task 9 once the mapper change re-seeds).

- [ ] **Step 6: Commit**

```bash
git add -A
git commit -m "feat: seed Room from bundle on startup, drop sync calls from screens

HomeContainer and OnboardingContainer now invoke
BundledContentLoader.ensureSeeded() instead of dataSyncManager. The
Supabase code is still present and registered in DI; deletion follows
in upcoming commits once image resolution is migrated and the repo
interface is trimmed."
```

---

## Task 8: Switch `GetStateResourcesUseCase` to `StateResourcesProvider`

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/merkost/honq/domain/usecase/GetStateResourcesUseCase.kt`
- Modify: `shared/src/commonMain/kotlin/com/merkost/honq/domain/di/DomainModule.kt`

Today the use case calls `QuestionApi.fetchStateResources(stateId)` directly — a remote call on every Home render. Replace with a synchronous read from `StateResourcesProvider` (loaded eagerly during `ensureSeeded`).

- [ ] **Step 1: Rewrite the use case**

Replace `shared/src/commonMain/kotlin/com/merkost/honq/domain/usecase/GetStateResourcesUseCase.kt` with:

```kotlin
package com.merkost.honq.domain.usecase

import com.merkost.honq.core.util.Result
import com.merkost.honq.data.local.seed.StateResourcesProvider
import com.merkost.honq.domain.model.StateResource
import org.kimplify.cedar.logging.Cedar

class GetStateResourcesUseCase(
    private val provider: StateResourcesProvider,
) {
    suspend operator fun invoke(stateId: String): Result<List<StateResource>> = try {
        val resources = provider.getByState(stateId)
        Cedar.tag("StateResources").d("getByState($stateId): ${resources.size} rows")
        Result.Success(resources)
    } catch (e: Exception) {
        Cedar.tag("StateResources").e("getByState($stateId) failed: ${e.message}", e)
        Result.Error(e)
    }
}
```

- [ ] **Step 2: Update DI registration**

In `shared/src/commonMain/kotlin/com/merkost/honq/domain/di/DomainModule.kt`, find `factory { GetStateResourcesUseCase(get()) }` and confirm Koin resolves `StateResourcesProvider` for the single argument — since the use case now depends on `StateResourcesProvider` (not `QuestionApi`) and Koin uses positional `get()`, the registration line itself need not change unless the dependency was explicitly typed. If an explicit type was used, change `get<QuestionApi>()` to `get<StateResourcesProvider>()`.

- [ ] **Step 3: Build**

```bash
./gradlew :androidApp:assembleDebug 2>&1 | tail -20
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Manual smoke**

Reinstall the app, navigate to Home → "Helpful resources" section (or wherever state resources surface). Verify links appear (16 rows total, filtered by selected state) and tapping one opens the URL.

- [ ] **Step 5: Commit**

```bash
git add shared/src/commonMain/kotlin/com/merkost/honq/domain/usecase/GetStateResourcesUseCase.kt \
        shared/src/commonMain/kotlin/com/merkost/honq/domain/di/DomainModule.kt
git commit -m "refactor: read state resources from in-memory provider, not Supabase"
```

---

## Task 9: Switch image resolution to `BundledImagePath`

**Files:**
- Modify: every Compose UI file that calls `SupabaseConfig.getStorageUrl(...)` to render question images.

Today the entity holds a full Supabase URL (legacy mapper baked it in). After Task 7 a re-seed has happened: every existing user's `QuestionEntity.imageUrl` now contains the relative path (`questions/nt/CSB002.png`) because the new mapper (Task 2) stored the raw value. UI call sites must switch from `SupabaseConfig.getStorageUrl(path)` → `BundledImagePath.resolve(path)`.

- [ ] **Step 1: Find all call sites**

```bash
grep -rn 'SupabaseConfig\.getStorageUrl\|SupabaseConfig\b' \
    shared/src/commonMain/kotlin/com/merkost/honq/presentation \
    androidApp/src 2>/dev/null
```

Expected: a small handful of hits (image-rendering composables in Practice/Mock-test/QuestionDetail screens).

- [ ] **Step 2: Replace each call**

For each match, change:
```kotlin
import com.merkost.honq.data.remote.api.SupabaseConfig
// ...
val imageUrl = SupabaseConfig.getStorageUrl(question.imageUrl)
```
to:
```kotlin
import com.merkost.honq.data.local.seed.BundledImagePath
// ...
val imageUrl = BundledImagePath.resolve(question.imageUrl)
```

The return shape stays a nullable `String?`, so downstream Coil-loading logic works without further changes.

- [ ] **Step 3: Build**

```bash
./gradlew :androidApp:assembleDebug 2>&1 | tail -20
```

Expected: BUILD SUCCESSFUL. There should be NO remaining `SupabaseConfig` references outside the `data/remote/api/` package — verify:

```bash
grep -rn 'SupabaseConfig' shared/src/commonMain/kotlin androidApp/src \
  | grep -v 'data/remote/api/SupabaseConfig.kt'
```

Expected: empty.

- [ ] **Step 4: Manual smoke (image rendering)**

Reinstall the app, navigate to a question with an image (for example, any NT question — most have signs). Verify the image renders. If it 404s or shows a broken-image icon, double-check that:
- The image file actually exists at `shared/src/commonMain/composeResources/files/content/v1/questions/<state>/<file>.png`.
- `BundledImagePath.resolve` returned the right path (log it temporarily if needed).
- Compose Multiplatform's Coil resource loader is registered (it usually is by default in CMP 1.7+).

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "feat: resolve question image URIs from bundled resources"
```

---

## Task 10: Trim `QuestionRepository` interface and `QuestionRepositoryImpl`

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/merkost/honq/domain/repository/QuestionRepository.kt`
- Modify: `shared/src/commonMain/kotlin/com/merkost/honq/data/repository/QuestionRepositoryImpl.kt`

Remove `syncQuestions()`, `syncQuestions(questionSetId: String)`, `syncStates()`, `fullSync(...)`, `getLastSyncTime(...)`, and `isDatabaseEmpty()` from the interface and impl. The impl drops its `questionApi`, `dataSyncManager`, and `syncPreferences` constructor parameters. `getLastSyncTime` callers (if any beyond the home screen, which we already cleaned) and `isDatabaseEmpty` callers go away with the home-container changes from Task 7.

- [ ] **Step 1: Trim the interface**

In `shared/src/commonMain/kotlin/com/merkost/honq/domain/repository/QuestionRepository.kt`, delete:

```kotlin
suspend fun syncQuestions(): Result<Unit>
suspend fun syncQuestions(questionSetId: String): Result<Unit>
suspend fun syncStates(): Result<Unit>
suspend fun fullSync(questionSetId: String?, remoteVersion: Int? = null): Result<Unit>
suspend fun getLastSyncTime(questionSetId: String): kotlin.time.Instant?
suspend fun isDatabaseEmpty(): Boolean
```

Also delete `suspend fun hasQuestionsForSet(questionSetId: String): Boolean` if it's no longer called anywhere — search for callers first:

```bash
grep -rn 'hasQuestionsForSet' shared/src/commonMain/kotlin
```

If empty, remove it from the interface and impl.

- [ ] **Step 2: Trim the implementation**

In `shared/src/commonMain/kotlin/com/merkost/honq/data/repository/QuestionRepositoryImpl.kt`:

- Remove constructor parameters: `private val questionApi: QuestionApi`, `private val dataSyncManager: DataSyncManager`. Keep `localDataSource`, `dispatchers`, `json`, and `syncPreferences` for now (the latter three may be used by other methods).
- If `syncPreferences` is only referenced by deleted methods, remove it too. After the deletions below, re-grep within the file for `syncPreferences\.` — if zero hits, drop the param.
- Delete the bodies of `syncQuestions`, `syncStates`, `fullSync`, `getLastSyncTime`, `isDatabaseEmpty`, `hasQuestionsForSet` (if applicable).
- Remove the private `data class SyncData(...)` at the bottom of the file.
- Remove imports that become unused: `QuestionApi`, `DataSyncManager`, `dto.*`, `mapper.toEntity` (actually keep `toEntity` only if BundledContentLoader-style mappers are imported here — they shouldn't be), `kotlinx.coroutines.async/coroutineScope`.

The impl should now consist purely of read methods that delegate to `localDataSource`.

- [ ] **Step 3: Update DI**

In `shared/src/commonMain/kotlin/com/merkost/honq/data/di/DataModule.kt`, change the `QuestionRepositoryImpl(...)` registration to pass only the parameters that remain:

```kotlin
single<QuestionRepository> {
    QuestionRepositoryImpl(get(), get(), get())   // localDataSource, dispatchers, json
}
```

(Adjust to whatever final parameter list the impl has.)

- [ ] **Step 4: Build**

```bash
./gradlew :androidApp:assembleDebug 2>&1 | tail -30
```

Expected: BUILD SUCCESSFUL. If compile errors remain, they point at additional callers of removed methods — most likely in `SyncQuestionsUseCase`, which is deleted in Task 11. To keep this task compiling on its own, also delete `SyncQuestionsUseCase.kt` here:

```bash
rm shared/src/commonMain/kotlin/com/merkost/honq/domain/usecase/SyncQuestionsUseCase.kt
```

Plus its registration in `DomainModule.kt`. (`HomeContainer` no longer references it after Task 7.)

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "refactor: make QuestionRepository read-only; drop sync methods"
```

---

## Task 11: Delete remaining Supabase code

**Files:**
- Delete: `shared/src/commonMain/kotlin/com/merkost/honq/data/remote/api/QuestionApi.kt`
- Delete: `shared/src/commonMain/kotlin/com/merkost/honq/data/remote/api/AppConfigApi.kt`
- Delete: `shared/src/commonMain/kotlin/com/merkost/honq/data/remote/api/SupabaseConfig.kt`
- Delete: `shared/src/commonMain/kotlin/com/merkost/honq/data/remote/` (entire directory once the api/ files are gone)
- Delete: `shared/src/commonMain/kotlin/com/merkost/honq/data/repository/DataSyncManager.kt`
- Delete: `shared/src/commonMain/kotlin/com/merkost/honq/data/di/FakeDataModule.kt`
- Modify: `shared/src/commonMain/kotlin/com/merkost/honq/data/di/DataModule.kt` (remove `SupabaseClient`, `QuestionApi`, `AppConfigApi`, `DataSyncManager` `single { ... }` blocks)

After Tasks 7–10 nothing references these symbols. Delete cleanly.

- [ ] **Step 1: Delete the files**

```bash
cd /Users/merkost/StudioProjects/Honq
git rm shared/src/commonMain/kotlin/com/merkost/honq/data/remote/api/QuestionApi.kt \
       shared/src/commonMain/kotlin/com/merkost/honq/data/remote/api/AppConfigApi.kt \
       shared/src/commonMain/kotlin/com/merkost/honq/data/remote/api/SupabaseConfig.kt \
       shared/src/commonMain/kotlin/com/merkost/honq/data/repository/DataSyncManager.kt \
       shared/src/commonMain/kotlin/com/merkost/honq/data/di/FakeDataModule.kt
# remove the now-empty remote/ tree
rmdir shared/src/commonMain/kotlin/com/merkost/honq/data/remote/api 2>/dev/null
rmdir shared/src/commonMain/kotlin/com/merkost/honq/data/remote 2>/dev/null
```

- [ ] **Step 2: Strip `DataModule.kt`**

In `shared/src/commonMain/kotlin/com/merkost/honq/data/di/DataModule.kt`, remove these lines (and their `import` counterparts at the top):

```kotlin
single<SupabaseClient> { SupabaseConfig.createClient() }
single { QuestionApi(get()) }
single { AppConfigApi(get()) }
single { DataSyncManager(get(), get()) }
```

Remove these imports:
```kotlin
import com.merkost.honq.data.remote.api.AppConfigApi
import com.merkost.honq.data.remote.api.QuestionApi
import com.merkost.honq.data.remote.api.SupabaseConfig
import com.merkost.honq.data.repository.DataSyncManager
import io.github.jan.supabase.SupabaseClient
```

- [ ] **Step 3: Make sure no source file still imports `io.github.jan.supabase.*` or `data.remote.*`**

```bash
grep -rn 'io\.github\.jan\.supabase\|com\.merkost\.honq\.data\.remote' \
    shared/src/commonMain/kotlin androidApp/src 2>/dev/null
```

Expected: empty.

- [ ] **Step 4: Build**

```bash
./gradlew :androidApp:assembleDebug 2>&1 | tail -30
```

Expected: BUILD SUCCESSFUL. If something still imports from the deleted package, fix it (likely a stale Koin registration in some module file).

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "chore: delete Supabase API, config, and sync manager"
```

---

## Task 12: Drop the Supabase SDK and credentials from build config

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `shared/build.gradle.kts`
- Modify: `local.properties`

Removes the actual Supabase Kotlin SDK from the dependency graph and the `BuildKonfig` fields that surfaced the URL/key.

- [ ] **Step 1: Remove the version and library coordinates**

In `gradle/libs.versions.toml`:

- Delete the line `supabase = "3.4.1"` from the `[versions]` block.
- Delete these lines from the `[libraries]` block:
  ```toml
  supabase-postgrest = { module = "io.github.jan-tennert.supabase:postgrest-kt", version.ref = "supabase" }
  supabase-functions = { module = "io.github.jan-tennert.supabase:functions-kt", version.ref = "supabase" }
  ```

- [ ] **Step 2: Drop dependencies and BuildKonfig fields**

In `shared/build.gradle.kts`:

- Delete these two lines from the `commonMain.dependencies` block (around lines 86–87):
  ```kotlin
  implementation(libs.supabase.postgrest)
  implementation(libs.supabase.functions)
  ```
- Delete these two `buildConfigField` lines from the `BuildKonfig` block (around lines 151–152):
  ```kotlin
  buildConfigField(STRING, "SUPABASE_URL", getLocalProperty("supabase.url", ""))
  buildConfigField(STRING, "SUPABASE_KEY", getLocalProperty("supabase.key", ""))
  ```

- [ ] **Step 3: Strip `local.properties`**

Edit `local.properties`. Remove these three lines:
```
# Supabase Configuration
# Get these from: Supabase Dashboard > Settings > API
supabase.url=https://qhgpwybskpcrjykntvyo.supabase.co
supabase.key=sb_publishable_K8uMTcyDnAY_6hImstx37Q_f3JVI4zn
```

`local.properties` is gitignored — this is local cleanup, not a commit.

- [ ] **Step 4: Build clean**

```bash
./gradlew clean :androidApp:assembleDebug 2>&1 | tail -30
```

Expected: BUILD SUCCESSFUL with no Supabase artifacts pulled.

Verify no Supabase artifacts remain in the dependency tree:
```bash
./gradlew :shared:dependencies --configuration releaseRuntimeClasspath 2>&1 \
  | grep -i supabase
```

Expected: empty.

- [ ] **Step 5: Commit**

```bash
git add gradle/libs.versions.toml shared/build.gradle.kts
git commit -m "chore: remove Supabase Kotlin SDK and BuildKonfig credentials"
```

---

## Task 13: Archive docs and update README

**Files:**
- Move: `docs/SUPABASE_SCHEMA.md` → `docs/archive/SUPABASE_SCHEMA.md`
- Modify: `README.md` (remove any Supabase setup instructions)

Preserve the schema doc as historical reference — useful if the team ever needs to look back at what tables existed — but get it out of the active docs path.

- [ ] **Step 1: Archive the schema doc**

```bash
mkdir -p docs/archive
git mv docs/SUPABASE_SCHEMA.md docs/archive/SUPABASE_SCHEMA.md
```

- [ ] **Step 2: Update README**

Open `README.md`. If any section mentions Supabase setup, configuration, or the URL/key in `local.properties`, delete it. The current top of the README doesn't reference Supabase directly, so the change may be a no-op — confirm with:

```bash
grep -i supabase README.md
```

If the grep returns hits, remove those passages. If empty, leave the README alone.

- [ ] **Step 3: Add a note at the top of the archived schema doc**

Open `docs/archive/SUPABASE_SCHEMA.md` and add at the very top, just below the title:

```markdown
> **Archived 2026-04-28.** This schema describes the Supabase backend the
> Honq app used for content delivery from launch through April 2026. The
> backend was retired in favor of bundled offline content; see
> `docs/superpowers/specs/2026-04-28-supabase-removal-design.md` for the
> migration plan. Kept here for historical reference only.
```

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "docs: archive Supabase schema reference"
```

---

## Task 14: Manual end-to-end smoke test

**Files:** none modified — verification only.

Verifies the migration on real devices. No automated test covers the full Coil-loads-from-Compose-resource path, so this step is required before considering the work done.

- [ ] **Step 1: Fresh install — Android, airplane mode**

```bash
adb uninstall com.merkost.honq 2>/dev/null
adb shell svc wifi disable
adb shell svc data disable
./gradlew :androidApp:installDebug
adb shell am start -n com.merkost.honq/.MainActivity
```

Verify:
- App launches without network.
- Splash dismisses (seeding completes within a few seconds).
- Onboarding lists 8 states.
- Selecting NSW + Car shows ≥360 questions in practice.
- Starting a practice session loads questions correctly.
- A question with an image (e.g. NT category SIGN questions) renders the image.
- Completing a mock test saves a result; reopening the result review shows it.

Re-enable connectivity:
```bash
adb shell svc wifi enable
adb shell svc data enable
```

- [ ] **Step 2: Fresh install — iOS Simulator (or device)**

Open `iosApp/iosApp.xcodeproj` in Xcode. Build and run on a simulator. Repeat the verification list above (manual taps).

- [ ] **Step 3: Upgrade path — Android**

If you have a previously-installed build (the Supabase-era one) on a device:
- Take a `pm` data snapshot if possible, or just install the old build first, populate some progress (mark a few favorites, complete a mock test), then sideload the new build via `adb install -r app-debug.apk`.
- Confirm: app boots; favorites and mock-test history survive; question text/images still render; no crash on first run.

If no old build is conveniently available, you can simulate the upgrade by manually editing `localDataVersion` in DataStore preferences using a debugger, or by building the prior commit before this branch and then rebuilding HEAD. Either way, the goal is to verify `BUNDLED_DATA_VERSION = 2 > localDataVersion = 1` triggers exactly one re-seed.

- [ ] **Step 4: Final commit (release notes / RELEASE_NOTES.md)**

If `RELEASE_NOTES.md` is maintained, add an entry for the next version describing the offline migration:

```markdown
## vX.Y.Z (2026-04-28)
- Content (questions, images) is now bundled with the app — no network required for practice.
- Supabase backend retired; updates ship with new app releases.
```

```bash
git add RELEASE_NOTES.md
git commit -m "docs: note offline content migration in release notes"
```

---

## Self-Review Checklist

After implementing all tasks, verify:

- [ ] `grep -rn 'io\.github\.jan\.supabase' shared/ androidApp/` returns empty.
- [ ] `grep -rn 'SupabaseConfig\|QuestionApi\|AppConfigApi\|DataSyncManager\|SyncQuestionsUseCase' shared/ androidApp/` returns empty.
- [ ] `grep -i supabase local.properties` returns empty.
- [ ] `./gradlew clean :androidApp:assembleDebug :androidApp:assembleRelease` BUILD SUCCESSFUL.
- [ ] All unit tests pass: `./gradlew :shared:testDebugUnitTest`.
- [ ] App boots in airplane mode on a fresh install (Android + iOS).
- [ ] Question images render.
- [ ] Existing-user upgrade path re-seeds correctly without losing user progress.
- [ ] `BUNDLED_DATA_VERSION = 2` is correctly set in `BundledContentLoader.kt`.
