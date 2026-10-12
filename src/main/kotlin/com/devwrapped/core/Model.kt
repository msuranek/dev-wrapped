package com.devwrapped.core

import java.nio.file.Path
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class GitIdentity(
    val name: String,
    val email: String,
    val commitCount: Int = 0,
) {
    val displayName: String get() = "$name <$email> — $commitCount ${if (commitCount == 1) "commit" else "commits"}"
}

enum class ReportingPeriod(val displayName: String) {
    LAST_30_DAYS("Last 30 days"),
    CURRENT_YEAR("This year"),
    ALL_TIME("All time");

    fun start(now: Instant, zone: ZoneId): Instant? = when (this) {
        LAST_30_DAYS -> now.minusSeconds(30L * 24 * 60 * 60)
        CURRENT_YEAR -> LocalDate.of(now.atZone(zone).year, 1, 1).atStartOfDay(zone).toInstant()
        ALL_TIME -> null
    }
}

data class LanguageStat(val language: String, val changedFiles: Int)

data class DeveloperProfile(
    val identity: GitIdentity,
    val period: ReportingPeriod,
    val periodLabel: String,
    val totalCommits: Int,
    val mostActiveDay: DayOfWeek?,
    val lateNightPercentage: Int,
    val mostActiveHour: Int?,
    val activeDays: Int,
    val longestStreakDays: Int,
    val fixCommits: Int,
    val weekendPercentage: Int,
    val languages: List<LanguageStat>,
    val firstActivityDate: LocalDate?,
    val lastActivityDate: LocalDate?,
) {
    val primaryLanguage: String? get() = languages.firstOrNull()?.language
    val commitsPerActiveDay: Double get() = if (activeDays == 0) 0.0 else totalCommits.toDouble() / activeDays
    val fixPercentage: Int get() = if (totalCommits == 0) 0 else ((fixCommits * 100.0) / totalCommits).toInt()
}

data class AnalysisRequest(
    val repository: Path,
    val identity: GitIdentity,
    val period: ReportingPeriod,
    val now: Instant = Instant.now(),
    val zone: ZoneId = ZoneId.systemDefault(),
)

class GitAnalysisException(message: String, cause: Throwable? = null) : Exception(message, cause)
