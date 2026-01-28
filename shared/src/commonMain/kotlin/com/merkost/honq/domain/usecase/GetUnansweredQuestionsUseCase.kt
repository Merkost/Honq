package com.merkost.honq.domain.usecase

import com.merkost.honq.domain.model.Question
import com.merkost.honq.domain.repository.ProgressRepository

class GetUnansweredQuestionsUseCase(
    private val progressRepository: ProgressRepository
) {
    suspend operator fun invoke(limit: Int = 50): List<Question> =
        progressRepository.getUnansweredQuestions(limit)
}
