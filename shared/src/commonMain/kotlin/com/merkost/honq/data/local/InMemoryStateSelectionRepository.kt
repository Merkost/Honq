package com.merkost.honq.data.local

import com.merkost.honq.domain.repository.StateSelectionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class InMemoryStateSelectionRepository : StateSelectionRepository {
    private val selectedState = MutableStateFlow<String?>(null)

    override val selectedStateId: StateFlow<String?> = selectedState

    override fun setSelectedStateId(stateId: String) {
        selectedState.value = stateId
    }
}
