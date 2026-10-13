package com.devwrapped.render

import com.devwrapped.core.Archetype
import com.devwrapped.core.DeveloperProfile
import com.devwrapped.core.GitIdentity
import com.devwrapped.core.LanguageStat
import com.devwrapped.core.PersonalityResult
import com.devwrapped.core.ReportingPeriod
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.awt.Font
import java.awt.image.DataBufferInt
import java.time.DayOfWeek
import java.nio.file.Path
import javax.imageio.ImageIO
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CardRendererTest {
    @TempDir lateinit var output: Path

    @Test fun `renders and writes a decodable high resolution PNG`() {
        val profile = DeveloperProfile(
            GitIdentity("Private", "private@example.test"), ReportingPeriod.CURRENT_YEAR, "2025", 1247,
            DayOfWeek.FRIDAY, 42, 23, 188, 17, 201, 12,
            listOf(LanguageStat("PHP", 900)), null, null
        )
        val image = CardRenderer.render(profile, PersonalityResult(Archetype.MIDNIGHT_ARCHITECT, "test"))
        assertEquals(1200, image.width)
        assertEquals(1500, image.height)
        val file = output.resolve("card.png")
        CardRenderer.writePng(image, file)
        val decoded = ImageIO.read(file.toFile())
        assertEquals(1200, decoded.width)
        assertEquals(1500, decoded.height)
        assertTrue(file.toFile().length() > 10_000)
    }

    @Test fun `renders every archetype at export resolution`() {
        Archetype.entries.forEach { archetype ->
            val image = CardRenderer.render(profile(), PersonalityResult(archetype, "test"))
            assertEquals(CardRenderer.WIDTH, image.width, archetype.name)
            assertEquals(CardRenderer.HEIGHT, image.height, archetype.name)
            assertTrue(image.getRGB(0, 0) ushr 24 != 0, "${archetype.name} background must be opaque")
        }
    }

    @Test fun `renders the low signal state`() {
        val image = CardRenderer.render(
            profile().copy(
                totalCommits = 0,
                mostActiveDay = null,
                lateNightPercentage = 0,
                mostActiveHour = null,
                activeDays = 0,
                longestStreakDays = 0,
                fixCommits = 0,
                weekendPercentage = 0,
                languages = emptyList(),
            ),
            PersonalityResult(null, "At least 5 commits are needed for a meaningful Developer DNA result."),
        )

        assertEquals(CardRenderer.WIDTH, image.width)
        assertEquals(CardRenderer.HEIGHT, image.height)
    }

    @Test fun `long dynamic text is constrained to the card layout`() {
        val image = CardRenderer.render(
            profile().copy(
                periodLabel = "An intentionally long reporting period label",
                languages = listOf(LanguageStat("An Extremely Long Programming Language Name", 760)),
            ),
            PersonalityResult(Archetype.MIDNIGHT_ARCHITECT, "test"),
        )
        val graphics = image.createGraphics()
        try {
            val metrics = graphics.getFontMetrics(Font("SansSerif", Font.BOLD, 22))
            val fitted = CardRenderer.ellipsize("An Extremely Long Programming Language Name", metrics, 250)
            assertTrue(fitted.endsWith("…"))
            assertTrue(metrics.stringWidth(fitted) <= 250)
        } finally {
            graphics.dispose()
        }
        assertEquals(CardRenderer.WIDTH, image.width)
        assertEquals(CardRenderer.HEIGHT, image.height)
    }

    @Test fun `rendering is deterministic for the same profile`() {
        val personality = PersonalityResult(Archetype.POLYGLOT, "test")
        val first = CardRenderer.render(profile(), personality).raster.dataBuffer as DataBufferInt
        val second = CardRenderer.render(profile(), personality).raster.dataBuffer as DataBufferInt

        assertContentEquals(first.data, second.data)
    }

    private fun profile() = DeveloperProfile(
        GitIdentity("Sample", "sample@example.test"), ReportingPeriod.CURRENT_YEAR, "2026", 842,
        DayOfWeek.THURSDAY, 46, 23, 163, 21, 96, 17,
        listOf(LanguageStat("Kotlin", 760), LanguageStat("TypeScript", 320)), null, null,
    )
}
