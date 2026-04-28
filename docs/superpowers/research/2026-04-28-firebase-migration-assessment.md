# Firebase Migration Assessment for Honq

**Date:** 2026-04-28
**Branch:** `claude/firebase-vs-supabase-assessment` (off `claude/review-app-downloads-3FjoW`)
**Constraint:** A backend stays in scope — full-offline / fully-bundled content is **not** under consideration here.
**Question:** The user is giving up the Supabase free-tier slot for a different project. If Honq must keep using a remote backend, is migrating to Firebase worth it?

## TL;DR

**Yes — migrate to Firebase, but pick the minimum surface (Firestore + Storage, no Auth/Functions yet).** Honq's backend usage is shallow and read-only, the migration is mechanical, Firebase is already partially in the stack via Crashlytics, and Firestore's free tier comfortably covers a driving-test app's traffic. Estimated effort: **2–3 focused days**, including image re-hosting and a one-shot data export.

If a cheaper option exists, it's a **plain static JSON + image CDN** (e.g. GitHub Pages, Cloudflare R2 + Pages) — see "Cheaper alternative" below. That option is more work to build a CMS for later, but zero ongoing cost. Pick Firebase if you might want auth/sync within ~6 months; pick static-hosted JSON if you definitely won't.

## What Honq actually uses Supabase for today

Inspected on `claude/review-app-downloads-3FjoW`:

| Surface | Files | Notes |
|---|---|---|
| Postgrest reads | `QuestionApi.kt`, `AppConfigApi.kt` | 9 tables: `questions`, `states`, `license_types`, `assessment_types`, `question_sets`, `question_set_categories`, `categories`, `state_resources`, `app_config`. All filtered by `is_active = true`. |
| Storage (public) | `SupabaseConfig.getStorageUrl`, used in `QuestionDtoMapper` | One bucket (`questions`) holding ~392 PNGs referenced by `image_url`. |
| Functions | `install(Functions)` in `SupabaseConfig.kt` | Loaded but **not invoked anywhere** — dead weight. |
| Auth | none | RLS is "public read" on every table. |
| Realtime | none | All reads are one-shot fetches, no subscriptions. |
| Writes from client | none | Content is admin-managed; client is read-only. |

User-state data (favorites, answer history, mock test results) is already in Room, on-device. RevenueCat handles premium. **The backend is purely a content CDN with a SQL face.**

## The migration shape

Because Honq's usage is shallow and read-only, the porting work has clear boundaries:

| Concern | Supabase today | Firebase replacement | Migration cost |
|---|---|---|---|
| Tabular reads | `client.postgrest["questions"].select { filter { ... } }` | `firestore.collection("questions").where(...).get()` | Rewrite `QuestionApi.kt` + `AppConfigApi.kt` against `dev.gitlive:firebase-firestore` (already in dep family). ~½ day. |
| Filtering by `is_active` | Postgrest `filter { eq(...) }` | Firestore `whereEqualTo` (cheap, no index needed for ==) | Direct mapping. |
| Joins (e.g. `question_set_categories`) | Foreign keys + RLS | Denormalize into the document, or do client-side joins (already done — `QuestionApi` fetches per-table) | No real change; the current code already issues separate fetches per table and joins in Kotlin. |
| Image hosting | Public Supabase bucket, `getStorageUrl` builds public URL | Firebase Storage public bucket, build URL via SDK or direct `gs://...` → CDN URL | One-shot: `gsutil rsync` (or equivalent) the `questions` bucket into Firebase Storage. Update `getStorageUrl` to point at Firebase. ~½ day including verifying every URL still loads. |
| `app_config` (e.g. `data_version`, `min_app_version`) | Postgres KV table | Either a single Firestore doc `config/app` with fields, or **Firebase Remote Config** (better fit for `min_app_version` because it has built-in audience targeting). | ~2 hours. Remote Config is a stronger primitive than the current KV shape. |
| Deprecated Functions install | unused | drop it | trivial — already dead code |
| Build config | `BuildKonfig.SUPABASE_URL/KEY` from `local.properties` | `google-services.json` (already in repo for Crashlytics) | No new secret management; Firebase project is already provisioned. |
| KMP support | `io.github.jan-tennert.supabase` | `dev.gitlive:firebase-firestore` (matches the existing `dev.gitlive:firebase-crashlytics`) | Same vendor family already validated in this codebase. |

There is no auth to port, no realtime subscriptions to port, no Edge Functions to port. The conceptual mapping is **"replace `postgrest[...]` with `firestore.collection(...)`"** — and that's it.

## Why Firebase, specifically (with offline excluded)

1. **Already half in the stack.** `dev.gitlive:firebase-crashlytics:2.4.0` is in `libs.versions.toml`. The Android app already has `google-services.json` and the Google Services Gradle plugin. The KMP wrapper family is the same one (`dev.gitlive`) — adding `firebase-firestore-kt` and `firebase-storage-kt` is a drop-in.
2. **Free tier easily covers Honq's load.** Firestore free quota is 50K reads / 20K writes / 1GiB stored per day. Honq's read shape is "fetch ~9 small tables once at install + delta sync" — even at 5K daily active users that's well under quota. Storage egress free tier (5GB stored, 1GB/day download) covers the 392-image bundle and typical access patterns. No "auto-pause after a week of inactivity" gotcha like Supabase free tier.
3. **No 2-projects-per-org limit.** This is *the* constraint that triggered the question. Firebase doesn't cap free projects per org the way Supabase does.
4. **The data is document-shaped anyway.** A `questions` table with `options JSONB` and `category` as a string is already a Firestore document in disguise. Migrating doesn't require reshaping the data.
5. **Firebase Remote Config for `app_config`.** The `min_app_version` and `featured_state` use cases in `app_config` are exactly what Remote Config is for — versioning, gradual rollout, A/B targeting if you ever need it. Use it for `min_app_version`; keep `data_version` in a Firestore doc since the client uses it for delta-sync logic.
6. **Anonymous Auth is one line if you ever need it.** The doc that lives in this repo (`docs/superpowers/research/2026-04-28-firebase-vs-supabase-assessment.md`) already enumerated the future features (cross-device progress, leaderboards, question reporting). All of those need anonymous auth, and Firebase Anonymous Auth is the simplest path. You don't need it now, but the door is open.

## Why this isn't a clearcut win

Be honest about the costs:

1. **Firestore lacks Postgres-style ad-hoc querying.** Today `QuestionApi` does straightforward equality filters, so this isn't biting — but if a future feature needs "questions where difficulty BETWEEN 2 AND 3 AND category IN ('SAFETY','HAZARDS')", you'll hit Firestore's composite-index requirement and (sometimes) the lack of `OR` across fields. For Honq's current query shape this is fine.
2. **Random sampling is awkward.** `get_random_questions` and `get_mock_test_questions` (Supabase RPC functions) use `ORDER BY RANDOM()`. Firestore has no equivalent. **However:** check the current code — those Postgres functions don't appear to actually be called from `QuestionApi.kt`. The client fetches everything for the question set and does mock-test sampling in Kotlin. So this is a non-issue today, but worth flagging if you ever wanted to push that down.
3. **Admin/CMS UX is weaker.** The Supabase dashboard's table editor is genuinely nice for content authoring. Firebase Console's Firestore editor is workable but more click-heavy. If non-engineers ever edit questions, this is a paper cut. Workaround: keep questions in a JSON file in a private repo and write a small `gcloud firestore import`-style sync script. (You'd need a similar pipeline to refresh either backend; Supabase just hides it behind the dashboard.)
4. **You re-do the migration once.** Supabase removal was already planned (specs at `docs/superpowers/specs/2026-04-28-supabase-removal-design.md`). Migrating to Firebase replaces that work — same SDK swap, different vendor. The plan doc for offline embedding is now mostly not applicable.
5. **Vendor lock-in shifts, doesn't disappear.** Both Postgrest and Firestore are vendor-coupled SDKs. The data layer (`QuestionApi`, `AppConfigApi`, mappers) is already a thin abstraction — keep the `QuestionRepository` interface boundary clean and the next switch (whatever it is) will be similarly mechanical.

## Cheaper alternative: static JSON + image CDN

If the question is "do I need any database at all," the answer is probably no. The data is read-only, ~26 MB total (8 tables + 392 PNGs), updated maybe once a quarter when handbook content changes.

**Shape:**
- Bundle the 9 JSON tables and 392 PNGs into a `gh-pages` branch (or Cloudflare Pages, or any S3/R2 bucket).
- Client fetches `https://<host>/v1/questions.json` etc. on first launch, caches in Room.
- Use a single `manifest.json` with `data_version` + a SHA per file for delta-fetch.
- `min_app_version` → also a JSON file, or just inline in the manifest.

**Pros:** $0/month, no auth, no SDK, no dashboard to maintain, content changes are just `git push`.
**Cons:** No anonymous auth path, no analytics on backend reads, no easy migration to user-data features later. Building a CMS = "edit JSON in a repo."

This is *strictly cheaper and simpler* than Firebase **if** Honq genuinely never grows beyond static content. If you'd bet >30% on adding cross-device sync / leaderboards / question reporting in the next 12 months, Firebase is the better starting point because the second migration is the expensive one.

## Recommendation

**Migrate to Firebase (Firestore + Storage + Remote Config), not the static-JSON path** — but with a tight scope:

1. Add `dev.gitlive:firebase-firestore` and `dev.gitlive:firebase-storage` to `libs.versions.toml`.
2. Replace `QuestionApi` and `AppConfigApi` implementations with Firestore equivalents (preserve their public surface — the repository layer doesn't change).
3. Move `min_app_version` to Remote Config; keep `data_version` as a Firestore doc for delta-sync.
4. One-shot bulk export from Supabase → Firestore (write a tiny Python/Node script, see `scripts/export-supabase-bundle.py` already in `.gitignore` for inspiration).
5. Mirror the `questions` storage bucket into Firebase Storage; update `SupabaseConfig.getStorageUrl` (rename to `RemoteImageUrl` or similar) to build Firebase URLs.
6. Drop the Supabase deps, the `Functions` install (already dead), and the `SUPABASE_URL/KEY` build config.
7. Pause-then-delete the Supabase project (calendar reminder for ~30 days post-rollout, after telemetry confirms no client is still hitting it).

**Why this over offline:** Constraint is set — backend stays in scope.

**Why this over static-JSON:** ~50% odds Honq adds *some* user-data feature within a year (the "Future Considerations" list in `SUPABASE_SCHEMA.md` literally enumerates four). Firebase makes that next step ~3 days; static-JSON makes it a full backend project.

**Why not Supabase-on-paid:** $25/month for a hobby driving-test app is wrong-sized; the free tier is the only economically reasonable Supabase footprint here, and the free slot is already spoken for.

## Effort estimate

- Bulk data export + Firestore import: 4 hours
- Image bucket migration + URL rewrite: 4 hours
- Replace `QuestionApi` + `AppConfigApi` against Firestore SDK: 6 hours
- Wire Remote Config for `min_app_version`: 2 hours
- Smoke test on Android + iOS, verify image loads, verify app_config: 4 hours
- Drop deps, clean BuildKonfig, README updates: 2 hours

**Total: ~22 hours of focused work** (~2.5 days). Adjust upward by 1 day if the Firestore KMP wrapper has any sharp edges on iOS — the gitlive Crashlytics integration is well-trodden, but Firestore is a larger surface.

## Open questions to confirm before starting

1. Is the existing `google-services.json` for the Honq Firebase project, or piggybacking on a shared dev project? (Affects whether you need to provision a new project + bundle ID config.)
2. Does the iOS side have `GoogleService-Info.plist` already? (`androidApp/release/` exists locally — check the iOS counterpart.)
3. Is content authoring done by an engineer (writes JSON / runs scripts) or by a non-engineer (needs a dashboard)? If the latter, the Firebase Console editor is workable but the Supabase dashboard is genuinely nicer — that's the one place where the migration is a small downgrade.

## Why this doc exists

The earlier assessment at `docs/superpowers/research/2026-04-28-firebase-vs-supabase-assessment.md` (on `claude/firebase-migration-assessment`) concluded *"don't migrate, offline is enough"* — but that was written under the assumption that the offline-bundle path was the right call. With offline excluded, the conclusion flips: **migrate to Firebase, scope it tight, expect ~2.5 days of work**.
