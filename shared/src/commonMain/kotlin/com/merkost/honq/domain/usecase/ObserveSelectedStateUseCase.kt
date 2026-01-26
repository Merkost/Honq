package com.merkost.honq.domain.usecase

import com.merkost.honq.domain.repository.StateSelectionRepository
import kotlinx.coroutines.flow.StateFlow

class ObserveSelectedStateUseCase(
    private val repository: StateSelectionRepository
) {
    operator fun invoke(): StateFlow<String?> =
        repository.selectedStateId
}
