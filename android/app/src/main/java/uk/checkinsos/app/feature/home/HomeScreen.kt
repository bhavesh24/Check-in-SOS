package uk.checkinsos.app.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uk.checkinsos.app.designsystem.AppTheme
import uk.checkinsos.app.designsystem.FlowScreen
import uk.checkinsos.app.designsystem.PrimaryButton
import uk.checkinsos.app.feature.shared.EventEffect
import uk.checkinsos.domain.model.Role

@Composable
fun HomeScreen(viewModel: HomeViewModel, onRoleSelected: (Role) -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    EventEffect(viewModel.events) { onRoleSelected(it.role) }
    HomeContent(state, onSelect = viewModel::select)
}

@Composable
fun HomeContent(state: HomeUiState, onSelect: (Role) -> Unit) {
    FlowScreen(title = "Home", onBack = null) {
        Text(
            "Are you", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold,
            color = AppTheme.colors.text, modifier = Modifier.semantics { heading() },
        )
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            state.options.forEach { option ->
                PrimaryButton(option.title, option.testTag, onClick = { onSelect(option.role) })
            }
        }
    }
}
