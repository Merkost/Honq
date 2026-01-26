package com.merkost.honq.domain.usecase

import com.merkost.honq.core.util.Result
import com.merkost.honq.domain.repository.QuestionRepository
import com.merkost.honq.domain.repository.QuestionSetSelectionRepository

class SyncQuestionsUseCase(
    private val repository: QuestionRepository,
    private val questionSetSelectionRepository: QuestionSetSelectionRepository
) {
    suspend operator fun invoke(): Result<Unit> =
        questionSetSelectionRepository.selectedQuestionSetId.value?.let { questionSetId ->
            repository.syncQuestions(questionSetId)
        } ?: repository.syncQuestions()
}
