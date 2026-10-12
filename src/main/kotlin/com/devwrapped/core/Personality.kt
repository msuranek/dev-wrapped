package com.devwrapped.core

enum class Archetype(val title: String, val subtitle: String) {
    MIDNIGHT_ARCHITECT("The Midnight Architect", "You build while the world sleeps."),
    WEEKEND_WARRIOR("The Weekend Warrior", "Weekends are just another opportunity to ship."),
    BUG_WHISPERER("The Bug Whisperer", "Your bugs know you by name."),
    COMMIT_MACHINE("The Commit Machine", "One more commit. And another."),
    POLYGLOT("The Polyglot", "One language was never going to be enough."),
    STEADY_BUILDER("The Steady Builder", "Consistency is your superpower.")
}

object PersonalityThresholds {
    const val MIN_COMMITS = 5
    const val MIDNIGHT_PERCENT = 35
    const val WEEKEND_PERCENT = 35
    const val FIX_PERCENT = 30
    const val MACHINE_MIN_COMMITS = 15
    const val MACHINE_COMMITS_PER_DAY = 3.0
    const val POLYGLOT_MIN_LANGUAGES = 3
    const val POLYGLOT_MIN_FILES_PER_LANGUAGE = 3
}

data class PersonalityResult(val archetype: Archetype?, val explanation: String)

object PersonalityClassifier {
    fun classify(profile: DeveloperProfile): PersonalityResult {
        if (profile.totalCommits < PersonalityThresholds.MIN_COMMITS) {
            return PersonalityResult(null, "At least ${PersonalityThresholds.MIN_COMMITS} commits are needed for a meaningful Developer DNA result.")
        }
        val meaningfulLanguages = profile.languages.count { it.changedFiles >= PersonalityThresholds.POLYGLOT_MIN_FILES_PER_LANGUAGE }
        val scored = listOf(
            Candidate(Archetype.MIDNIGHT_ARCHITECT, profile.lateNightPercentage / PersonalityThresholds.MIDNIGHT_PERCENT.toDouble(), profile.lateNightPercentage >= PersonalityThresholds.MIDNIGHT_PERCENT, "${profile.lateNightPercentage}% of commits landed late at night"),
            Candidate(Archetype.WEEKEND_WARRIOR, profile.weekendPercentage / PersonalityThresholds.WEEKEND_PERCENT.toDouble(), profile.weekendPercentage >= PersonalityThresholds.WEEKEND_PERCENT, "${profile.weekendPercentage}% of commits landed on weekends"),
            Candidate(Archetype.BUG_WHISPERER, profile.fixPercentage / PersonalityThresholds.FIX_PERCENT.toDouble(), profile.fixPercentage >= PersonalityThresholds.FIX_PERCENT, "${profile.fixPercentage}% of commit messages suggest fixes"),
            Candidate(Archetype.COMMIT_MACHINE, profile.commitsPerActiveDay / PersonalityThresholds.MACHINE_COMMITS_PER_DAY, profile.totalCommits >= PersonalityThresholds.MACHINE_MIN_COMMITS && profile.commitsPerActiveDay >= PersonalityThresholds.MACHINE_COMMITS_PER_DAY, "${"%.1f".format(profile.commitsPerActiveDay)} commits per active day"),
            Candidate(Archetype.POLYGLOT, meaningfulLanguages / PersonalityThresholds.POLYGLOT_MIN_LANGUAGES.toDouble(), meaningfulLanguages >= PersonalityThresholds.POLYGLOT_MIN_LANGUAGES, "$meaningfulLanguages meaningfully used languages"),
        ).filter { it.qualifies }
        val winner = scored.maxWithOrNull(compareBy<Candidate> { it.score }.thenByDescending { it.archetype.ordinal })
        return if (winner != null) PersonalityResult(winner.archetype, winner.explanation)
        else PersonalityResult(Archetype.STEADY_BUILDER, "A balanced activity pattern across ${profile.activeDays} active days")
    }

    private data class Candidate(val archetype: Archetype, val score: Double, val qualifies: Boolean, val explanation: String)
}
