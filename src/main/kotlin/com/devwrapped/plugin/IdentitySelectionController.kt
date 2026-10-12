package com.devwrapped.plugin

import com.devwrapped.core.GitIdentity
import com.devwrapped.core.ReportingPeriod

data class IdentityKey(val name: String, val email: String)

fun GitIdentity.key() = IdentityKey(name, email)

/** Maintains period-specific options without discarding deliberate user choices. */
class IdentitySelectionController(
    private val identitiesByPeriod: Map<ReportingPeriod, List<GitIdentity>>,
    configuredIdentity: GitIdentity?,
    initialPeriod: ReportingPeriod = ReportingPeriod.LAST_30_DAYS,
) {
    private val configuredKey = configuredIdentity?.key()
    private val manualByPeriod = mutableMapOf<ReportingPeriod, IdentityKey>()
    private var lastManualKey: IdentityKey? = null

    var currentPeriod: ReportingPeriod = initialPeriod
        private set

    fun identities(period: ReportingPeriod = currentPeriod): List<GitIdentity> = identitiesByPeriod[period].orEmpty()

    fun initialSelection(): GitIdentity? = preferred(currentPeriod)

    fun markManualSelection(identity: GitIdentity?) {
        identity ?: return
        manualByPeriod[currentPeriod] = identity.key()
        lastManualKey = identity.key()
    }

    fun switchPeriod(period: ReportingPeriod): GitIdentity? {
        currentPeriod = period
        return preferred(period)
    }

    private fun preferred(period: ReportingPeriod): GitIdentity? {
        val options = identities(period)
        fun find(key: IdentityKey?) = key?.let { candidate -> options.firstOrNull { it.key() == candidate } }
        return find(manualByPeriod[period])
            ?: find(lastManualKey)
            ?: find(configuredKey)
            ?: options.firstOrNull()
    }
}
