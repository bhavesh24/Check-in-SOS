package uk.checkinsos.app.feature.family.status

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import uk.checkinsos.app.feature.shared.DisplayText
import uk.checkinsos.app.feature.shared.FlowViewModel
import uk.checkinsos.domain.usecase.GetLovedOneStatus

/** Reloads on every appearance so an edited neighbour shows straight away. */
class StatusViewModel(
    private val olderAdultId: String,
    private val getStatus: GetLovedOneStatus,
) : FlowViewModel<StatusUiState, StatusViewModel.Event>(StatusUiState()) {

    sealed interface Event {
        data class EditNeighbour(val olderAdultId: String) : Event
        data class Timeline(val olderAdultId: String) : Event
    }

    fun load() {
        viewModelScope.launch {
            try {
                val adult = getStatus(olderAdultId)
                setState { it.copy(adult = adult, errorMessage = null) }
            } catch (e: Exception) {
                setState { it.copy(errorMessage = DisplayText.GENERIC_ERROR) }
            }
        }
    }

    fun editNeighbour() = send(Event.EditNeighbour(olderAdultId))
    fun openTimeline() = send(Event.Timeline(olderAdultId))
}
