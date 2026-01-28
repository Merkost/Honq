package com.merkost.honq.domain.usecase

import com.merkost.honq.domain.model.WeakQuestion
import com.merkost.honq.domain.repository.ProgressRepository

class GetWeakestQuestionsUseCase(
    private val progressRepository: ProgressRepository
) {
    suspend operator fun invoke(limit: Int = 50): List<WeakQuestion> =
        progressRepository.getWeakestQuestions(limit)
}
