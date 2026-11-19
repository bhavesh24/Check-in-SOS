package uk.checkinsos.app.feature.family.editneighbour

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uk.checkinsos.app.designsystem.AppTheme
import uk.checkinsos.app.designsystem.ErrorBanner
import uk.checkinsos.app.designsystem.FlowScreen
import uk.checkinsos.app.designsystem.PrimaryButton
import uk.checkinsos.app.feature.shared.EventEffect

/** Callbacks for each form field. */
data class EditNeighbourActions(
    val onFirstName: (String) -> Unit,
    val onMiddleName: (String) -> Unit,
    val onLastName: (String) -> Unit,
    val onEmail: (String) -> Unit,
    val onMobile: (String) -> Unit,
    val onSave: () -> Unit,
)

@Composable
fun EditNeighbourScreen(viewModel: EditNeighbourViewModel, onBack: () -> Unit, onSaved: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.load() }
    EventEffect(viewModel.events) { onSaved() }
    EditNeighbourContent(
        state, onBack,
        EditNeighbourActions(
            viewModel::onFirstName, viewModel::onMiddleName, viewModel::onLastName,
            viewModel::onEmail, viewModel::onMobile, viewModel::save,
        ),
    )
}

@Composable
fun EditNeighbourContent(state: EditNeighbourUiState, onBack: () -> Unit, actions: EditNeighbourActions) {
    FlowScreen(
        title = "Edit Neighbour",
        onBack = onBack,
        actions = { PrimaryButton("Save", "editNeighbour.save", onClick = actions.onSave, enabled = !state.isSaving) },
    ) {
        Text(
            state.heading, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold,
            color = AppTheme.colors.text, modifier = Modifier.semantics { heading() },
        )
        Field("First name*", state.firstName, "editNeighbour.firstName", actions.onFirstName)
        Field("Middle name", state.middleName, "editNeighbour.middleName", actions.onMiddleName)
        Field("Last name*", state.lastName, "editNeighbour.lastName", actions.onLastName)
        Field("Email*", state.email, "editNeighbour.email", actions.onEmail, KeyboardType.Email)
        Field(
            "Mobile*", state.mobile, "editNeighbour.mobile", actions.onMobile, KeyboardType.Phone,
            prefix = state.mobilePrefix, imeAction = ImeAction.Done,
        )
        state.errorMessage?.let { ErrorBanner(it, testTag = "editNeighbour.error") }
    }
}

@Composable
private fun Field(
    label: String,
    value: String,
    testTag: String,
    onChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    prefix: String? = null,
    imeAction: ImeAction = ImeAction.Next,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        prefix = prefix?.let { { Text("$it ") } },
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction,
            capitalization = if (keyboardType == KeyboardType.Text) KeyboardCapitalization.Words else KeyboardCapitalization.None,
            autoCorrectEnabled = false,
        ),
        modifier = Modifier.fillMaxWidth().testTag(testTag),
    )
}
