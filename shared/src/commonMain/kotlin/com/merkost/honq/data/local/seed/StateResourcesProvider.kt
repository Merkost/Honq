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
