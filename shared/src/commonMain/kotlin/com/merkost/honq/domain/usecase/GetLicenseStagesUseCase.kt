package com.merkost.honq.domain.usecase

import com.merkost.honq.core.util.Result
import com.merkost.honq.domain.model.LicenseStage
import com.merkost.honq.domain.repository.QuestionRepository

class GetLicenseStagesUseCase(
    private val repository: QuestionRepository
) {
    suspend operator fun invoke(): Result<List<LicenseStage>> =
        repository.getLicenseStages()
}
