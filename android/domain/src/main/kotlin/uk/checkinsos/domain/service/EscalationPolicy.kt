package uk.checkinsos.domain.service

import java.time.Duration

/** How long a recipient has to acknowledge an alert, by score. */
object EscalationPolicy {
    /** Placeholder: no timeout is specified for a missed check-in. */
    val MISSED_CHECK_IN_TIMEOUT: Duration = Duration.ofMinutes(15)

    /** True if a score should alert the 1st-level neighbour. */
    fun shouldAlert(score: Int): Boolean = !ScoreCalculator.isGood(score)

    /** 50–69 → 30 min, 30–49 → 20 min, 0–29 → 15 min, ≥70 → null (no alert), null score → missed check-in timeout. */
    fun acknowledgementTimeout(score: Int?): Duration? = when {
        score == null -> MISSED_CHECK_IN_TIMEOUT
        score >= ScoreCalculator.GOOD_THRESHOLD -> null
        score >= 50 -> Duration.ofMinutes(30)
        score >= 30 -> Duration.ofMinutes(20)
        else -> Duration.ofMinutes(15)
    }
}
