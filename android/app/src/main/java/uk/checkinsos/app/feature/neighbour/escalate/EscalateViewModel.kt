package uk.checkinsos.app.feature.neighbour.escalate

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import uk.checkinsos.app.feature.shared.AlertSummary
import uk.checkinsos.app.feature.shared.DisplayText
import uk.checkinsos.app.feature.shared.FlowViewModel
import uk.checkinsos.domain.model.CareOutcome
import uk.checkinsos.domain.usecase.EscalateAlert
import uk.checkinsos.domain.usecase.RecordCareOutcome

/** "Yes" escalates, then offers 999/111, which never dial in this build. */
class EscalateViewModel(
    alert: AlertSummary,
    private val recordOutcome: RecordCareOutcome,
    private val escalateAlert: EscalateAlert,
) : FlowViewModel<EscalateUiState, EscalateViewModel.Event>(EscalateUiState(alert)) {

    sealed interface Event {
        data object AllOk : Event
        data class Called(val alert: AlertSummary, val number: String) : Event
    }

    fun allOk() = perform {
        recordOutcome(currentState.alert.id, CareOutcome.ALL_OK)
        send(Event.AllOk)
    }

    fun alertServices() = perform {
        escalateAlert(currentState.alert.id)
        setState { it.copy(isEmergencyDialogShown = true) }
    }

    /** Demo only: never dials. The Alive screen shows "Demo: would call <number>". */
    fun call(number: String) {
        setState { it.copy(isEmergencyDialogShown = false) }
        send(Event.Called(currentState.alert, number))
    }

    fun cancel() = setState { it.copy(isEmergencyDialogShown = false) }

    private fun perform(work: suspend () -> Unit) {
        if (currentState.isBusy) return
        setState { it.copy(isBusy = true) }
        viewModelScope.launch {
            try {
                work()
                setState { it.copy(isBusy = false, errorMessage = null) }
            } catch (e: Exception) {
                setState { it.copy(isBusy = false, errorMessage = DisplayText.GENERIC_ERROR) }
            }
        }
    }
}
