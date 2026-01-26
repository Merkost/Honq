package com.merkost.honq.domain.model

data class Category(
    val id: String,
    val name: String,
    val description: String = "",
    val iconName: String = "",
    val displayOrder: Int = 0,
    val isActive: Boolean = true
)
