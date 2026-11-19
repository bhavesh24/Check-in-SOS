package uk.checkinsos.domain.model

import uk.checkinsos.domain.service.ScoreCalculator
import java.time.Instant

enum class TimelineSeverity { CRITICAL, WARNING, OK }

sealed interface TimelineEventKind {
    data class Score(val value: Int) : TimelineEventKind
    data object MissedCheckIn : TimelineEventKind
    data class NotificationSent(val level: Int) : TimelineEventKind
    data class Acknowledged(val level: Int) : TimelineEventKind
    data object NeighbourReached : TimelineEventKind
    data class Escalated(val level: Int) : TimelineEventKind
    data class Outcome(val outcome: CareOutcome) : TimelineEventKind

    /** Copy shown on the Timeline screen (see docs/screens.md). */
    val title: String
        get() = when (this) {
            is Score -> "Score $value"
            MissedCheckIn -> "Missed check-in"
            is NotificationSent -> "Notification sent to ${ordinal(level)} level"
            is Acknowledged -> "${ordinal(level)} level acknowledged"
            NeighbourReached -> "Neighbour reached"
            is Escalated -> "Escalation to ${ordinal(level)} level"
            is Outcome -> when (outcome) {
                CareOutcome.ALL_OK -> "Neighbour reports all OK"
                CareOutcome.IN_CARE -> "In care in hospital"
                CareOutcome.RECOVERING -> "Recovering"
                CareOutcome.RECOVERED -> "Recovered"
                CareOutcome.BAD_CONDITION -> "In bad condition"
                CareOutcome.ALIVE -> "Confirmed alive"
                CareOutcome.DECEASED -> "Reported deceased"
            }
        }

    val severity: TimelineSeverity
        get() = when (this) {
            is Score -> if (ScoreCalculator.isGood(value)) TimelineSeverity.OK else TimelineSeverity.CRITICAL
            MissedCheckIn, is NotificationSent, is Escalated -> TimelineSeverity.CRITICAL
            is Acknowledged -> TimelineSeverity.WARNING
            NeighbourReached -> TimelineSeverity.OK
            is Outcome -> when (outcome) {
                CareOutcome.IN_CARE -> TimelineSeverity.WARNING
                CareOutcome.BAD_CONDITION, CareOutcome.DECEASED -> TimelineSeverity.CRITICAL
                CareOutcome.ALL_OK, CareOutcome.RECOVERING, CareOutcome.RECOVERED, CareOutcome.ALIVE -> TimelineSeverity.OK
            }
        }

    companion object {
        fun ordinal(level: Int): String = when (level) {
            1 -> "1st"
            2 -> "2nd"
            3 -> "3rd"
            else -> "${level}th"
        }
    }
}

data class TimelineEvent(
    val id: String,
    val olderAdultId: String,
    val date: Instant,
    val kind: TimelineEventKind,
)
