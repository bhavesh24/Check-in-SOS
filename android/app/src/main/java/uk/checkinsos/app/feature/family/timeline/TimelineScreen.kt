package uk.checkinsos.app.feature.family.timeline

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uk.checkinsos.app.designsystem.AppTheme
import uk.checkinsos.app.designsystem.FlowScreen
import uk.checkinsos.app.designsystem.MessageText
import uk.checkinsos.app.designsystem.TimelineRow

@Composable
fun TimelineScreen(viewModel: TimelineViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.load() }
    TimelineContent(state, onBack)
}

@Composable
fun TimelineContent(state: TimelineUiState, onBack: () -> Unit) {
    FlowScreen(title = "Timeline", onBack = onBack, errorMessage = state.errorMessage) {
        if (state.isEmpty) MessageText(state.emptyText, testTag = "timeline.empty")
        Column {
            state.rows.forEach { row ->
                TimelineRow(row.title, row.time, row.severity, row.testTag)
                if (row.index < state.rows.lastIndex) {
                    Spacer(
                        Modifier.padding(start = 10.dp).width(2.dp).height(16.dp)
                            .background(AppTheme.colors.disabledBackground),
                    )
                }
            }
        }
    }
}
