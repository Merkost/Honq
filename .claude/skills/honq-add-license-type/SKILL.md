---
name: honq-add-license-type
description: Use when adding a new license type (heavy rigid trucks, motorcycle, bus, light rigid, heavy combination, learner permit variants) and/or its question set to the Honq driving-test app's offline content bundle. Triggers on phrases like "add trucks", "add HR/HC/MR/LR license", "add motorcycle", "add bus license", "add a new license type", "add a question set for X", "add a new state's question bank", or any request that grows the bundled question content. Make sure to use this skill whenever the user mentions extending the question bank with a new license category or state coverage, even if they don't explicitly say "license type".
---

# Add a License Type / Question Set to the Honq Bundle

## What this covers

The Honq app ships question content as JSON + PNG resources under `shared/src/commonMain/composeResources/files/content/v1/`. There's no remote backend at runtime; updates ship as new app releases. This skill walks through adding a new license type (e.g., Heavy Rigid trucks) to that bundle so the change reaches users.

The same workflow applies to: adding a new state, adding a new question_set within an existing state+license combination, or just adding more questions to an existing set. The license-type case is the most common and the only one with a real schema decision; the rest are mechanical.

## Two ways to author the data

**Pick one upfront — don't mix.**

### Path A: edit the JSON files directly (preferred for small additions)

Best when adding one license type, a few dozen questions, or fixing typos. Just edit `shared/src/commonMain/composeResources/files/content/v1/*.json` by hand, then jump to **Bump and ship** below. No Supabase needed.

### Path B: re-author in Supabase, then re-export

Best when adding hundreds of questions, multi-state expansion, or you want the Supabase admin UI for batch editing. Steps:

1. Resume the paused Supabase project (`https://supabase.com/dashboard/project/qhgpwybskpcrjykntvyo`). The schema is in `docs/archive/SUPABASE_SCHEMA.md` for reference.
2. Add the rows via Supabase Studio (Tables → license_types / question_sets / questions / etc.).
3. Re-add the credentials to `local.properties` (the file is gitignored, so pulling them locally is fine):
   ```
   supabase.url=https://qhgpwybskpcrjykntvyo.supabase.co
   supabase.key=<publishable read-only key from Supabase dashboard>
   ```
4. Run the exporter — this overwrites the bundle with current Supabase data:
   ```bash
   python3 scripts/export-supabase-bundle.py
   ```
5. After committing, re-pause the Supabase project and remove `supabase.*` from `local.properties` again. We don't want to leak credentials in long-lived dev shells.

Continue at **Bump and ship**.

## Schema reference

These DTOs determine the JSON shape. Field names use snake_case in the JSON (the Kotlin DTOs use `@SerialName`).

### `license_types.json`
```json
{
  "id": "heavy_rigid",
  "name": "Heavy Rigid",
  "short_name": "HR",
  "is_active": true,
  "display_order": 3,
  "created_at": "2026-04-28T00:00:00+00:00",
  "updated_at": "2026-04-28T00:00:00+00:00"
}
```
- `id`: lowercase snake_case stable identifier. Used in `question_sets.license_type_id`. Existing values: `car`, `rider`.
- `display_order`: controls sort in the license-picker UI.

### `question_sets.json`
```json
{
  "id": "nsw_heavy_rigid",
  "state_id": "nsw",
  "license_type_id": "heavy_rigid",
  "assessment_type_id": "knowledge_test",
  "mock_test_question_count": 45,
  "mock_test_time_limit_minutes": 45,
  "mock_test_pass_percentage": 75,
  "is_active": true,
  "created_at": "2026-04-28T00:00:00+00:00",
  "updated_at": "2026-04-28T00:00:00+00:00"
}
```
- `id`: convention is `<state_id>_<license_type_id>`. Used in `questions.question_set_id`.
- The `mock_test_*` fields drive the Mock Test UI (timer, pass-mark display, question count). If the new license has different official rules, set them here. NSW HR uses 45/45/75 for example.
- `assessment_type_id` is almost always `knowledge_test`. Existing assessment types: see `assessment_types.json`.

### `questions.json`
```json
{
  "id": "nsw-hr-001",
  "code": "HR001",
  "question_set_id": "nsw_heavy_rigid",
  "state_id": "nsw",
  "category": "ROAD_RULES",
  "text": "When driving a heavy rigid vehicle, the maximum speed limit unless otherwise signed is:",
  "options": ["80 km/h", "100 km/h", "90 km/h", "110 km/h"],
  "correct_index": 1,
  "explanation": "Default speed limit for heavy vehicles on NSW open roads is 100 km/h unless signed lower.",
  "image_url": null,
  "difficulty": 2,
  "is_active": true,
  "version": 1,
  "source": "manual",
  "created_at": "2026-04-28T00:00:00+00:00",
  "updated_at": "2026-04-28T00:00:00+00:00"
}
```
- `id` is a UUID-like string in the existing data, but any unique string works. Suggest `<state>-<license_short>-<seq>` for new content.
- `category` must be uppercase and match an `id` (in lowercase) in `categories.json`. The runtime mapper lowercases it before storing.
- `options` is a JSON array (not a string). The mapper converts it to a JSON-encoded string for Room.
- `image_url` is a relative path under `composeResources/files/content/v1/` (e.g., `questions/nsw/HR001.png`) or `null`.

### `question_set_categories.json` (optional)
Maps which categories appear in which question set, with a per-set display order. If absent for a new set, the runtime falls back to "categories used by questions in that set" (see `QuestionLocalDataSource.getCategoriesForQuestionSet`). You can skip this file unless you want a specific category sort order or want a category to appear before any questions exist.

## Adding question images

If your new questions reference images:

1. Place PNGs under `shared/src/commonMain/composeResources/files/content/v1/questions/<state_id>/<filename>.png`. Match the existing per-state subdir layout (`questions/nsw/...`, `questions/nt/...`).
2. Reference them in the question's `image_url` field as the relative path (e.g., `questions/nsw/HR001.png`).
3. Keep PNGs reasonably sized — current bundle averages ~64KB per image. Optimize with `oxipng` or `pngquant` before committing if files exceed ~150KB.

The runtime resolves these via `BundledImagePath.resolve(...)` and `Res.getUri(...)` (in `QuestionCard.kt`). No code changes needed for new images.

## Bump and ship

After the JSON / image edits are done:

1. **Bump the version** in `shared/src/commonMain/kotlin/com/merkost/honq/data/local/seed/BundledContentLoader.kt`:
   ```kotlin
   const val BUNDLED_DATA_VERSION = 3   // was 2
   ```
   Without this bump, existing users' Room DB will skip the re-seed and never see the new content. The version is checked against `SyncPreferences.localDataVersion` and only differing values trigger a re-seed.

2. **Validate the JSON** is parseable and FK-consistent:
   ```bash
   python3 -c "
   import json
   from pathlib import Path
   d = Path('shared/src/commonMain/composeResources/files/content/v1')
   tables = {f.stem: json.loads(f.read_text()) for f in d.glob('*.json')}
   print({k: len(v) for k, v in tables.items()})
   ids = {t: {r['id'] for r in tables[t] if 'id' in tables[t][0]} for t in ['states','license_types','assessment_types','question_sets','categories']}
   bad = [q['id'] for q in tables['questions'] if q['question_set_id'] not in ids['question_sets']]
   print('orphan questions:', len(bad), bad[:5])
   "
   ```
   `orphan questions: 0` is the green light. Anything else means a `question_set_id` typo.

3. **Run unit tests** to catch obvious regressions:
   ```bash
   ./gradlew :shared:testDebugUnitTest
   ```
   Expected: 11 tests passing (existing seed/path tests still cover the parsing and resolver).

4. **Build a debug APK** to confirm the resources package cleanly:
   ```bash
   ./gradlew :androidApp:assembleDebug
   ```

5. **Commit** as a single change describing what was added:
   ```bash
   git add shared/src/commonMain/composeResources/files/content/v1 \
           shared/src/commonMain/kotlin/com/merkost/honq/data/local/seed/BundledContentLoader.kt
   git commit -m "feat(content): add NSW heavy rigid license + 120 questions

   Bumps BUNDLED_DATA_VERSION to 3 so existing users re-seed on next
   launch."
   ```

6. **Smoke test on a real device** before the App Store release:
   - Fresh install: confirm the new license type appears in onboarding and Home, and questions load with images.
   - Upgrade install (over the prior production build): confirm the re-seed fires once on first launch and progress survives. Check `Cedar` logs for `Seed: ensureSeeded: seeding from bundle (local=2, bundle=3)`.

7. **Cut a new app release** through the normal release flow — that's the only way users actually get the new content.

## Common mistakes

- **Forgetting `BUNDLED_DATA_VERSION`.** Without the bump, only fresh installs see the new content; everyone else stays on the previous bundle. The Room DB doesn't auto-detect that the JSON changed.
- **Mixing Path A and Path B mid-task.** If you start editing JSON by hand and then run the export script, you'll lose your hand edits — the script overwrites everything from Supabase.
- **Inconsistent `category` casing.** `category` in `questions.json` is uppercase ("ROAD_RULES"), but `id` in `categories.json` is lowercase ("road_rules"). The mapper lowercases the question's category before lookup. Stick to the existing convention.
- **Adding questions without a matching `question_set_id`.** PostgREST / Supabase will reject this if you go through Path B; with Path A nobody catches it until the FK insert blows up at app start. The validation snippet above catches it.
- **Re-using a question `id` across sets.** IDs are globally unique (PRIMARY KEY in Room). If you copy a question from one set to another, give it a fresh ID.

## When this skill doesn't apply

- **Removing a license type.** Setting `is_active: false` is the standard hide; full deletion requires migrating user progress (mock test results referencing those question IDs). Out of scope here.
- **Changing the schema** (e.g., adding a new column to `questions.json`). That's a bigger change — touches Room entities, migrations, and the seed mapper. Treat it as a normal feature, not a content update.
- **Hot-fixing a typo without an app release.** There is no remote update channel. Either ship a release or accept the typo until the next one.
