package com.merkost.honq.data.local.seed

object BundledImagePath {
    private const val ROOT = "files/content/v1/"

    /**
     * Returns a Compose-resource path suitable for Coil + Compose Multiplatform's
     * resource loader. Returns `null` for null/blank inputs so callers can pass
     * directly into image components without extra null-checks.
     */
    fun resolve(relativePath: String?): String? {
        if (relativePath.isNullOrBlank()) return null
        val trimmed = relativePath.trim().trimStart('/')
        return "$ROOT$trimmed"
    }
}
