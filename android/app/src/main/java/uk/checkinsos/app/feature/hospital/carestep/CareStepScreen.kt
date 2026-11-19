package uk.checkinsos.app.feature.hospital.carestep

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uk.checkinsos.app.designsystem.FlowScreen
import uk.checkinsos.app.designsystem.MessageText
import uk.checkinsos.app.designsystem.PrimaryButton
import uk.checkinsos.app.designsystem.SecondaryButton
import uk.checkinsos.app.feature.shared.AlertSummary
import uk.checkinsos.app.feature.shared.EventEffect

/** In care / Recovering / Recovered / Bad condition / Patient alive. */
@Composable
fun CareStepScreen(viewModel: CareStepViewModel, onBack: () -> Unit, onNext: (CareStep?, AlertSummary) -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    EventEffect(viewModel.events) { onNext(it.next, it.alert) }
    CareStepContent(state, onBack, onChoose = viewModel::choose)
}

@Composable
fun CareStepContent(state: CareStepUiState, onBack: () -> Unit, onChoose: (CareStep.Answer) -> Unit) {
    val answers = state.step.answers
    FlowScreen(
        title = "Help",
        onBack = onBack,
        step = state.step.stepNumber,
        totalSteps = state.totalSteps,
        errorMessage = state.errorMessage,
        actions = {
            // The last answer is the filled primary button; earlier ones are outlined (No / Yes on the prototype).
            answers.forEachIndexed { index, answer ->
                if (index == answers.lastIndex) {
                    PrimaryButton(answer.title, answer.testTag, onClick = { onChoose(answer) }, enabled = !state.isBusy)
                } else {
                    SecondaryButton(answer.title, answer.testTag, onClick = { onChoose(answer) }, enabled = !state.isBusy)
                }
            }
        },
    ) {
        MessageText(state.step.heading, isHeading = true)
    }
}
