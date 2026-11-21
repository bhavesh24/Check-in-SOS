package uk.checkinsos.app.feature.family.status

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uk.checkinsos.app.designsystem.AppTheme
import uk.checkinsos.app.designsystem.Dimens
import uk.checkinsos.app.designsystem.FlowScreen
import uk.checkinsos.app.designsystem.InitialsAvatar
import uk.checkinsos.app.designsystem.ScoreNumeral
import uk.checkinsos.app.designsystem.ScoreRing
import uk.checkinsos.app.designsystem.ScoreTrendChart
import uk.checkinsos.app.designsystem.SecondaryButton
import uk.checkinsos.app.feature.shared.EventEffect

@Composable
fun StatusScreen(
    viewModel: StatusViewModel,
    onBack: () -> Unit,
    onEditNeighbour: (String) -> Unit,
    onTimeline: (String) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.load() }
    EventEffect(viewModel.events) {
        when (it) {
            is StatusViewModel.Event.EditNeighbour -> onEditNeighbour(it.olderAdultId)
            is StatusViewModel.Event.Timeline -> onTimeline(it.olderAdultId)
        }
    }
    StatusContent(state, onBack, onEditNeighbour = viewModel::editNeighbour, onTimeline = viewModel::openTimeline)
}

@Composable
fun StatusContent(state: StatusUiState, onBack: () -> Unit, onEditNeighbour: () -> Unit, onTimeline: () -> Unit) {
    val colors = AppTheme.colors
    FlowScreen(
        title = state.title,
        onBack = onBack,
        errorMessage = state.errorMessage,
        actions = { SecondaryButton("Timeline", "status.timeline", onClick = onTimeline) },
    ) {
        val adult = state.adult ?: return@FlowScreen
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(state.scoreCaption, style = MaterialTheme.typography.titleMedium, color = colors.secondaryText)
            val score = adult.latestScore
            val band = state.band
            if (score != null && band != null) ScoreRing(score, band, "status.score", ringSize = 160.dp) else ScoreNumeral(null, "status.score")
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.semantics(mergeDescendants = true) {}.testTag("status.lastCheckIn"),
            ) {
                Icon(Icons.Filled.Schedule, contentDescription = null, tint = colors.secondaryText, modifier = Modifier.size(18.dp))
                Text(state.lastCheckInText, style = MaterialTheme.typography.bodyMedium, color = colors.secondaryText)
            }
        }
        Section(state.neighbourCaption) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surfaceTint, RoundedCornerShape(Dimens.cornerRadius + 4.dp))
                    .padding(start = Dimens.spacing, end = 4.dp, top = 8.dp, bottom = 8.dp),
            ) {
                InitialsAvatar(state.neighbourName)
                Text(
                    state.neighbourText, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold,
                    color = colors.text, modifier = Modifier.weight(1f).testTag("status.neighbour"),
                )
                IconButton(onClick = onEditNeighbour, modifier = Modifier.testTag("status.editNeighbour")) {
                    Icon(Icons.Filled.Edit, contentDescription = "Edit neighbour", tint = colors.primary)
                }
            }
        }
        if (state.showsTrend) {
            Section(state.trendTitle) {
                ScoreTrendChart(
                    state.history, state.trendAccessibilityText, "status.trend",
                    Modifier.background(colors.surfaceTint, RoundedCornerShape(Dimens.cornerRadius + 4.dp)).padding(Dimens.spacing),
                )
            }
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold,
            color = AppTheme.colors.text, modifier = Modifier.semantics { heading() },
        )
        content()
    }
}
