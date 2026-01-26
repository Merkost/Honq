package com.merkost.honq.domain.usecase

import com.merkost.honq.domain.model.IncorrectAnswer
import com.merkost.honq.domain.repository.ReviewRepository
import kotlinx.coroutines.flow.Flow

class ObserveIncorrectAnswersUseCase(
    private val reviewRepository: ReviewRepository
) {
    operator fun invoke(): Flow<List<IncorrectAnswer>> =
        reviewRepository.observeIncorrectAnswers()
}
