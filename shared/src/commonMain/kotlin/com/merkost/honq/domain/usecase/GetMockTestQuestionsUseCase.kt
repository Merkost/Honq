package com.merkost.honq.domain.usecase

import com.merkost.honq.core.util.Result
import com.merkost.honq.domain.model.Question
import com.merkost.honq.domain.repository.QuestionRepository
import com.merkost.honq.domain.repository.QuestionSetSelectionRepository

class GetMockTestQuestionsUseCase(
    private val repository: QuestionRepository,
    private val questionSetSelectionRepository: QuestionSetSelectionRepository
) {
    suspend operator fun invoke(): Result<List<Question>> =
        questionSetSelectionRepository.selectedQuestionSetId.value?.let { questionSetId ->
            repository.getMockTestQuestions(questionSetId)
        } ?: repository.getMockTestQuestions()
}
