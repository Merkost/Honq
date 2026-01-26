package com.merkost.honq.domain.repository

import kotlinx.coroutines.flow.StateFlow

interface StateSelectionRepository {
    val selectedStateId: StateFlow<String?>
    fun setSelectedStateId(stateId: String)
}
