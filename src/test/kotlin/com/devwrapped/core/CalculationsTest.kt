package com.devwrapped.core

import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.test.assertEquals

class CalculationsTest {
    @Test fun `streak calculation ignores duplicates and finds longest run`() {
        val days = listOf("2025-01-01", "2025-01-02", "2025-01-02", "2025-01-04", "2025-01-05", "2025-01-06").map(LocalDate::parse)
        assertEquals(3, GitAnalyzer.longestStreak(days))
        assertEquals(0, GitAnalyzer.longestStreak(emptyList()))
    }

    @Test fun `generated and binary paths are excluded`() {
        assertEquals(null, LanguageDetector.detect("node_modules/pkg/index.js"))
        assertEquals(null, LanguageDetector.detect("public/app.min.js"))
        assertEquals(null, LanguageDetector.detect("assets/logo.png"))
        assertEquals("TypeScript", LanguageDetector.detect("src/app.ts"))
    }
}
