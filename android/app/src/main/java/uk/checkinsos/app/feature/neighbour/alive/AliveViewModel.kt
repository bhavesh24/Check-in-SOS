package uk.checkinsos.app.feature.neighbour.alive

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import uk.checkinsos.app.feature.shared.AlertSummary
import uk.checkinsos.app.feature.shared.DisplayText
import uk.checkinsos.app.feature.shared.FlowViewModel
import uk.checkinsos.domain.model.CareOutcome
import uk.checkinsos.domain.usecase.RecordCareOutcome

/** Both answers are recorded and return to the Neighbours list. */
class AliveViewModel(
    alert: AlertSummary,
    calledNumber: String?,
    private val recordOutcome: RecordCareOutcome,
) : FlowViewModel<AliveUiState, AliveViewModel.Event>(AliveUiState(alert, calledNumber)) {

    data object Event

    fun answer(alive: Boolean) {
        if (currentState.isBusy) return
        setState { it.copy(isBusy = true) }
        viewModelScope.launch {
            try {
                recordOutcome(currentState.alert.id, if (alive) CareOutcome.ALIVE else CareOutcome.DECEASED)
                setState { it.copy(isBusy = false) }
                send(Event)
            } catch (e: Exception) {
                setState { it.copy(isBusy = false, errorMessage = DisplayText.GENERIC_ERROR) }
            }
        }
    }
}
