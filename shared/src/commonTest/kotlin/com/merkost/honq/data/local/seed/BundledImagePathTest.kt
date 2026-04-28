package com.merkost.honq.data.local.seed

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class BundledImagePathTest {
    @Test
    fun resolve_null_returns_null() {
        assertNull(BundledImagePath.resolve(null))
    }

    @Test
    fun resolve_blank_returns_null() {
        assertNull(BundledImagePath.resolve(""))
        assertNull(BundledImagePath.resolve("   "))
    }

    @Test
    fun resolve_returns_canonical_resource_path() {
        val path = BundledImagePath.resolve("questions/nt/CSB002.png")
        assertEquals("files/content/v1/questions/nt/CSB002.png", path)
    }

    @Test
    fun resolve_trims_leading_slash() {
        val path = BundledImagePath.resolve("/questions/nt/CSB002.png")
        assertEquals("files/content/v1/questions/nt/CSB002.png", path)
    }
}
