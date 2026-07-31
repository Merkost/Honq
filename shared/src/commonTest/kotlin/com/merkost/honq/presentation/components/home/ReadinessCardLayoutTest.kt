package com.merkost.honq.presentation.components.home

import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals

class ReadinessCardLayoutTest {

    @Test
    fun stacks_header_when_content_is_narrow() {
        assertEquals(
            ReadinessHeaderLayout.Stacked,
            readinessHeaderLayout(268.dp)
        )
    }

    @Test
    fun keeps_header_inline_when_content_has_room() {
        assertEquals(
            ReadinessHeaderLayout.Inline,
            readinessHeaderLayout(320.dp)
        )
    }
}
