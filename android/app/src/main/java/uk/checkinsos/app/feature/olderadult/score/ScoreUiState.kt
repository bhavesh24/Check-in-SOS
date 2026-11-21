package uk.checkinsos.app.feature.olderadult.score

import uk.checkinsos.app.feature.shared.FlowSteps
import uk.checkinsos.app.feature.shared.ScoreBand
import uk.checkinsos.domain.service.ScoreCalculator

/** Result of a check-in. Below 70 the neighbour has been notified. */
data class ScoreUiState(val score: Int, val neighbourNotified: Boolean) {
    val step: Int get() = 2
    val totalSteps: Int get() = FlowSteps.OLDER_ADULT
    val notificationText: String get() = "A notification has been sent to your neighbour"
    val reassuranceText: String get() = "You're doing well today."

    /** Ring colour: green when good, then deepening reds for 50–69, 30–49 and 0–29. */
    val band: ScoreBand get() = ScoreBand.of(score)

    /** Shown when the score is good, in place of the neighbour notification. */
    val showsReassurance: Boolean get() = score >= ScoreCalculator.GOOD_THRESHOLD
}
