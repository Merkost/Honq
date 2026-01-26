package com.merkost.honq.domain.usecase

import com.merkost.honq.core.util.getOrNull
import com.merkost.honq.domain.repository.QuestionRepository
import com.merkost.honq.domain.repository.QuestionSetSelectionRepository
import com.merkost.honq.domain.repository.StateSelectionRepository

class SetSelectedStateUseCase(
    private val stateSelectionRepository: StateSelectionRepository,
    private val questionSetSelectionRepository: QuestionSetSelectionRepository,
    private val questionRepository: QuestionRepository
) {
    suspend operator fun invoke(stateId: String) {
        stateSelectionRepository.setSelectedStateId(stateId)

        val questionSets = questionRepository.getQuestionSetsByState(stateId)
        val defaultQuestionSetId = questionSets
            .getOrNull()
            ?.let { sets ->
                sets.firstOrNull {
                    it.isActive &&
                        it.licenseTypeId == DEFAULT_LICENSE_TYPE_ID &&
                        it.licenseStageId == DEFAULT_LICENSE_STAGE_ID &&
                        it.assessmentTypeId == DEFAULT_ASSESSMENT_TYPE_ID
                } ?: sets.firstOrNull { it.isActive }
            }
            ?.id

        if (defaultQuestionSetId != null) {
            questionSetSelectionRepository.setSelectedQuestionSetId(defaultQuestionSetId)
        }
    }

    companion object {
        private const val DEFAULT_LICENSE_TYPE_ID = "car"
        private const val DEFAULT_LICENSE_STAGE_ID = "learner"
        private const val DEFAULT_ASSESSMENT_TYPE_ID = "knowledge_test"
    }
}
