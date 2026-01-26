package com.merkost.honq.data.local

import com.merkost.honq.domain.repository.QuestionSetSelectionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class InMemoryQuestionSetSelectionRepository : QuestionSetSelectionRepository {
    private val selectedQuestionSet = MutableStateFlow<String?>(null)

    override val selectedQuestionSetId: StateFlow<String?> = selectedQuestionSet

    override fun setSelectedQuestionSetId(questionSetId: String) {
        selectedQuestionSet.value = questionSetId
    }
}
