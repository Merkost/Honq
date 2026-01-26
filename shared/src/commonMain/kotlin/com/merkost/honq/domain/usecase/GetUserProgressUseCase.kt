package com.merkost.honq.domain.usecase

import com.merkost.honq.domain.model.UserProgress
import com.merkost.honq.domain.repository.ProgressRepository
import kotlinx.coroutines.flow.Flow

class GetUserProgressUseCase(
    private val repository: ProgressRepository
) {
    operator fun invoke(): Flow<UserProgress> =
        repository.observeUserProgress()
}
