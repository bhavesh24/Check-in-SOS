package uk.checkinsos.app.feature.family.lovedones

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import uk.checkinsos.app.feature.shared.DisplayText
import uk.checkinsos.app.feature.shared.FlowViewModel
import uk.checkinsos.domain.usecase.GetLovedOnes

class LovedOnesViewModel(private val getLovedOnes: GetLovedOnes) :
    FlowViewModel<LovedOnesUiState, LovedOnesViewModel.Event>(LovedOnesUiState()) {

    data class Event(val olderAdultId: String)

    fun load() {
        viewModelScope.launch {
            try {
                val list = getLovedOnes()
                setState { it.copy(lovedOnes = list, errorMessage = null) }
            } catch (e: Exception) {
                setState { it.copy(errorMessage = DisplayText.GENERIC_ERROR) }
            }
        }
    }

    fun select(olderAdultId: String) = send(Event(olderAdultId))
}
