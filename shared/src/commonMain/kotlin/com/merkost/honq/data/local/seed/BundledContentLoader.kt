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
