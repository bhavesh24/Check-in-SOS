package uk.checkinsos.app.feature.neighbour.escalate

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uk.checkinsos.app.designsystem.AppTheme
import uk.checkinsos.app.designsystem.FlowScreen
import uk.checkinsos.app.designsystem.MessageText
import uk.checkinsos.app.designsystem.PrimaryButton
import uk.checkinsos.app.designsystem.SecondaryButton
import uk.checkinsos.app.feature.shared.AlertSummary
import uk.checkinsos.app.feature.shared.EventEffect

/** Callbacks for the escalate step and its emergency dialog. */
data class EscalateActions(
    val onAllOk: () -> Unit,
    val onAlertServices: () -> Unit,
    val onCall: (String) -> Unit,
    val onCancel: () -> Unit,
)

@Composable
fun EscalateScreen(
    viewModel: EscalateViewModel,
    onBack: () -> Unit,
    onAllOk: () -> Unit,
    onCalled: (AlertSummary, String) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    EventEffect(viewModel.events) {
        when (it) {
            EscalateViewModel.Event.AllOk -> onAllOk()
            is EscalateViewModel.Event.Called -> onCalled(it.alert, it.number)
        }
    }
    EscalateContent(state, onBack, EscalateActions(viewModel::allOk, viewModel::alertServices, viewModel::call, viewModel::cancel))
}

@Composable
fun EscalateContent(state: EscalateUiState, onBack: () -> Unit, actions: EscalateActions) {
    FlowScreen(
        title = "Escalate",
        onBack = onBack,
        step = state.step,
        totalSteps = state.totalSteps,
        errorMessage = state.errorMessage,
        actions = {
            SecondaryButton("No, All OK!", "escalate.allOk", onClick = actions.onAllOk, enabled = !state.isBusy)
            PrimaryButton(
                "Yes, Alert emergency services", "escalate.alertServices",
                onClick = actions.onAlertServices, enabled = !state.isBusy,
            )
        },
    ) {
        MessageText(state.heading, isHeading = true)
    }
    if (state.isEmergencyDialogShown) {
        EmergencyDialog(state.emergencyNumbers, actions)
    }
}

/** Material dialog standing in for the iOS action sheet. 999/111 never dial. */
@Composable
private fun EmergencyDialog(numbers: List<String>, actions: EscalateActions) {
    val colors = AppTheme.colors
    AlertDialog(
        onDismissRequest = actions.onCancel,
        title = { Text("Alert emergency services") },
        text = { Text("Do you want to call") },
        confirmButton = {
            numbers.forEach { number ->
                TextButton(onClick = { actions.onCall(number) }, modifier = Modifier.testTag("emergency.call$number")) {
                    Text(number, color = colors.primary)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = actions.onCancel, modifier = Modifier.testTag("emergency.cancel")) {
                Text("Cancel", color = colors.primary)
            }
        },
        containerColor = colors.background,
        titleContentColor = colors.text,
        textContentColor = colors.text,
    )
}
