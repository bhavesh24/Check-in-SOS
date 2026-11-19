package uk.checkinsos.app.feature.neighbour.alive

import uk.checkinsos.app.feature.shared.AlertSummary
import uk.checkinsos.app.feature.shared.FlowSteps

/** "Is your neighbour alive?" — step 4. [calledNumber] set when 999/111 was chosen (demo, never dials). */
data class AliveUiState(
    val alert: AlertSummary,
    val calledNumber: String?,
    val isBusy: Boolean = false,
    val errorMessage: String? = null,
) {
    val step: Int get() = 4
    val totalSteps: Int get() = FlowSteps.NEIGHBOUR
    val heading: String get() = "Emergency services alerted, Is your neighbour alive?"
    val demoMessage: String? get() = calledNumber?.let { "Demo: would call $it" }
}
