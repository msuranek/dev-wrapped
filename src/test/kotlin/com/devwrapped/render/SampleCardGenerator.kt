package com.devwrapped.render

import com.devwrapped.core.Archetype
import com.devwrapped.core.DeveloperProfile
import com.devwrapped.core.GitIdentity
import com.devwrapped.core.LanguageStat
import com.devwrapped.core.PersonalityResult
import com.devwrapped.core.ReportingPeriod
import java.nio.file.Path
import java.time.DayOfWeek

object SampleCardGenerator {
    @JvmStatic fun main(args: Array<String>) {
        CardRenderer.writePng(
            CardRenderer.render(fictionalSampleProfile(), PersonalityResult(Archetype.MIDNIGHT_ARCHITECT, "Sample profile")),
            Path.of(requireNotNull(args.firstOrNull()) { "Output path is required." }),
        )
    }

    /** Fictional values used only for documentation and visual QA. */
    internal fun fictionalSampleProfile() = DeveloperProfile(
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
