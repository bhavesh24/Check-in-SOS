package uk.checkinsos.data

import uk.checkinsos.domain.model.CareOutcome
import uk.checkinsos.domain.model.Neighbour
import uk.checkinsos.domain.model.OlderAdult
import uk.checkinsos.domain.model.TimelineEventKind
import java.time.Instant

/** Seed values shared with iOS and docs/screens.md ("Mock data" + "Fixed values for tests"). */
object MockFixtures {
    /** Fixed clock for tests and the test AppContainer: 2026-01-15T09:00:00Z. */
    val FIXED_NOW: Instant = Instant.parse("2026-01-15T09:00:00Z")

    /** The older adult who uses the older-adult role. */
    const val CURRENT_OLDER_ADULT_ID = "oa-1"

    internal fun olderAdults(): List<OlderAdult> = listOf(
        OlderAdult(
            "oa-1", "Margaret Wilson", 76,
            Neighbour(firstName = "Alex", lastName = "Brown", email = "alex.brown@example.com", mobile = "07123456789"),
        ),
        OlderAdult(
            "oa-2", "Arthur Patel", 38,
            Neighbour(firstName = "Priya", lastName = "Shah", email = "priya.shah@example.com", mobile = "07234567890"),
        ),
        OlderAdult(
            "oa-3", "Edith Clarke", null,
            Neighbour(firstName = "Tom", middleName = "J", lastName = "Hughes", email = "tom.hughes@example.com", mobile = "07345678901"),
        ),
    )

    /** Patient id → older adult id, in list order. */
    internal val patients: List<Pair<String, String>> = listOf("p-1" to "oa-2", "p-2" to "oa-3", "p-3" to "oa-1")

    internal data class SeedEvent(val olderAdultId: String, val minutesBefore: Long, val kind: TimelineEventKind)

    internal fun timeline(): List<SeedEvent> = listOf(
        SeedEvent("oa-1", 60, TimelineEventKind.Score(76)),
        SeedEvent("oa-2", 180, TimelineEventKind.Score(38)),
        SeedEvent("oa-2", 179, TimelineEventKind.NotificationSent(1)),
        SeedEvent("oa-2", 170, TimelineEventKind.Acknowledged(1)),
        SeedEvent("oa-2", 155, TimelineEventKind.NeighbourReached),
        SeedEvent("oa-2", 150, TimelineEventKind.Escalated(2)),
        SeedEvent("oa-2", 140, TimelineEventKind.Acknowledged(2)),
        SeedEvent("oa-2", 90, TimelineEventKind.Outcome(CareOutcome.IN_CARE)),
        SeedEvent("oa-2", 10, TimelineEventKind.Outcome(CareOutcome.RECOVERED)),
    )
}
