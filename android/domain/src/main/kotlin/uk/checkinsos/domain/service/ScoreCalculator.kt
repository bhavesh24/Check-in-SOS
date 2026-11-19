package uk.checkinsos.domain.service

import uk.checkinsos.domain.model.Assessment
import uk.checkinsos.domain.model.Mood
import uk.checkinsos.domain.model.PhysicalWellbeing

/**
 * Converts an assessment into a 0–100 wellbeing score.
 *
 * PLACEHOLDER FORMULA: none is specified. Weights were chosen so the prototype's
 * examples reproduce (Good + Pleasant = 76, Ill + Anxious = 38). Needs clinical input — see docs/screens.md.
 */
object ScoreCalculator {
    /** Scores at or above this are "good" and do not notify anyone. */
    const val GOOD_THRESHOLD = 70

    fun score(assessment: Assessment): Int = points(assessment.physical) + points(assessment.mood)

    fun isGood(score: Int): Boolean = score >= GOOD_THRESHOLD

    internal fun points(physical: PhysicalWellbeing): Int = when (physical) {
        PhysicalWellbeing.FINE -> 50
        PhysicalWellbeing.GOOD -> 38
        PhysicalWellbeing.ILL -> 20
        PhysicalWellbeing.VERY_SICK -> 5
    }

    internal fun points(mood: Mood): Int = when (mood) {
        Mood.HAPPY -> 50
        Mood.PLEASANT -> 38
        Mood.ANXIOUS -> 18
        Mood.DEPRESSED -> 5
    }
}
