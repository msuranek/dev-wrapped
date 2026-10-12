package com.devwrapped.core

import org.junit.jupiter.api.Test
import java.time.DayOfWeek
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PersonalityClassifierTest {
    @Test fun `low activity does not invent an archetype`() {
        val result = PersonalityClassifier.classify(profile(commits = 4))
        assertNull(result.archetype)
    }

    @Test fun `midnight classification is deterministic`() {
        val result = PersonalityClassifier.classify(profile(commits = 20, late = 70, weekend = 40, fixes = 2))
        assertEquals(Archetype.MIDNIGHT_ARCHITECT, result.archetype)
    }

    @Test fun `bug whisperer threshold is inclusive`() {
        val result = PersonalityClassifier.classify(profile(commits = 10, fixes = 3))
        assertEquals(Archetype.BUG_WHISPERER, result.archetype)
    }

    @Test fun `polyglot requires meaningful language evidence`() {
        val langs = listOf(LanguageStat("Kotlin", 4), LanguageStat("Python", 3), LanguageStat("TypeScript", 7))
        assertEquals(Archetype.POLYGLOT, PersonalityClassifier.classify(profile(commits = 12, languages = langs)).archetype)
    }

    @Test fun `balanced profile falls back to steady builder`() {
        assertEquals(Archetype.STEADY_BUILDER, PersonalityClassifier.classify(profile(commits = 8)).archetype)
    }

    private fun profile(commits: Int, late: Int = 0, weekend: Int = 0, fixes: Int = 0, languages: List<LanguageStat> = emptyList()) = DeveloperProfile(
        GitIdentity("Test", "test@example.test"), ReportingPeriod.ALL_TIME, "All time", commits,
        DayOfWeek.TUESDAY, late, 10, minOf(commits, 5), 2, fixes, weekend, languages, null, null
    )
}
