package com.devwrapped.render

import com.devwrapped.core.Archetype
import com.devwrapped.core.PersonalityResult
import java.nio.file.Files
import java.nio.file.Path

/** Writes ignored, temporary cards for visual QA; it never changes the README asset. */
object ArchetypeQaGenerator {
    @JvmStatic fun main(args: Array<String>) {
        val output = Path.of(requireNotNull(args.firstOrNull()) { "Output directory is required." })
        Files.createDirectories(output)
        val profile = SampleCardGenerator.fictionalSampleProfile()
        Archetype.entries.forEach { archetype ->
            CardRenderer.writePng(
                CardRenderer.render(profile, PersonalityResult(archetype, "Visual QA")),
                output.resolve("${archetype.name.lowercase()}.png"),
            )
        }
        CardRenderer.writePng(
            CardRenderer.render(
                profile.copy(
                    totalCommits = 2,
                    mostActiveDay = null,
                    lateNightPercentage = 0,
                    mostActiveHour = null,
                    activeDays = 2,
                    longestStreakDays = 1,
                    fixCommits = 0,
                    weekendPercentage = 0,
                    languages = emptyList(),
                ),
                PersonalityResult(null, "At least 5 commits are needed for a meaningful Developer DNA result."),
            ),
            output.resolve("low-signal.png"),
        )
    }
}
