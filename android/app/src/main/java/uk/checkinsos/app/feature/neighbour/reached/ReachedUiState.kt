package uk.checkinsos.app.feature.neighbour.reached

import uk.checkinsos.app.feature.shared.AlertSummary
import uk.checkinsos.app.feature.shared.FlowSteps

/** "Have you reached?" — step 2 of the neighbour flow. */
data class ReachedUiState(val alert: AlertSummary, val isBusy: Boolean = false, val errorMessage: String? = null) {
    val step: Int get() = 2
    val totalSteps: Int get() = FlowSteps.NEIGHBOUR
    val message: String
        get() = "You have acknowledged to help your neighbour ${alert.subjectName}, please let us know have you reached"
}
