package uk.checkinsos.app.feature.hospital.patients

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uk.checkinsos.app.designsystem.FlowScreen
import uk.checkinsos.app.designsystem.PersonRow
import uk.checkinsos.app.designsystem.PrimaryButton
import uk.checkinsos.app.feature.shared.AlertSummary
import uk.checkinsos.app.feature.shared.EventEffect

@Composable
fun PatientsScreen(viewModel: PatientsViewModel, onBack: () -> Unit, onAlert: (AlertSummary) -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.load() }
    EventEffect(viewModel.events) { onAlert(it.alert) }
    PatientsContent(state, onBack, onSimulate = viewModel::simulate)
}

@Composable
fun PatientsContent(state: PatientsUiState, onBack: () -> Unit, onSimulate: () -> Unit) {
    FlowScreen(
        title = "Patients",
        onBack = onBack,
        errorMessage = state.errorMessage,
        actions = { PrimaryButton("Simulate alert", "patients.simulateAlert", onClick = onSimulate, enabled = !state.isBusy) },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            state.patients.forEach { patient ->
                PersonRow(patient.name, state.subtitle(patient), "patients.row.${patient.id}")
            }
        }
    }
}
