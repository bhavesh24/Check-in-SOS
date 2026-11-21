package uk.checkinsos.app.feature.neighbour.neighbours

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uk.checkinsos.app.designsystem.FlowScreen
import uk.checkinsos.app.designsystem.GroupedList
import uk.checkinsos.app.designsystem.PersonRow
import uk.checkinsos.app.designsystem.PrimaryButton
import uk.checkinsos.app.feature.shared.AlertSummary
import uk.checkinsos.app.feature.shared.EventEffect

@Composable
fun NeighboursScreen(viewModel: NeighboursViewModel, onBack: () -> Unit, onAlert: (AlertSummary) -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.load() }
    EventEffect(viewModel.events) { onAlert(it.alert) }
    NeighboursContent(state, onBack, onSimulate = viewModel::simulate)
}

@Composable
fun NeighboursContent(state: NeighboursUiState, onBack: () -> Unit, onSimulate: () -> Unit) {
    FlowScreen(
        title = "Neighbours",
        onBack = onBack,
        errorMessage = state.errorMessage,
        actions = { PrimaryButton("Simulate alert", "neighbours.simulateAlert", onClick = onSimulate, enabled = !state.isBusy) },
    ) {
        GroupedList(state.neighbours) { adult ->
            PersonRow(adult.name, adult.latestScore, "neighbours.row.${adult.id}")
        }
    }
}
