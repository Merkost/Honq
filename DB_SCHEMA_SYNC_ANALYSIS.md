# Database Schema & Sync Process Analysis

## Schema Overview (11 tables, Room v1)

The schema is well-structured with clear separation between **reference data** (states, categories, license_types, assessment_types, question_sets, question_set_categories), **content data** (questions), and **user-generated data** (answer_history, favorite_questions, mock_test_results, mock_test_answers).

---

## Schema Issues

### 1. Missing Foreign Keys on Core Tables

`QuestionEntity` references `categoryId`, `questionSetId`, and `stateId` but has **no foreign key constraints**. Similarly, `QuestionSetEntity` references `stateId`, `licenseTypeId`, and `assessmentTypeId` with no FK constraints. `AnswerHistoryEntity` references `questionId` without a FK. Only `MockTestAnswerEntity` correctly declares a foreign key.

**Impact**: Orphaned rows can accumulate — e.g., deleting a question set leaves dangling questions. The `deleteByQuestionSet()` call in `syncQuestions()` works around this manually, but it's fragile.

**Suggestion**: Add `@ForeignKey` declarations with appropriate `onDelete` actions:
- `QuestionEntity.questionSetId → QuestionSetEntity.id` (CASCADE)
- `QuestionEntity.categoryId → CategoryEntity.id` (NO ACTION)
- `QuestionEntity.stateId → StateEntity.id` (NO ACTION)
- `QuestionSetEntity.stateId → StateEntity.id` (NO ACTION)
- `AnswerHistoryEntity.questionId → QuestionEntity.id` (CASCADE)
- `FavoriteQuestionEntity.questionId → QuestionEntity.id` (CASCADE)

### 2. String Timestamps Instead of Typed Values

All `createdAt`, `updatedAt`, `answeredAt`, and `completedAt` fields are stored as `String`. This prevents SQL-level date comparisons and ordering from being reliable (depends on consistent formatting). The incremental sync relies on `MAX(updatedAt)` string comparison (`QuestionDao` line 84).

**Suggestion**: Use `Long` (epoch millis) for timestamps with a Room `@TypeConverter`. This makes comparisons deterministic and avoids format-dependent bugs. At minimum, enforce ISO 8601 format with timezone (`Z` suffix) consistently.

### 3. JSON-Serialized `options` Column

`QuestionEntity.options` stores a JSON-serialized list of strings. The search query at `QuestionDao` line 48 does `LIKE :query` on raw JSON, matching brackets and quotes.

**Suggestion**: If search over options isn't a key feature, this is acceptable. But be aware the search matches JSON syntax characters. Consider FTS for robust search.

### 4. Missing Index on `MockTestResultEntity.questionSetId`

`MockTestResultEntity` has a `questionSetId` column but no index on it. Filtering mock test results by question set will be a full table scan.

**Suggestion**: Add `@Index(value = ["questionSetId"])`.

### 5. Missing Composite Index on `AnswerHistoryEntity`

Several queries filter by `wasCorrect = 1` combined with joins on `questionId`. A composite index on `(questionId, wasCorrect)` would help.

### 6. `getMockTestQuestions()` Hardcodes LIMIT 45

`QuestionDao` line 27 has `LIMIT 45` hardcoded. Should come from question set config.

---

## Sync Process Issues

### 7. Sequential API Calls in `syncStates()` — HIGH PRIORITY

`QuestionRepositoryImpl` lines 207-260 makes **6 sequential network requests**. These are independent and could run in parallel.

**Suggestion**: Use `coroutineScope { async { } }` to fetch all 6 in parallel:

```kotlin
coroutineScope {
    val states = async { questionApi.fetchStates(includeInactive = true) }
    val licenseTypes = async { questionApi.fetchLicenseTypes(includeInactive = true) }
    val assessmentTypes = async { questionApi.fetchAssessmentTypes(includeInactive = true) }
    val questionSets = async { questionApi.fetchQuestionSets(includeInactive = true) }
    val categories = async { questionApi.fetchCategories(includeInactive = true) }
    val qsCategories = async { questionApi.fetchQuestionSetCategories(includeInactive = true) }
    // await all and upsert...
}
```

### 8. No Transactional Sync — HIGH PRIORITY

Reference data upserts in `syncStates()` happen without a database transaction. A crash mid-sync leaves the DB partially updated.

**Suggestion**: Wrap the upsert phase in a Room `withTransaction { }` block.

### 9. Full Fetch Deletes Before Insert (Data Loss Window) — HIGH PRIORITY

In `syncQuestions()` line 175-176: full fetch calls `deleteQuestionsByQuestionSet()` **before** inserting new data. If the insert fails, the user loses all questions for that set.

**Suggestion**: Upsert all rows and let PK conflict resolution handle it — skip the delete. Or use a single transaction.

### 10. Incremental Sync Misses Deletions — HIGH PRIORITY

`fetchUpdatedQuestions()` only fetches questions where `updated_at > since`. Deleted or deactivated questions before that timestamp remain in the local DB.

**Suggestion**: Add soft-delete support, periodically full sync on version changes, or return deleted IDs from the API.

### 11. States Uses `insert` Instead of `upsert`

`QuestionRepositoryImpl` line 213 uses `insertStates()` while other tables use `upsert*()`. Inconsistent conflict handling.

**Suggestion**: Use `upsertStates()` consistently.

### 12. Version Tracking Disconnected from Sync

`DataSyncManager.markSyncCompleted()` is never called from `syncStates()`/`syncQuestions()`. The caller must remember to call it.

**Suggestion**: Move `markSyncCompleted()` into `fullSync()` after success.

### 13. No Retry or Backoff on Sync Failures

Network calls have no retry logic. Transient failures cause full sync failure.

**Suggestion**: Add retry with exponential backoff at the API or repository layer.

### 14. No Sync Progress Reporting

No progress feedback during sync. Initial sync downloads all data with no UI indication.

**Suggestion**: Expose `Flow<SyncProgress>` from sync manager.

### 15. Naming Ambiguity: `clearSyncTimes()` vs `resetSyncState()`

The difference isn't obvious from names alone.

**Suggestion**: Rename to `clearQuestionSetSyncTimestamps()` and `resetAllSyncData()`.

---

## Priority Summary

| Priority | Issue | Effort |
|----------|-------|--------|
| **High** | #9 - Delete-before-insert data loss window | Low |
| **High** | #7 - Parallelize `syncStates()` API calls | Low |
| **High** | #10 - Incremental sync misses deletions | Medium |
| **High** | #8 - No transactional sync | Low |
| **Medium** | #1 - Missing foreign keys | Medium (migration) |
| **Medium** | #12 - Version tracking disconnected | Low |
| **Medium** | #11 - States insert vs upsert inconsistency | Low |
| **Medium** | #2 - String timestamps | Medium (migration) |
| **Low** | #4 - Missing index on mock_test_results | Low (migration) |
| **Low** | #5 - Missing composite index on answer_history | Low (migration) |
| **Low** | #6 - Hardcoded LIMIT 45 | Low |
| **Low** | #13 - No retry logic | Medium |
| **Low** | #14 - No sync progress | Medium |
| **Low** | #15 - Naming ambiguity | Low |
