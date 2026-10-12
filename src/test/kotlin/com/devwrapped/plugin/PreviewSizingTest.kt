package com.devwrapped.plugin

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class PreviewSizingTest {
    @Test fun `fits portrait card by height while preserving aspect ratio`() {
        val size = PreviewSizing.fit(1200, 1500, 600, 700)
        assertEquals(560, size.width)
        assertEquals(700, size.height)
    }

    @Test fun `fits portrait card by width while preserving aspect ratio`() {
        val size = PreviewSizing.fit(1200, 1500, 500, 1000)
        assertEquals(500, size.width)
        assertEquals(625, size.height)
    }

    @Test fun `does not upscale beyond the rendered card resolution`() {
        val size = PreviewSizing.fit(1200, 1500, 2400, 3000)
        assertEquals(1200, size.width)
        assertEquals(1500, size.height)
    }
}
