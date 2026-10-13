package com.devwrapped.render

import com.devwrapped.core.Archetype
import com.devwrapped.core.DeveloperProfile
import com.devwrapped.core.GitIdentity
import com.devwrapped.core.LanguageStat
import com.devwrapped.core.ReportingPeriod
import org.junit.jupiter.api.Test
import java.awt.Font
import java.awt.image.BufferedImage
import java.time.DayOfWeek
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class CardAchievementsTest {
    @Test fun `every archetype has deterministic copy distinct from its main subtitle`() {
        val profile = profile()
        val achievements = Archetype.entries.map { CardAchievements.forProfile(profile, it) }

        assertEquals(Archetype.entries.size, achievements.map { it.title }.distinct().size)
        achievements.forEachIndexed { index, achievement ->
            val archetype = Archetype.entries[index]
            assertTrue(achievement.title.isNotBlank(), archetype.name)
            assertTrue(achievement.description.isNotBlank(), archetype.name)
            assertNotEquals(archetype.subtitle, achievement.description, archetype.name)
            assertEquals(achievement, CardAchievements.forProfile(profile, archetype))
        }
    }

    @Test fun `achievement descriptions use calculated profile statistics`() {
        val profile = profile()

        assertEquals(
            "46% of your commits landed after dark. Your keyboard clearly has a night shift.",
            CardAchievements.forProfile(profile, Archetype.MIDNIGHT_ARCHITECT).description,
        )
        assertEquals(
            "17% of your commits landed on weekends. Git does not own a day planner.",
            CardAchievements.forProfile(profile, Archetype.WEEKEND_WARRIOR).description,
        )
        assertEquals(
            "96 commits (11%) mentioned fixes. Your history came with patch notes.",
            CardAchievements.forProfile(profile, Archetype.BUG_WHISPERER).description,
        )
        assertEquals(
            "5.2 commits per active day. Your Git graph found a steady beat.",
            CardAchievements.forProfile(profile, Archetype.COMMIT_MACHINE).description,
        )
        assertEquals(
            "3 languages appeared in the diff. Your syntax highlighter had range.",
            CardAchievements.forProfile(profile, Archetype.POLYGLOT).description,
        )
        assertEquals(
            "163 active days with a 21-day streak. Your commit trail kept its cadence.",
            CardAchievements.forProfile(profile, Archetype.STEADY_BUILDER).description,
        )
    }

    @Test fun `zero and missing statistics receive concise fallbacks`() {
        val empty = profile().copy(
            totalCommits = 0,
            lateNightPercentage = 0,
            activeDays = 0,
            longestStreakDays = 0,
            fixCommits = 0,
            weekendPercentage = 0,
            languages = emptyList(),
        )

        (Archetype.entries.map { CardAchievements.forProfile(empty, it) } + CardAchievements.forProfile(empty, null))
            .forEach { achievement ->
                assertTrue(achievement.description.isNotBlank())
                assertTrue(achievement.description.length <= 100, achievement.description)
            }
        assertTrue(CardAchievements.forProfile(empty, null).description.startsWith("No commits"))
    }

    @Test fun `descriptions fit the existing two line achievement area`() {
        val image = BufferedImage(1200, 1500, BufferedImage.TYPE_INT_ARGB)
        val graphics = image.createGraphics()
        try {
            graphics.font = Font("SansSerif", Font.PLAIN, 21)
            Archetype.entries.forEach { archetype ->
                val description = CardAchievements.forProfile(profile(), archetype).description
                val lines = CardRenderer.wrap(description, graphics.fontMetrics, 800, 2)
                assertTrue(lines.size <= 2, archetype.name)
                assertTrue(lines.all { graphics.fontMetrics.stringWidth(it) <= 800 }, archetype.name)
            }
        } finally {
            graphics.dispose()
        }
    }

    private fun profile() = DeveloperProfile(
        identity = GitIdentity("Sample", "sample@example.test"),
        period = ReportingPeriod.CURRENT_YEAR,
        periodLabel = "2026",
        totalCommits = 842,
        mostActiveDay = DayOfWeek.THURSDAY,
        lateNightPercentage = 46,
        mostActiveHour = 23,
        activeDays = 163,
        longestStreakDays = 21,
        fixCommits = 96,
        weekendPercentage = 17,
        languages = listOf(
            LanguageStat("Kotlin", 760),
            LanguageStat("TypeScript", 320),
            LanguageStat("Python", 90),
        ),
        firstActivityDate = null,
        lastActivityDate = null,
    )
}
