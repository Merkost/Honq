package com.merkost.honq.domain.usecase

import com.merkost.honq.domain.model.Question
import com.merkost.honq.domain.repository.ProgressRepository

class GetSmartPracticeQuestionsUseCase(
    private val progressRepository: ProgressRepository
) {
    suspend operator fun invoke(count: Int): List<Question> =
        progressRepository.getSmartPracticeQuestions(count)
}
