package uk.checkinsos.app.feature.shared

import uk.checkinsos.domain.service.ScoreCalculator

/** How a check-in score is coloured on the Score ring. Follows the EscalationPolicy bands. */
enum class ScoreBand {
    /** ≥ 70: no alert. */
    GOOD,

    /** 50–69. */
    MILD,

    /** 30–49. */
    MODERATE,

    /** 0–29. */
    SEVERE,
    ;

    companion object {
        fun of(score: Int): ScoreBand = when {
            score >= ScoreCalculator.GOOD_THRESHOLD -> GOOD
            score >= 50 -> MILD
            score >= 30 -> MODERATE
            else -> SEVERE
        }
    }
}
