package com.merkost.honq.domain.usecase

import com.merkost.honq.core.util.Result
import com.merkost.honq.domain.model.Question
import com.merkost.honq.domain.repository.QuestionRepository
import com.merkost.honq.domain.repository.QuestionSetSelectionRepository

class SearchQuestionsUseCase(
    private val repository: QuestionRepository,
    private val questionSetSelectionRepository: QuestionSetSelectionRepository
) {
    suspend operator fun invoke(query: String): Result<List<Question>> {
        val questionSetId = questionSetSelectionRepository.selectedQuestionSetId.value
            ?: return Result.Success(emptyList())
        return repository.searchQuestions(questionSetId, query)
    }
}
