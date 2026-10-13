package com.devwrapped.render

import com.devwrapped.core.Archetype
import com.devwrapped.core.DeveloperProfile
import java.util.Locale

data class CardAchievement(
    val title: String,
    val description: String,
)

/** Deterministic, playful card copy. These labels are not analytical assessments. */
object CardAchievements {
    fun forProfile(profile: DeveloperProfile, archetype: Archetype?): CardAchievement = when (archetype) {
        Archetype.MIDNIGHT_ARCHITECT -> CardAchievement(
            "Night Owl Mode Activated",
            if (profile.lateNightPercentage > 0) {
                "${profile.lateNightPercentage}% of your commits landed after dark. Your keyboard clearly has a night shift."
            } else {
                "No late-night commits appeared in this period. The moon badge stayed on standby."
            },
        )
        Archetype.WEEKEND_WARRIOR -> CardAchievement(
            "Weekend Mode: ON",
            if (profile.weekendPercentage > 0) {
                "${profile.weekendPercentage}% of your commits landed on weekends. Git does not own a day planner."
            } else {
                "No weekend commits appeared in this period. The calendar badge is taking five."
            },
        )
        Archetype.BUG_WHISPERER -> CardAchievement(
            "Bug Hunter Unlocked",
            if (profile.fixCommits > 0) {
                "${profile.fixCommits} ${commitWord(profile.fixCommits)} (${profile.fixPercentage}%) mentioned fixes. Your history came with patch notes."
            } else {
                "No fix-tagged commits appeared in this period. This badge is ready for its first patch."
            },
        )
        Archetype.COMMIT_MACHINE -> CardAchievement(
            "Commit Streak Energy",
            if (profile.totalCommits > 0 && profile.activeDays > 0) {
                "${String.format(Locale.US, "%.1f", profile.commitsPerActiveDay)} commits per active day. Your Git graph found a steady beat."
            } else {
                "No commit rhythm was available for this period. The counter is waiting at the starting line."
            },
        )
        Archetype.POLYGLOT -> CardAchievement(
            "Fluent in Code",
            if (profile.languages.isNotEmpty()) {
                "${profile.languages.size} ${languageWord(profile.languages.size)} appeared in the diff. Your syntax highlighter had range."
            } else {
                "No language mix was detected in this period. The syntax palette is waiting for more diff data."
            },
        )
        Archetype.STEADY_BUILDER -> CardAchievement(
            "Consistency Unlocked",
            when {
                profile.activeDays > 0 && profile.longestStreakDays > 0 ->
                    "${profile.activeDays} active days with a ${profile.longestStreakDays}-day streak. Your commit trail kept its cadence."
                profile.activeDays > 0 ->
                    "${profile.activeDays} active ${dayWord(profile.activeDays)} in this period. Your commit trail kept a measured cadence."
                else -> "No active days appeared in this period. A future commit can start the pattern."
            },
        )
        null -> CardAchievement(
            "More Signal Needed",
            if (profile.totalCommits > 0) {
                "${profile.totalCommits} ${commitWord(profile.totalCommits)} found in this period. A little more history will reveal the pattern."
            } else {
                "No commits were found in this period. A future commit can start the pattern."
            },
        )
    }

    private fun commitWord(count: Int) = if (count == 1) "commit" else "commits"
    private fun languageWord(count: Int) = if (count == 1) "language" else "languages"
    private fun dayWord(count: Int) = if (count == 1) "day" else "days"
}
