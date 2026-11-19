package uk.checkinsos.app.feature.neighbour.alive

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uk.checkinsos.app.designsystem.AppTheme
import uk.checkinsos.app.designsystem.Dimens
import uk.checkinsos.app.designsystem.FlowScreen
import uk.checkinsos.app.designsystem.MessageText
import uk.checkinsos.app.designsystem.PrimaryButton
import uk.checkinsos.app.designsystem.SecondaryButton
import uk.checkinsos.app.feature.shared.EventEffect

@Composable
fun AliveScreen(viewModel: AliveViewModel, onBack: () -> Unit, onDone: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    EventEffect(viewModel.events) { onDone() }
    AliveContent(state, onBack, onAnswer = viewModel::answer)
}

@Composable
fun AliveContent(state: AliveUiState, onBack: () -> Unit, onAnswer: (Boolean) -> Unit) {
    val colors = AppTheme.colors
    FlowScreen(
        title = "Help",
        onBack = onBack,
        step = state.step,
        totalSteps = state.totalSteps,
        errorMessage = state.errorMessage,
        actions = {
            SecondaryButton("No", "alive.no", onClick = { onAnswer(false) }, enabled = !state.isBusy)
            PrimaryButton("Yes", "alive.yes", onClick = { onAnswer(true) }, enabled = !state.isBusy)
        },
    ) {
        state.demoMessage?.let { demo ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surfaceTint, RoundedCornerShape(Dimens.cornerRadius))
                    .padding(Dimens.spacing)
                    .semantics(mergeDescendants = true) {}
                    .testTag("emergency.demoMessage"),
            ) {
                Icon(Icons.Filled.Phone, contentDescription = null, tint = colors.statusWarning)
                Text(demo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = colors.statusWarning)
            }
        }
        MessageText(state.heading, isHeading = true)
    }
}
