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
        val profile = DeveloperProfile(
            identity = GitIdentity("Sample", "sample@example.test"),
            period = ReportingPeriod.CURRENT_YEAR,
            periodLabel = "2026",
            totalCommits = 1247,
            mostActiveDay = DayOfWeek.FRIDAY,
            lateNightPercentage = 42,
            mostActiveHour = 23,
            activeDays = 188,
            longestStreakDays = 17,
            fixCommits = 201,
            weekendPercentage = 18,
            languages = listOf(LanguageStat("PHP", 913), LanguageStat("TypeScript", 404)),
            firstActivityDate = null,
            lastActivityDate = null,
        )
        CardRenderer.writePng(
            CardRenderer.render(profile, PersonalityResult(Archetype.MIDNIGHT_ARCHITECT, "Sample profile")),
            Path.of(requireNotNull(args.firstOrNull()) { "Output path is required." }),
        )
    }
}
