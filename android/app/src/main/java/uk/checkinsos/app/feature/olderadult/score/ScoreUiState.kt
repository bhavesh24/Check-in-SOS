package uk.checkinsos.app.feature.olderadult.score

import uk.checkinsos.app.feature.shared.FlowSteps

/** Result of a check-in. Below 70 the neighbour has been notified. */
data class ScoreUiState(val score: Int, val neighbourNotified: Boolean) {
    val step: Int get() = 2
    val totalSteps: Int get() = FlowSteps.OLDER_ADULT
    val notificationText: String get() = "A notification has been sent to your neighbour"
}
