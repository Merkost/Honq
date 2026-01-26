package com.merkost.honq.domain.model

enum class LicenseTypeId(val id: String) {
    CAR("car"),
    RIDER("rider");

    companion object {
        fun fromId(id: String): LicenseTypeId? = entries.find { it.id == id }
    }
}

data class LicenseType(
    val id: String,
    val name: String,
    val shortName: String,
    val isActive: Boolean = true,
    val displayOrder: Int = 0
) {
    val typeId: LicenseTypeId? get() = LicenseTypeId.fromId(id)
}
