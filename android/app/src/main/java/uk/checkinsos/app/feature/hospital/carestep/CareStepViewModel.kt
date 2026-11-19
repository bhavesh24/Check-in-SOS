package uk.checkinsos.app.feature.hospital.carestep

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import uk.checkinsos.app.feature.shared.AlertSummary
import uk.checkinsos.app.feature.shared.DisplayText
import uk.checkinsos.app.feature.shared.FlowViewModel
import uk.checkinsos.domain.usecase.RecordCareOutcome

/** One hospital question step. Records the answer's outcome (if any), then moves on. */
class CareStepViewModel(
    step: CareStep,
    alert: AlertSummary,
    private val recordOutcome: RecordCareOutcome,
) : FlowViewModel<CareStepUiState, CareStepViewModel.Event>(CareStepUiState(step, alert)) {

    /** [next] null → back to the Patients list. */
    data class Event(val next: CareStep?, val alert: AlertSummary)

    fun choose(answer: CareStep.Answer) {
        if (currentState.isBusy) return
        setState { it.copy(isBusy = true) }
        viewModelScope.launch {
            try {
                answer.outcome?.let { recordOutcome(currentState.alert.id, it) }
                setState { it.copy(isBusy = false) }
                send(Event(answer.next, currentState.alert))
            } catch (e: Exception) {
                setState { it.copy(isBusy = false, errorMessage = DisplayText.GENERIC_ERROR) }
            }
        }
    }
}
