package com.merkost.honq.domain.model

enum class Difficulty(val value: Int) {
    EASY(1),
    MEDIUM(2),
    HARD(3);

    companion object {
        fun fromValue(value: Int): Difficulty = entries.find { it.value == value } ?: MEDIUM
    }
}
