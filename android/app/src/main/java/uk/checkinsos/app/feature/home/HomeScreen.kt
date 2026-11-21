package uk.checkinsos.app.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uk.checkinsos.app.designsystem.AppTheme
import uk.checkinsos.app.designsystem.FlowScreen
import uk.checkinsos.app.designsystem.RoleCard
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
                RoleCard(
                    title = option.title,
                    description = option.description,
                    icon = option.role.icon,
                    tint = AppTheme.colors.forRole(option.role),
                    testTag = option.testTag,
                    onClick = { onSelect(option.role) },
                )
            }
        }
    }
}

private val Role.icon: ImageVector
    get() = when (this) {
        Role.OLDER_ADULT -> Icons.Filled.HowToReg
        Role.FAMILY -> Icons.Filled.Groups
        Role.HOSPITAL -> Icons.Filled.LocalHospital
        Role.NEIGHBOUR -> Icons.Filled.Home
    }
