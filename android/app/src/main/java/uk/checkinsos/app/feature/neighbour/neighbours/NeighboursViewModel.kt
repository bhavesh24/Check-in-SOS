package uk.checkinsos.app.feature.neighbour.neighbours

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import uk.checkinsos.app.feature.shared.AlertSummary
import uk.checkinsos.app.feature.shared.DisplayText
import uk.checkinsos.app.feature.shared.FlowViewModel
import uk.checkinsos.domain.model.AlertRecipient
import uk.checkinsos.domain.usecase.GetNeighbours
import uk.checkinsos.domain.usecase.SimulateAlert

/** The neighbour's list of older adults. "Simulate alert" stands in for a push notification. */
class NeighboursViewModel(
    private val getNeighbours: GetNeighbours,
    private val simulateAlert: SimulateAlert,
) : FlowViewModel<NeighboursUiState, NeighboursViewModel.Event>(NeighboursUiState()) {

    data class Event(val alert: AlertSummary)

    fun load() {
        viewModelScope.launch {
            try {
                val list = getNeighbours()
                setState { it.copy(neighbours = list, errorMessage = null) }
            } catch (e: Exception) {
                setState { it.copy(errorMessage = DisplayText.GENERIC_ERROR) }
            }
        }
    }

    fun simulate() {
        if (currentState.isBusy) return
        setState { it.copy(isBusy = true) }
        viewModelScope.launch {
            try {
                val alert = simulateAlert(AlertRecipient.NEIGHBOUR)
                setState { it.copy(isBusy = false) }
                send(Event(AlertSummary(alert)))
            } catch (e: Exception) {
                setState { it.copy(isBusy = false, errorMessage = DisplayText.GENERIC_ERROR) }
            }
        }
    }
}
