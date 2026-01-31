package com.merkost.honq.domain.repository

import kotlinx.coroutines.flow.StateFlow

interface QuestionSetSelectionRepository {
    val selectedQuestionSetId: StateFlow<String?>
    fun setSelectedQuestionSetId(questionSetId: String?)
}
