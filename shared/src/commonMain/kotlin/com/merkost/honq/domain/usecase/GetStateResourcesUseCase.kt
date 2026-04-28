package com.merkost.honq.domain.usecase

import com.merkost.honq.core.util.Result
import com.merkost.honq.core.util.runCatching as runCatchingResult
import com.merkost.honq.data.remote.api.FirestoreContentApi
import com.merkost.honq.data.remote.mapper.toDomain
import com.merkost.honq.domain.model.StateResource

class GetStateResourcesUseCase(
    private val api: FirestoreContentApi
) {
    suspend operator fun invoke(stateId: String): Result<List<StateResource>> =
        runCatchingResult {
            api.fetchStateResources(stateId)
                .map { it.toDomain() }
                .sortedBy { it.displayOrder }
        }
}
