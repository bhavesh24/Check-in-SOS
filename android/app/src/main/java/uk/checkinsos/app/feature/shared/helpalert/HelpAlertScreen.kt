package uk.checkinsos.app.feature.shared.helpalert

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import uk.checkinsos.app.designsystem.CountdownRing
import uk.checkinsos.app.designsystem.FlowScreen
import uk.checkinsos.app.designsystem.MessageText
import uk.checkinsos.app.designsystem.PrimaryButton
import uk.checkinsos.app.designsystem.UrgentBanner
import uk.checkinsos.app.feature.shared.AlertSummary
import uk.checkinsos.app.feature.shared.EventEffect

@Composable
fun HelpAlertScreen(viewModel: HelpAlertViewModel, onBack: () -> Unit, onAccepted: (AlertSummary) -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        while (true) {
            viewModel.tick()
            delay(1_000)
        }
    }
    EventEffect(viewModel.events) { onAccepted(it.alert) }
    HelpAlertContent(state, onBack, onAccept = viewModel::accept)
}

@Composable
fun HelpAlertContent(state: HelpAlertUiState, onBack: () -> Unit, onAccept: () -> Unit) {
    FlowScreen(
        title = state.title,
        onBack = onBack,
        step = state.step,
        totalSteps = state.totalSteps,
        errorMessage = state.errorMessage,
        actions = { PrimaryButton("Accept", "${state.tagPrefix}.accept", onClick = onAccept, enabled = !state.isBusy) },
    ) {
        UrgentBanner(state.bannerText)
        MessageText(state.message, testTag = "${state.tagPrefix}.message")
        CountdownRing(state.countdownText, state.countdownProgress, state.countdownCaption, "${state.tagPrefix}.countdown")
    }
}
