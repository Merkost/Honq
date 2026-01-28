package com.merkost.honq.domain.usecase

import com.merkost.honq.core.util.Result
import com.merkost.honq.domain.model.Category
import com.merkost.honq.domain.repository.QuestionRepository
import com.merkost.honq.domain.repository.QuestionSetSelectionRepository

class GetCategoriesUseCase(
    private val repository: QuestionRepository,
    private val questionSetSelectionRepository: QuestionSetSelectionRepository
) {
    suspend operator fun invoke(): Result<List<Category>> {
        val questionSetId = questionSetSelectionRepository.selectedQuestionSetId.value
            ?: return repository.getAllActiveCategories()
        return repository.getCategoriesByQuestionSet(questionSetId)
    }
}
