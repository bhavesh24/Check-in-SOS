package uk.checkinsos.app.feature.olderadult.assessment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.Umbrella
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uk.checkinsos.app.designsystem.AppTheme
import uk.checkinsos.app.designsystem.ChoiceGrid
import uk.checkinsos.app.designsystem.ChoiceTile
import uk.checkinsos.app.designsystem.Dimens
import uk.checkinsos.app.designsystem.FlowScreen
import uk.checkinsos.app.designsystem.PrimaryButton
import uk.checkinsos.app.feature.shared.EventEffect
import uk.checkinsos.domain.model.AssessmentResult
import uk.checkinsos.domain.model.Mood
import uk.checkinsos.domain.model.PhysicalWellbeing

@Composable
fun AssessmentScreen(
    viewModel: AssessmentViewModel,
    onBack: () -> Unit,
    onSubmitted: (AssessmentResult) -> Unit,
    onMissedCheckIn: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    EventEffect(viewModel.events) {
        when (it) {
            is AssessmentViewModel.Event.Submitted -> onSubmitted(it.result)
            AssessmentViewModel.Event.MissedCheckIn -> onMissedCheckIn()
        }
    }
    AssessmentContent(
        state = state,
        onBack = onBack,
        onPhysical = viewModel::select,
        onMood = viewModel::select,
        onSubmit = viewModel::submit,
        onMissedCheckIn = viewModel::showMissedCheckIn,
    )
}

@Composable
fun AssessmentContent(
    state: AssessmentUiState,
    onBack: () -> Unit,
    onPhysical: (PhysicalWellbeing) -> Unit,
    onMood: (Mood) -> Unit,
    onSubmit: () -> Unit,
    onMissedCheckIn: () -> Unit,
) {
    FlowScreen(
        title = "Assessment",
        onBack = onBack,
        step = state.step,
        totalSteps = state.totalSteps,
        errorMessage = state.errorMessage,
        actions = { PrimaryButton("Submit", "assessment.submit", onClick = onSubmit, enabled = state.canSubmit) },
    ) {
        Section("How do you feel physically?") {
            ChoiceGrid(PhysicalWellbeing.entries) { option, modifier ->
                ChoiceTile(option.title, option.icon, state.physical == option, option.testTag, { onPhysical(option) }, modifier)
            }
        }
        Section("How is your mood?") {
            ChoiceGrid(Mood.entries) { option, modifier ->
                ChoiceTile(option.title, option.icon, state.mood == option, option.testTag, { onMood(option) }, modifier)
            }
        }
        TextButton(
            onClick = onMissedCheckIn,
            modifier = Modifier.heightIn(min = Dimens.minTouchTarget).testTag("assessment.demoNoCheckIn"),
        ) {
            Text("Demo: missed check-in", style = MaterialTheme.typography.titleMedium, color = AppTheme.colors.primary)
        }
    }
}

@Composable
private fun Section(heading: String, options: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.selectableGroup()) {
        Text(
            heading, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold,
            color = AppTheme.colors.text, modifier = Modifier.semantics { heading() },
        )
        options()
    }
}

private val PhysicalWellbeing.icon: ImageVector
    get() = when (this) {
        PhysicalWellbeing.FINE -> Icons.Filled.ThumbUp
        PhysicalWellbeing.GOOD -> Icons.Filled.Star
        PhysicalWellbeing.ILL -> Icons.Filled.Thermostat
        PhysicalWellbeing.VERY_SICK -> Icons.Filled.Bed
    }

private val Mood.icon: ImageVector
    get() = when (this) {
        Mood.HAPPY -> Icons.Filled.WbSunny
        Mood.PLEASANT -> Icons.Filled.WbCloudy
        Mood.ANXIOUS -> Icons.Filled.Thunderstorm
        Mood.DEPRESSED -> Icons.Filled.Umbrella
    }
