package uk.checkinsos.app.feature.hospital.carestep

import uk.checkinsos.app.feature.shared.AlertSummary
import uk.checkinsos.app.feature.shared.FlowSteps

data class CareStepUiState(
    val step: CareStep,
    val alert: AlertSummary,
    val isBusy: Boolean = false,
    val errorMessage: String? = null,
) {
    val totalSteps: Int get() = FlowSteps.HOSPITAL
}
