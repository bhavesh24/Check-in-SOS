package uk.checkinsos.app.feature.family.lovedones

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import uk.checkinsos.app.feature.shared.DisplayText
import uk.checkinsos.app.feature.shared.FlowViewModel
import uk.checkinsos.domain.service.TimeProvider
import uk.checkinsos.domain.usecase.GetLovedOnes
import java.time.ZoneId

class LovedOnesViewModel(
    private val getLovedOnes: GetLovedOnes,
    private val time: TimeProvider,
    private val zone: ZoneId,
) :
    FlowViewModel<LovedOnesUiState, LovedOnesViewModel.Event>(LovedOnesUiState()) {

    data class Event(val olderAdultId: String)

    fun load() {
        viewModelScope.launch {
            try {
                val list = getLovedOnes()
                val now = time.now()
                val texts = list.associate { it.id to DisplayText.lastCheckIn(it.lastCheckIn, now, zone) }
                setState { it.copy(lovedOnes = list, checkInTexts = texts, errorMessage = null) }
            } catch (e: Exception) {
                setState { it.copy(errorMessage = DisplayText.GENERIC_ERROR) }
            }
        }
    }

    fun select(olderAdultId: String) = send(Event(olderAdultId))
}
