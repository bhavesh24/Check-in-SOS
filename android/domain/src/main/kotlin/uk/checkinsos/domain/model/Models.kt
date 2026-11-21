package uk.checkinsos.domain.model

import java.time.Duration
import java.time.Instant

/** The four user types of Check-in SOS. */
enum class Role { OLDER_ADULT, FAMILY, HOSPITAL, NEIGHBOUR }

/** "Are you" — physical wellbeing answer. */
enum class PhysicalWellbeing { FINE, GOOD, ILL, VERY_SICK }

/** "Are you" — mood answer. */
enum class Mood { HAPPY, PLEASANT, ANXIOUS, DEPRESSED }

/** A completed daily check-in. */
data class Assessment(val physical: PhysicalWellbeing, val mood: Mood)

/** Outcome of `SubmitAssessment`. */
data class AssessmentResult(val score: Int, val neighbourNotified: Boolean)

/** A 1st-level notified person (the neighbour contact a family member manages). */
data class Neighbour(
    val firstName: String,
    val middleName: String = "",
    val lastName: String,
    val email: String,
    val mobile: String,
) {
    /** "First Last" — the middle name is omitted for display. */
    val displayName: String get() = "$firstName $lastName"
}

/** The person who checks in daily. [latestScore] is null if they have never checked in. */
data class OlderAdult(
    val id: String,
    val name: String,
    val latestScore: Int?,
    val neighbour: Neighbour,
    /** When they last checked in, or null if they never have. */
    val lastCheckIn: Instant? = null,
)

/** One past check-in score, for the family's "last check-in" line and trend chart. */
data class ScoreRecord(val score: Int, val date: Instant)

/** An older adult as known to a hospital. */
data class Patient(
    val id: String,
    val name: String,
    val olderAdultId: String,
    val latestScore: Int?,
)

/** Who an alert is sent to; [level] is the escalation level. */
enum class AlertRecipient(val level: Int) { NEIGHBOUR(1), HOSPITAL(2) }

enum class AlertStatus { PENDING, ACKNOWLEDGED, ARRIVED, ESCALATED, RESOLVED }

/** A help request ("Your neighbour X needs your help"). [score] is null for a missed check-in. */
data class Alert(
    val id: String,
    val olderAdultId: String,
    val subjectName: String,
    val recipient: AlertRecipient,
    val score: Int?,
    val raisedAt: Instant,
    val acknowledgeBy: Instant,
    val status: AlertStatus = AlertStatus.PENDING,
) {
    /** Seconds left before the acknowledgement deadline (rounded up, never negative). */
    fun secondsRemaining(now: Instant): Long {
        val millis = Duration.between(now, acknowledgeBy).toMillis()
        return if (millis <= 0) 0 else (millis + 999) / 1000
    }
}

/** Answers recorded by neighbours and hospitals after an alert. */
enum class CareOutcome(val resolvesAlert: Boolean) {
    ALL_OK(true), IN_CARE(false), RECOVERING(false), RECOVERED(true),
    BAD_CONDITION(false), ALIVE(true), DECEASED(true),
}
