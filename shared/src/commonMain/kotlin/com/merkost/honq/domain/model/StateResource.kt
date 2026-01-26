package com.merkost.honq.domain.model

data class StateResource(
    val id: String,
    val stateId: String,
    val title: String,
    val url: String,
    val resourceType: ResourceType,
    val licenseType: String? = null,
    val displayOrder: Int = 0
)

enum class ResourceType {
    PRACTICE_TEST,
    PDF,
    HANDBOOK,
    OTHER
}
