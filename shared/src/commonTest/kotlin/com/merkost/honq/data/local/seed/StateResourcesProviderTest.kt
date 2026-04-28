package com.merkost.honq.data.local.seed

import com.merkost.honq.domain.model.ResourceType
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StateResourcesProviderTest {
    private val json = Json { ignoreUnknownKeys = true }

    private val sampleJson = """
        [
          {
            "id": "1",
            "state_id": "NSW",
            "title": "Road User Handbook",
            "url": "https://example.com/handbook.pdf",
            "resource_type": "pdf",
            "license_type": "car",
            "display_order": 1,
            "is_active": true
          },
          {
            "id": "2",
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
    fun getByState_returns_matching_rows_lowercased() = runBlocking {
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
    fun getByState_unknown_returns_empty() = runBlocking {
        val provider = StateResourcesProvider(json) { sampleJson.encodeToByteArray() }
        provider.ensureLoaded()
        assertTrue(provider.getByState("vic").isEmpty())
    }

    @Test
    fun ensureLoaded_is_idempotent() = runBlocking {
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
