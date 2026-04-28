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
    fun QuestionDto_toEntity_preserves_raw_imageUrl_path() {
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
        assertEquals("nsw", entity.stateId)
        assertEquals("road_rules", entity.categoryId)
        assertEquals("""["A","B","C","D"]""", entity.options)
    }

    @Test
    fun QuestionDto_toEntity_tolerates_null_imageUrl() {
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
        assertEquals("", entity.explanation)
    }

    @Test
    fun StateDto_toEntity_preserves_all_fields() {
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
