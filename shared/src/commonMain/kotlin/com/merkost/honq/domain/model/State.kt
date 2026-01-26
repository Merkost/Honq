package com.merkost.honq.domain.model

data class State(
    val id: String,
    val name: String,
    val shortName: String,
    val externalPracticeUrl: String? = null,
    val handbookUrl: String? = null,
    val isActive: Boolean = true
) {
    val isExternalOnly: Boolean get() = !externalPracticeUrl.isNullOrBlank()
}
