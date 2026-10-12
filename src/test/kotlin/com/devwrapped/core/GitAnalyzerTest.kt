package com.devwrapped.core

import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant
import java.time.ZoneId
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GitAnalyzerTest {
    @TempDir lateinit var repository: Path
    private val analyzer = GitAnalyzer()

    @Test fun `empty repository returns no identities`() {
        initRepo()
        assertEquals(emptyList(), analyzer.discoverIdentities(repository))
    }

    @Test fun `filters exact identities and periods`() {
        initRepo()
        commit("src/Old.kt", "old", "old", "Alex", "alex@one.test", "2023-01-02T10:00:00+00:00")
        commit("src/Main.kt", "one", "Initial", "Alex", "alex@one.test", "2025-01-02T10:00:00+00:00")
        commit("src/Fix.kt", "two", "fix: issue", "Alex", "alex@two.test", "2025-01-03T22:00:00+00:00")
        val identities = analyzer.discoverIdentities(repository)
        assertEquals(2, identities.size)
        val profile = analyzer.analyze(AnalysisRequest(
            repository, GitIdentity("Alex", "alex@one.test"), ReportingPeriod.CURRENT_YEAR,
            Instant.parse("2025-06-01T00:00:00Z"), ZoneId.of("UTC")
        ))
        assertEquals(1, profile.totalCommits)
        assertEquals("Kotlin", profile.primaryLanguage)
        assertEquals(0, profile.fixCommits)
    }

    @Test fun `uses reporting timezone for dates hours and streak`() {
        initRepo()
        commit("a.py", "1", "fix bug", "Dev", "dev@test", "2025-01-01T23:30:00+00:00")
        commit("b.py", "2", "fixed parser", "Dev", "dev@test", "2025-01-02T23:30:00+00:00")
        val profile = analyzer.analyze(AnalysisRequest(
            repository, GitIdentity("Dev", "dev@test"), ReportingPeriod.ALL_TIME,
            Instant.parse("2025-01-10T00:00:00Z"), ZoneId.of("Europe/Prague")
        ))
        assertEquals(2, profile.activeDays)
        assertEquals(2, profile.longestStreakDays)
        assertEquals(0, profile.mostActiveHour)
        assertEquals(100, profile.lateNightPercentage)
        assertEquals(2, profile.fixCommits)
    }

    @Test fun `missing language data is represented as null`() {
        initRepo()
        commit("README.md", "docs", "docs", "Dev", "dev@test", "2025-01-01T12:00:00+00:00")
        val profile = analyzer.analyze(AnalysisRequest(repository, GitIdentity("Dev", "dev@test"), ReportingPeriod.ALL_TIME, zone = ZoneId.of("UTC")))
        assertNull(profile.primaryLanguage)
    }

    @Test fun `identity commit counts honor the selected reporting period`() {
        initRepo()
        commit("old.kt", "old", "old", "Dev", "dev@test", "2025-01-01T12:00:00+00:00")
        commit("recent.kt", "recent", "recent", "Dev", "dev@test", "2025-03-20T12:00:00+00:00")
        commit("other.kt", "other", "other", "Other", "other@test", "2025-03-21T12:00:00+00:00")
        val now = Instant.parse("2025-04-01T00:00:00Z")

        val recent = analyzer.discoverIdentities(repository, ReportingPeriod.LAST_30_DAYS, now, ZoneId.of("UTC"))
        val allTime = analyzer.discoverIdentities(repository, ReportingPeriod.ALL_TIME, now, ZoneId.of("UTC"))

        assertEquals(1, recent.single { it.email == "dev@test" }.commitCount)
        assertEquals(2, allTime.single { it.email == "dev@test" }.commitCount)
        assertEquals("other@test", recent.first().email)
    }

    @Test fun `reads configured repository identity`() {
        initRepo()
        git("config", "user.name", "Configured Dev")
        git("config", "user.email", "configured@test")
        assertEquals(GitIdentity("Configured Dev", "configured@test"), analyzer.configuredIdentity(repository))
    }

    private fun initRepo() {
        assumeTrue(runCatching { ProcessBuilder("git", "--version").start().waitFor() == 0 }.getOrDefault(false))
        git("init", "-q")
        git("config", "commit.gpgsign", "false")
    }

    private fun commit(path: String, content: String, message: String, name: String, email: String, date: String) {
        val file = repository.resolve(path)
        Files.createDirectories(file.parent)
        Files.writeString(file, content)
        git("add", path)
        val process = ProcessBuilder("git", "-C", repository.toString(), "commit", "-q", "-m", message)
            .apply { environment().putAll(mapOf("GIT_AUTHOR_NAME" to name, "GIT_AUTHOR_EMAIL" to email, "GIT_AUTHOR_DATE" to date, "GIT_COMMITTER_NAME" to name, "GIT_COMMITTER_EMAIL" to email, "GIT_COMMITTER_DATE" to date)) }
            .redirectErrorStream(true).start()
        val output = process.inputStream.bufferedReader().readText()
        assertEquals(0, process.waitFor(), output)
    }

    private fun git(vararg args: String) {
        val process = ProcessBuilder(listOf("git", "-C", repository.toString()) + args).redirectErrorStream(true).start()
        val output = process.inputStream.bufferedReader().readText()
        assertEquals(0, process.waitFor(), output)
    }
}
