package com.merkost.honq.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class CategoryScore(
    val categoryId: String,
    val categoryName: String,
    val correct: Int,
    val total: Int
) {
    val percentage: Int get() = if (total > 0) ((correct.toFloat() / total) * 100).toInt() else 0
}
