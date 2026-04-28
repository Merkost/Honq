# Firestore Content Schema

Honq's content backend lives in Firestore (project `honq-ac8e4`, `(default)`
database, `australia-southeast1`). Documents mirror the shape of the former
Supabase tables; field names are snake_case so existing DTOs (which use
`@SerialName`) decode unchanged through the gitlive Firestore decoder.

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

All collections are read-only from clients. Writes happen via
`scripts/export-supabase-to-firestore.py` (run from a service account) or
manual edits in the Firebase Console.

## Image Hosting

Question images live in **Firebase Hosting**, not Firebase Storage. (Honq
stays on the Spark plan; Storage now requires Blaze, while Hosting's free
tier — 10 GB stored, 360 MB/day transfer — covers Honq's traffic shape.)

PNGs are deployed from the repo's `public/questions/` directory. The Hosting
config in `firebase.json` adds `Cache-Control: public, max-age=31536000,
immutable` to `/questions/**` so CDN caches keep them indefinitely. The image
URL pattern is constructed by `HostedImageUrlBuilder`:

```
https://honq-ac8e4.web.app/questions/<filename>
```

The base URL is read from `BuildKonfig.FIREBASE_HOSTING_BASE_URL` (defaults
to `https://honq-ac8e4.web.app`; override via `firebase.hosting.base.url` in
`local.properties` if a custom domain is set up).

To refresh images: re-run `scripts/export-supabase-to-firestore.py` (which
restages PNGs into `public/questions/`), then
`firebase deploy --only hosting`. Note that `public/questions/*.png` is
gitignored — images live on disk during deploy but never enter git history.

## Security Rules

See `firestore.rules` (deployed via `firebase deploy --only firestore:rules`).
Rules enforce:
- Public read of `is_active = true` documents on the gated collections
  (`questions`, `license_types`, `assessment_types`, `question_sets`,
  `state_resources`).
- Public read on the always-on collections (`states`, `categories`,
  `question_set_categories`, `app_config`).
- All client writes rejected.

There are no Firebase Storage rules because we do not use Firebase Storage.

## Indexes

Current query shape uses only equality + the `updated_at > since` range filter
on the `questions` collection. Firestore creates a single-field index for
`updated_at` automatically, and equality-only multi-field queries do not
require composite indexes.

If we ever need `is_active = true AND updated_at > X` as a single query
(currently we do them separately via two `whereEqualTo` clauses + one
`whereGreaterThan` clause, which Firestore handles without a composite index
on small datasets), add the index via `firestore.indexes.json` and
`firebase deploy --only firestore:indexes`. The current `firestore.indexes.json`
is empty.

## Configuration files

- `firebase.json` — points Firestore rules at `firestore.rules` and Hosting
  at `public/`.
- `firestore.rules` — security rules, deployed.
- `firestore.indexes.json` — empty index spec.
- `.firebaserc` — pins `honq-ac8e4` as the default project for the repo.

## Authoring workflow (post-migration)

The Supabase project is paused, not deleted, for ~30 days post-migration as a
rollback safety net. Once retired:

1. **For tabular content edits** — either edit Firestore documents directly
   in the Firebase Console (small changes), or maintain content in a private
   repo as JSON and write a small sync script that mirrors the export
   script's structure.
2. **For image updates** — drop new PNGs into `public/questions/`, run
   `firebase deploy --only hosting`. The year-long `Cache-Control: immutable`
   header means clients won't see changed images at the same path until the
   app's local Coil cache clears (or the URL changes). For a forced refresh,
   change the filename and update the corresponding `image_url` field in the
   Firestore document.
