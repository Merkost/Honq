package com.merkost.honq.domain.usecase

import com.merkost.honq.core.util.Result
import com.merkost.honq.domain.model.Question
import com.merkost.honq.domain.repository.QuestionRepository

class GetRandomQuestionsUseCase(
    private val repository: QuestionRepository
) {
    suspend operator fun invoke(count: Int): Result<List<Question>> =
        repository.getRandomQuestions(count)
}
