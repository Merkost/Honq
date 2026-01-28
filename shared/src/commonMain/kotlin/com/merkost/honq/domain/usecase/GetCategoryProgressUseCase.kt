package com.merkost.honq.domain.usecase

import com.merkost.honq.core.util.Result
import com.merkost.honq.domain.model.CategoryProgress
import com.merkost.honq.domain.repository.QuestionRepository
import com.merkost.honq.domain.repository.QuestionSetSelectionRepository

class GetCategoryProgressUseCase(
    private val repository: QuestionRepository,
    private val questionSetSelectionRepository: QuestionSetSelectionRepository
) {
    suspend operator fun invoke(): Result<Map<String, CategoryProgress>> {
        val questionSetId = questionSetSelectionRepository.selectedQuestionSetId.value
            ?: return Result.Success(emptyMap())
        return repository.getCategoryProgress(questionSetId)
    }
}
