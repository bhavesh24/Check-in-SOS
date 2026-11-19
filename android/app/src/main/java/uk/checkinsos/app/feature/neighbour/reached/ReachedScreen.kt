package uk.checkinsos.app.feature.neighbour.reached

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uk.checkinsos.app.designsystem.FlowScreen
import uk.checkinsos.app.designsystem.MessageText
import uk.checkinsos.app.designsystem.PrimaryButton
import uk.checkinsos.app.feature.shared.AlertSummary
import uk.checkinsos.app.feature.shared.EventEffect

@Composable
fun ReachedScreen(viewModel: ReachedViewModel, onBack: () -> Unit, onReached: (AlertSummary) -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    EventEffect(viewModel.events) { onReached(it.alert) }
    ReachedContent(state, onBack, onConfirm = viewModel::confirm)
}

@Composable
fun ReachedContent(state: ReachedUiState, onBack: () -> Unit, onConfirm: () -> Unit) {
    FlowScreen(
        title = "Help",
        onBack = onBack,
        step = state.step,
        totalSteps = state.totalSteps,
        errorMessage = state.errorMessage,
        actions = { PrimaryButton("I have reached", "reached.confirm", onClick = onConfirm, enabled = !state.isBusy) },
    ) {
        MessageText(state.message, testTag = "reached.message")
    }
}
