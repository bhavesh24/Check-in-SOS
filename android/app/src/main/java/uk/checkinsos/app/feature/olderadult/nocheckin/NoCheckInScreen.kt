package uk.checkinsos.app.feature.olderadult.nocheckin

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uk.checkinsos.app.designsystem.AppTheme
import uk.checkinsos.app.designsystem.FlowScreen
import uk.checkinsos.app.designsystem.MessageText

@Composable
fun NoCheckInScreen(viewModel: NoCheckInViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    NoCheckInContent(state, onBack)
}

@Composable
fun NoCheckInContent(state: NoCheckInUiState, onBack: () -> Unit) {
    FlowScreen(title = "No Check-in", onBack = onBack) {
        Icon(
            Icons.Filled.Notifications, contentDescription = null, tint = AppTheme.colors.statusWarning,
            modifier = Modifier.fillMaxWidth().wrapContentWidth().size(56.dp),
        )
        MessageText(state.message, testTag = "noCheckIn.message")
    }
}
