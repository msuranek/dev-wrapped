package com.devwrapped.render

import com.devwrapped.core.Archetype
import com.devwrapped.core.DeveloperProfile
import com.devwrapped.core.GitIdentity
import com.devwrapped.core.LanguageStat
import com.devwrapped.core.PersonalityResult
import com.devwrapped.core.ReportingPeriod
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.time.DayOfWeek
import java.nio.file.Path
import javax.imageio.ImageIO
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
}
