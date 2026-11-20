package uk.checkinsos.app.feature.family.editneighbour

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import uk.checkinsos.app.feature.shared.DisplayText
import uk.checkinsos.app.feature.shared.FlowViewModel
import uk.checkinsos.domain.model.DomainException
import uk.checkinsos.domain.model.Neighbour
import uk.checkinsos.domain.usecase.GetLovedOneStatus
import uk.checkinsos.domain.usecase.UpdateNeighbour

class EditNeighbourViewModel(
    private val olderAdultId: String,
    private val getStatus: GetLovedOneStatus,
    private val updateNeighbour: UpdateNeighbour,
) : FlowViewModel<EditNeighbourUiState, EditNeighbourViewModel.Event>(EditNeighbourUiState()) {

    data object Event

    /** Pre-fills the form once; later appearances keep what the user typed. */
    fun load() {
        if (currentState.isLoaded) return
        viewModelScope.launch {
            try {
                val n = getStatus(olderAdultId).neighbour
                setState {
                    it.copy(
                        firstName = n.firstName, middleName = n.middleName, lastName = n.lastName,
                        email = n.email, mobile = nationalNumber(n.mobile), isLoaded = true,
                    )
                }
            } catch (e: Exception) {
                setState { it.copy(errorMessage = DisplayText.GENERIC_ERROR) }
            }
        }
    }

    fun onFirstName(value: String) = setState { it.copy(firstName = value) }
    fun onMiddleName(value: String) = setState { it.copy(middleName = value) }
    fun onLastName(value: String) = setState { it.copy(lastName = value) }
    fun onEmail(value: String) = setState { it.copy(email = value) }
    fun onMobile(value: String) = setState { it.copy(mobile = value) }

    fun save() {
        val s = currentState
        if (s.isSaving) return
        setState { it.copy(isSaving = true) }
        val neighbour = Neighbour(s.firstName, s.middleName, s.lastName, s.email, storedNumber(s.mobile))
        viewModelScope.launch {
            try {
                updateNeighbour(olderAdultId, neighbour)
                setState { it.copy(isSaving = false, errorMessage = null) }
                send(Event)
            } catch (e: DomainException.InvalidNeighbour) {
                setState { it.copy(isSaving = false, errorMessage = e.error.message) }
            } catch (e: Exception) {
                setState { it.copy(isSaving = false, errorMessage = DisplayText.GENERIC_ERROR) }
            }
        }
    }

    companion object {
        /** "07700900123" → "7700900123" (shown after the "+44" prefix). */
        fun nationalNumber(stored: String): String = if (stored.startsWith("0")) stored.drop(1) else stored

        /** "7700 900123" → "07700 900123". Numbers already starting "0" or "+" are kept as typed. */
        fun storedNumber(field: String): String {
            val trimmed = field.trim()
            return if (trimmed.isEmpty() || trimmed.startsWith("0") || trimmed.startsWith("+")) trimmed else "0$trimmed"
        }
    }
}
