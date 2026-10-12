package com.devwrapped.plugin

import com.devwrapped.core.GitIdentity
import com.devwrapped.core.ReportingPeriod
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class IdentitySelectionControllerTest {
    private val configured = GitIdentity("Configured Dev", "configured@test")
    private val configuredRecent = configured.copy(commitCount = 3)
    private val otherNewest = GitIdentity("Other Dev", "other@test", 8)

    @Test fun `configured author is preferred when active despite a newer identity`() {
        val controller = IdentitySelectionController(
            mapOf(ReportingPeriod.LAST_30_DAYS to listOf(otherNewest, configuredRecent)),
            configured,
        )
        assertEquals(configured.key(), controller.initialSelection()?.key())
        assertEquals(3, controller.initialSelection()?.commitCount)
    }

    @Test fun `most recently active identity is used when configured author is inactive`() {
        val older = GitIdentity("Older", "older@test", 20)
        val controller = IdentitySelectionController(
            mapOf(ReportingPeriod.LAST_30_DAYS to listOf(otherNewest, older)),
            configured,
        )
        assertEquals(otherNewest.key(), controller.initialSelection()?.key())
    }

    @Test fun `period switching uses period counts and preserves manual choices`() {
        val firstThirty = configuredRecent.copy(commitCount = 2)
        val secondThirty = otherNewest.copy(commitCount = 4)
        val firstYear = configuredRecent.copy(commitCount = 11)
        val secondYear = otherNewest.copy(commitCount = 17)
        val controller = IdentitySelectionController(
            mapOf(
                ReportingPeriod.LAST_30_DAYS to listOf(firstThirty, secondThirty),
                ReportingPeriod.CURRENT_YEAR to listOf(firstYear, secondYear),
            ),
            configured,
        )

        controller.markManualSelection(secondThirty)
        val yearSelection = controller.switchPeriod(ReportingPeriod.CURRENT_YEAR)
        assertEquals(secondYear.key(), yearSelection?.key())
        assertEquals(17, yearSelection?.commitCount)

        controller.markManualSelection(firstYear)
        val restoredThirtyDaySelection = controller.switchPeriod(ReportingPeriod.LAST_30_DAYS)
        assertEquals(secondThirty.key(), restoredThirtyDaySelection?.key())
        assertEquals(4, restoredThirtyDaySelection?.commitCount)
    }

    @Test fun `empty period has no selection but another period can recover`() {
        val controller = IdentitySelectionController(
            mapOf(
                ReportingPeriod.LAST_30_DAYS to emptyList(),
                ReportingPeriod.ALL_TIME to listOf(configuredRecent),
            ),
            configured,
        )
        assertNull(controller.initialSelection())
        assertEquals(configured.key(), controller.switchPeriod(ReportingPeriod.ALL_TIME)?.key())
    }
}
