package com.merkost.honq.domain.usecase

import com.merkost.honq.domain.repository.QuestionSetSelectionRepository
import kotlinx.coroutines.flow.StateFlow

class ObserveSelectedQuestionSetUseCase(
    private val repository: QuestionSetSelectionRepository
) {
    operator fun invoke(): StateFlow<String?> = repository.selectedQuestionSetId
}
