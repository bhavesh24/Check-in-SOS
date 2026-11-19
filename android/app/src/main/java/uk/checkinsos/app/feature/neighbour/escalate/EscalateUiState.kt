package uk.checkinsos.app.feature.neighbour.escalate

import uk.checkinsos.app.feature.shared.AlertSummary
import uk.checkinsos.app.feature.shared.FlowSteps

/** "Do you want to escalate?" — step 3. */
data class EscalateUiState(
    val alert: AlertSummary,
    val isEmergencyDialogShown: Boolean = false,
    val isBusy: Boolean = false,
    val errorMessage: String? = null,
) {
    val step: Int get() = 3
    val totalSteps: Int get() = FlowSteps.NEIGHBOUR
    val heading: String get() = "Do you want to escalate?"
    val emergencyNumbers: List<String> get() = listOf("999", "111")
}
