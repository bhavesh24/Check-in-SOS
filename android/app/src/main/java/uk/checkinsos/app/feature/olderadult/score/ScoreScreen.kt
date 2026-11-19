package uk.checkinsos.app.feature.olderadult.score

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uk.checkinsos.app.designsystem.FlowScreen
import uk.checkinsos.app.designsystem.MessageText
import uk.checkinsos.app.designsystem.PrimaryButton
import uk.checkinsos.app.designsystem.ScoreNumeral
import uk.checkinsos.app.feature.shared.EventEffect

@Composable
fun ScoreScreen(viewModel: ScoreViewModel, onBack: () -> Unit, onDone: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    EventEffect(viewModel.events) { onDone() }
    ScoreContent(state, onBack = onBack, onDone = viewModel::done)
}

@Composable
fun ScoreContent(state: ScoreUiState, onBack: () -> Unit, onDone: () -> Unit) {
    FlowScreen(
        title = "Score",
        onBack = onBack,
        step = state.step,
        totalSteps = state.totalSteps,
        actions = { PrimaryButton("Done", "score.done", onClick = onDone) },
    ) {
        ScoreNumeral(state.score, "score.value", Modifier.padding(vertical = 24.dp))
        if (state.neighbourNotified) {
            MessageText(
                state.notificationText,
                Modifier.fillMaxWidth(),
                testTag = "score.neighbourNotified",
            )
        }
    }
}
