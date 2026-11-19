package uk.checkinsos.app.feature.hospital.patients

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import uk.checkinsos.app.feature.shared.AlertSummary
import uk.checkinsos.app.feature.shared.DisplayText
import uk.checkinsos.app.feature.shared.FlowViewModel
import uk.checkinsos.domain.model.AlertRecipient
import uk.checkinsos.domain.usecase.GetPatients
import uk.checkinsos.domain.usecase.SimulateAlert

/** The hospital's patient list. "Simulate alert" stands in for a push notification. */
class PatientsViewModel(
    private val getPatients: GetPatients,
    private val simulateAlert: SimulateAlert,
) : FlowViewModel<PatientsUiState, PatientsViewModel.Event>(PatientsUiState()) {

    data class Event(val alert: AlertSummary)

    fun load() {
        viewModelScope.launch {
            try {
                val list = getPatients()
                setState { it.copy(patients = list, errorMessage = null) }
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
                val alert = simulateAlert(AlertRecipient.HOSPITAL)
                setState { it.copy(isBusy = false) }
                send(Event(AlertSummary(alert)))
            } catch (e: Exception) {
                setState { it.copy(isBusy = false, errorMessage = DisplayText.GENERIC_ERROR) }
            }
        }
    }
}
