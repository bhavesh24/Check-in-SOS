package uk.checkinsos.app.feature.family.timeline

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
                TimelineRow(
                    row.title, row.time, row.kind, row.testTag,
                    isFirst = row.index == 0, isLast = row.index == state.rows.lastIndex,
                )
            }
        }
    }
}
