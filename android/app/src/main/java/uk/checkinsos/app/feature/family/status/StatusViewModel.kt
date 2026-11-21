package uk.checkinsos.app.feature.family.status

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import uk.checkinsos.app.feature.shared.DisplayText
import uk.checkinsos.app.feature.shared.FlowViewModel
import uk.checkinsos.domain.service.TimeProvider
import uk.checkinsos.domain.usecase.GetLovedOneStatus
import uk.checkinsos.domain.usecase.GetScoreHistory
import java.time.ZoneId

/** Reloads on every appearance so an edited neighbour shows straight away. */
class StatusViewModel(
    private val olderAdultId: String,
    private val getStatus: GetLovedOneStatus,
    private val getScoreHistory: GetScoreHistory,
    private val time: TimeProvider,
    private val zone: ZoneId,
) : FlowViewModel<StatusUiState, StatusViewModel.Event>(StatusUiState()) {

    sealed interface Event {
        data class EditNeighbour(val olderAdultId: String) : Event
        data class Timeline(val olderAdultId: String) : Event
    }

    fun load() {
        viewModelScope.launch {
            try {
                val adult = getStatus(olderAdultId)
                val history = getScoreHistory(olderAdultId)
                val lastCheckIn = DisplayText.lastCheckIn(adult.lastCheckIn, time.now(), zone)
                setState { it.copy(adult = adult, history = history, lastCheckInText = lastCheckIn, errorMessage = null) }
            } catch (e: Exception) {
                setState { it.copy(errorMessage = DisplayText.GENERIC_ERROR) }
            }
        }
    }

    fun editNeighbour() = send(Event.EditNeighbour(olderAdultId))
    fun openTimeline() = send(Event.Timeline(olderAdultId))
}
