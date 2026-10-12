package com.devwrapped.core

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import java.util.concurrent.CompletableFuture
import kotlin.math.roundToInt

fun interface CancellationCheck { fun checkCancelled() }

class GitAnalyzer(private val gitExecutable: String = "git") {
    fun validateRepository(repository: Path) {
        require(Files.isDirectory(repository)) { "Repository directory does not exist." }
        val result = runGit(repository, listOf("rev-parse", "--is-inside-work-tree"))
        if (result.trim() != "true") throw GitAnalysisException("The selected directory is not a Git working tree.")
    }

    fun discoverIdentities(
        repository: Path,
        period: ReportingPeriod = ReportingPeriod.ALL_TIME,
        now: Instant = Instant.now(),
        zone: ZoneId = ZoneId.systemDefault(),
        cancellation: CancellationCheck = CancellationCheck {},
    ): List<GitIdentity> {
        validateRepository(repository)
        val args = mutableListOf("log", "--all", "--no-merges", "--format=%aN%x1f%aE%x1f%aI")
        period.start(now, zone)?.let { args += "--since=${DateTimeFormatter.ISO_INSTANT.format(it)}" }
        val output = runGit(repository, args, cancellation)
        if (output.isBlank()) return emptyList()
        val commits = output.lineSequence().mapNotNull { line ->
            cancellation.checkCancelled()
            val fields = line.split('\u001f', limit = 3)
            val instant = fields.getOrNull(2)?.let { runCatching { OffsetDateTime.parse(it).toInstant() }.getOrNull() }
            if (fields.size != 3 || fields[0].isBlank() || fields[1].isBlank() || instant == null) null
            else IdentityCommit(fields[0], fields[1], instant)
        }.toList()
        return commits.groupBy { it.name to it.email }.map { (identity, activity) ->
            IdentityActivity(identity.first, identity.second, activity.size, activity.maxOf { it.instant })
        }.sortedWith(
            compareByDescending<IdentityActivity> { it.lastActiveAt }
                .thenByDescending { it.commitCount }
                .thenBy { it.name.lowercase() }
                .thenBy { it.email.lowercase() }
        ).map { GitIdentity(it.name, it.email, it.commitCount) }
    }

    fun configuredIdentity(repository: Path): GitIdentity? {
        validateRepository(repository)
        val name = runCatching { runGit(repository, listOf("config", "--get", "user.name")).trim() }.getOrNull()
        val email = runCatching { runGit(repository, listOf("config", "--get", "user.email")).trim() }.getOrNull()
        return if (name.isNullOrBlank() || email.isNullOrBlank()) null else GitIdentity(name, email)
    }

    fun analyze(request: AnalysisRequest, cancellation: CancellationCheck = CancellationCheck {}): DeveloperProfile {
        validateRepository(request.repository)
        val args = mutableListOf("log", "--all", "--no-merges", "--format=%x1e%H%x1f%aN%x1f%aE%x1f%aI%x1f%s", "--name-only")
        request.period.start(request.now, request.zone)?.let { args += "--since=${DateTimeFormatter.ISO_INSTANT.format(it)}" }
        val commits = parseCommits(runGit(request.repository, args, cancellation), cancellation)
            .filter { it.authorName == request.identity.name && it.authorEmail == request.identity.email }
        return aggregate(request, commits)
    }

    private fun aggregate(request: AnalysisRequest, commits: List<CommitRecord>): DeveloperProfile {
        val dates = commits.map { it.instant.atZone(request.zone).toLocalDate() }
        val dayCounts = dates.groupingBy { it.dayOfWeek }.eachCount()
        val hourCounts = commits.map { it.instant.atZone(request.zone).hour }.groupingBy { it }.eachCount()
        val late = hourCounts.filterKeys { it >= 20 || it < 5 }.values.sum()
        val weekend = dayCounts.filterKeys { it == DayOfWeek.SATURDAY || it == DayOfWeek.SUNDAY }.values.sum()
        val languages = commits.asSequence().flatMap { it.files.asSequence() }
            .mapNotNull(LanguageDetector::detect)
            .groupingBy { it }.eachCount()
            .map { LanguageStat(it.key, it.value) }
            .sortedWith(compareByDescending<LanguageStat> { it.changedFiles }.thenBy { it.language })
        val uniqueDates = dates.distinct().sorted()
        return DeveloperProfile(
            identity = request.identity,
            period = request.period,
            periodLabel = periodLabel(request.period, request.now, request.zone, uniqueDates.firstOrNull()),
            totalCommits = commits.size,
            mostActiveDay = dayCounts.maxWithOrNull(compareBy<Map.Entry<DayOfWeek, Int>> { it.value }.thenByDescending { it.key.value })?.key,
            lateNightPercentage = percentage(late, commits.size),
            mostActiveHour = hourCounts.maxWithOrNull(compareBy<Map.Entry<Int, Int>> { it.value }.thenByDescending { it.key })?.key,
            activeDays = uniqueDates.size,
            longestStreakDays = longestStreak(uniqueDates),
            fixCommits = commits.count { FIX_PATTERN.containsMatchIn(it.subject) },
            weekendPercentage = percentage(weekend, commits.size),
            languages = languages,
            firstActivityDate = uniqueDates.firstOrNull(),
            lastActivityDate = uniqueDates.lastOrNull(),
        )
    }

    private fun parseCommits(output: String, cancellation: CancellationCheck): List<CommitRecord> =
        output.split('\u001e').asSequence().mapNotNull { block ->
            cancellation.checkCancelled()
            val lines = block.trim().lines()
            if (lines.isEmpty() || lines[0].isBlank()) return@mapNotNull null
            val fields = lines[0].split('\u001f', limit = 5)
            if (fields.size != 5) return@mapNotNull null
            val instant = runCatching { OffsetDateTime.parse(fields[3]).toInstant() }.getOrNull() ?: return@mapNotNull null
            CommitRecord(fields[0], fields[1], fields[2], instant, fields[4], lines.drop(1).filter { it.isNotBlank() })
        }.distinctBy { it.hash }.toList()

    private fun runGit(repository: Path, args: List<String>, cancellation: CancellationCheck = CancellationCheck {}): String {
        val command = listOf(gitExecutable, "-C", repository.toAbsolutePath().normalize().toString()) + args
        try {
            val process = ProcessBuilder(command).redirectErrorStream(false).start()
            val stdoutFuture = CompletableFuture.supplyAsync { process.inputStream.use { it.readBytes() } }
            val stderrFuture = CompletableFuture.supplyAsync { process.errorStream.use { it.readBytes() } }
            while (!process.waitFor(100, TimeUnit.MILLISECONDS)) {
                try { cancellation.checkCancelled() } catch (t: Throwable) { process.destroyForcibly(); throw t }
            }
            val stdout = stdoutFuture.join()
            val stderr = stderrFuture.join()
            if (process.exitValue() != 0) {
                val detail = String(stderr, StandardCharsets.UTF_8).trim().take(500)
                throw GitAnalysisException(if (detail.isBlank()) "Git command failed." else detail)
            }
            return String(stdout, StandardCharsets.UTF_8)
        } catch (e: GitAnalysisException) {
            throw e
        } catch (e: Exception) {
            throw GitAnalysisException("Unable to run Git. Make sure Git is installed and available on PATH.", e)
        }
    }

    companion object {
        private val FIX_PATTERN = Regex("(?i)(^|\\b)(fix(e[ds])?|bug(s|fix)?|hotfix|regression|defect|patch|resolve[ds]?)(\\b|:)")
        private fun percentage(part: Int, total: Int) = if (total == 0) 0 else (part * 100.0 / total).roundToInt()

        fun longestStreak(sortedUniqueDates: List<LocalDate>): Int {
            if (sortedUniqueDates.isEmpty()) return 0
            var best = 1
            var current = 1
            sortedUniqueDates.distinct().sorted().zipWithNext().forEach { (before, after) ->
                current = if (before.plusDays(1) == after) current + 1 else 1
                best = maxOf(best, current)
            }
            return best
        }

        private fun periodLabel(period: ReportingPeriod, now: Instant, zone: ZoneId, first: LocalDate?): String = when (period) {
            ReportingPeriod.LAST_30_DAYS -> "Last 30 days"
            ReportingPeriod.CURRENT_YEAR -> now.atZone(zone).year.toString()
            ReportingPeriod.ALL_TIME -> first?.let { "${it.year}–${now.atZone(zone).year}" } ?: "All time"
        }
    }
}

private data class CommitRecord(
    val hash: String,
    val authorName: String,
    val authorEmail: String,
    val instant: Instant,
    val subject: String,
    val files: List<String>,
)

private data class IdentityCommit(val name: String, val email: String, val instant: Instant)
private data class IdentityActivity(val name: String, val email: String, val commitCount: Int, val lastActiveAt: Instant)

object LanguageDetector {
    private val excludedParts = setOf("node_modules", "vendor", "dist", "build", ".gradle", ".idea", "generated", "coverage", "target")
    private val binaryExtensions = setOf("png", "jpg", "jpeg", "gif", "webp", "ico", "pdf", "zip", "jar", "class", "woff", "woff2", "ttf", "mp3", "mp4", "mov")
    private val languages = mapOf(
        "kt" to "Kotlin", "kts" to "Kotlin", "java" to "Java", "php" to "PHP", "py" to "Python",
        "js" to "JavaScript", "jsx" to "JavaScript", "ts" to "TypeScript", "tsx" to "TypeScript",
        "go" to "Go", "rs" to "Rust", "rb" to "Ruby", "swift" to "Swift", "cs" to "C#",
        "c" to "C", "h" to "C/C++", "cc" to "C++", "cpp" to "C++", "hpp" to "C++",
        "scala" to "Scala", "dart" to "Dart", "ex" to "Elixir", "exs" to "Elixir", "clj" to "Clojure",
        "vue" to "Vue", "svelte" to "Svelte", "html" to "HTML", "css" to "CSS", "scss" to "SCSS",
        "sh" to "Shell", "sql" to "SQL", "r" to "R"
    )

    fun detect(path: String): String? {
        val clean = path.replace('\\', '/').lowercase()
        if (clean.split('/').any { it in excludedParts } || clean.endsWith(".min.js") || clean.endsWith(".min.css")) return null
        val ext = clean.substringAfterLast('.', "")
        if (ext.isBlank() || ext in binaryExtensions) return null
        return languages[ext]
    }
}
