package com.merkost.honq.domain.usecase

import com.merkost.honq.core.util.Result
import com.merkost.honq.domain.model.LicenseType
import com.merkost.honq.domain.repository.QuestionRepository

class GetLicenseTypesUseCase(
    private val repository: QuestionRepository
) {
    suspend operator fun invoke(): Result<List<LicenseType>> =
        repository.getLicenseTypes()
}
