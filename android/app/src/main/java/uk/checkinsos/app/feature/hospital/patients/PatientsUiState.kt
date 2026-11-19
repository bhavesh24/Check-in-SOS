package uk.checkinsos.app.feature.hospital.patients

import uk.checkinsos.app.feature.shared.DisplayText
import uk.checkinsos.domain.model.Patient

data class PatientsUiState(
    val patients: List<Patient> = emptyList(),
    val isBusy: Boolean = false,
    val errorMessage: String? = null,
) {
    fun subtitle(patient: Patient): String = DisplayText.score(patient.latestScore)
}
