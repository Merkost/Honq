package com.merkost.honq.domain.usecase

import com.merkost.honq.domain.repository.ReviewRepository

class HasIncorrectAnswersUseCase(
    private val reviewRepository: ReviewRepository
) {
    suspend operator fun invoke(): Boolean =
        reviewRepository.hasIncorrectAnswers()
}
