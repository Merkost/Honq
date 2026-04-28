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
