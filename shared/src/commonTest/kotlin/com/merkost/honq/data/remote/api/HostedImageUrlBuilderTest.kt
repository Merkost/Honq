package com.merkost.honq.data.remote.api

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class HostedImageUrlBuilderTest {

    private val builder = HostedImageUrlBuilder(baseUrl = "https://honq-ac8e4.web.app")

    @Test
    fun returns_null_for_null_input() {
        assertNull(builder.buildUrl(null))
    }

    @Test
    fun returns_null_for_blank_input() {
        assertNull(builder.buildUrl(""))
        assertNull(builder.buildUrl("   "))
    }

    @Test
    fun passes_through_absolute_https_url() {
        val abs = "https://example.com/img.png"
        assertEquals(abs, builder.buildUrl(abs))
    }

    @Test
    fun passes_through_absolute_http_url() {
        val abs = "http://example.com/img.png"
        assertEquals(abs, builder.buildUrl(abs))
    }

    @Test
    fun builds_url_for_simple_path() {
        assertEquals(
            "https://honq-ac8e4.web.app/questions/nsw-001.png",
            builder.buildUrl("questions/nsw-001.png")
        )
    }

    @Test
    fun strips_leading_slash_from_path() {
        assertEquals(
            "https://honq-ac8e4.web.app/questions/nsw-001.png",
            builder.buildUrl("/questions/nsw-001.png")
        )
    }

    @Test
    fun strips_trailing_slash_from_base_url() {
        val builderWithSlash = HostedImageUrlBuilder(baseUrl = "https://honq-ac8e4.web.app/")
        assertEquals(
            "https://honq-ac8e4.web.app/questions/nsw-001.png",
            builderWithSlash.buildUrl("questions/nsw-001.png")
        )
    }
}
