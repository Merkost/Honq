package com.merkost.honq.domain.usecase

import com.merkost.honq.domain.model.IncorrectAnswer
import com.merkost.honq.domain.repository.ReviewRepository

class SaveIncorrectAnswersUseCase(
    private val reviewRepository: ReviewRepository
) {
    suspend operator fun invoke(answers: List<IncorrectAnswer>) {
        reviewRepository.saveIncorrectAnswers(answers)
    }
}
