package uk.checkinsos.app.feature.neighbour.reached

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import uk.checkinsos.app.feature.shared.AlertSummary
import uk.checkinsos.app.feature.shared.DisplayText
import uk.checkinsos.app.feature.shared.FlowViewModel
import uk.checkinsos.domain.usecase.MarkArrived

class ReachedViewModel(alert: AlertSummary, private val markArrived: MarkArrived) :
    FlowViewModel<ReachedUiState, ReachedViewModel.Event>(ReachedUiState(alert)) {

    data class Event(val alert: AlertSummary)

    fun confirm() {
        if (currentState.isBusy) return
        setState { it.copy(isBusy = true) }
        viewModelScope.launch {
            try {
                markArrived(currentState.alert.id)
                setState { it.copy(isBusy = false) }
                send(Event(currentState.alert))
            } catch (e: Exception) {
                setState { it.copy(isBusy = false, errorMessage = DisplayText.GENERIC_ERROR) }
            }
        }
    }
}
