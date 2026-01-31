package com.merkost.honq.domain.usecase

import com.merkost.honq.domain.repository.QuestionSetSelectionRepository

class SetSelectedQuestionSetUseCase(
    private val repository: QuestionSetSelectionRepository
) {
    operator fun invoke(questionSetId: String?) {
        repository.setSelectedQuestionSetId(questionSetId)
    }
}
