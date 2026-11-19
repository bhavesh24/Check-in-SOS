package uk.checkinsos.app.feature.family.lovedones

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uk.checkinsos.app.designsystem.FlowScreen
import uk.checkinsos.app.designsystem.PersonRow
import uk.checkinsos.app.feature.shared.EventEffect

@Composable
fun LovedOnesScreen(viewModel: LovedOnesViewModel, onBack: () -> Unit, onSelect: (String) -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.load() }
    EventEffect(viewModel.events) { onSelect(it.olderAdultId) }
    LovedOnesContent(state, onBack, onSelect = viewModel::select)
}

@Composable
fun LovedOnesContent(state: LovedOnesUiState, onBack: () -> Unit, onSelect: (String) -> Unit) {
    FlowScreen(title = "Your loved ones", onBack = onBack, errorMessage = state.errorMessage) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            state.lovedOnes.forEach { adult ->
                PersonRow(
                    name = adult.name,
                    subtitle = state.subtitle(adult),
                    testTag = "lovedOnes.row.${adult.id}",
                    onClick = { onSelect(adult.id) },
                )
            }
        }
    }
}
