package com.merkost.honq.data.remote.api

class HostedImageUrlBuilder(baseUrl: String) {

    private val base: String = baseUrl.trimEnd('/')

    fun buildUrl(relativePath: String?): String? {
        if (relativePath.isNullOrBlank()) return null
        if (relativePath.startsWith("http://") || relativePath.startsWith("https://")) {
            return relativePath
        }
        return "$base/${relativePath.trimStart('/')}"
    }
}
