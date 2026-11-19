package uk.checkinsos.app.feature.family.timeline

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import uk.checkinsos.app.feature.shared.DisplayText
import uk.checkinsos.app.feature.shared.FlowViewModel
import uk.checkinsos.domain.usecase.GetTimeline
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class TimelineViewModel(
    private val olderAdultId: String,
    private val getTimeline: GetTimeline,
    zone: ZoneId,
) : FlowViewModel<TimelineUiState, Nothing>(TimelineUiState()) {

    private val formatter = DateTimeFormatter.ofPattern("HH:mm", Locale.UK).withZone(zone)

    fun load() {
        viewModelScope.launch {
            try {
                val rows = getTimeline(olderAdultId).mapIndexed { index, event ->
                    TimelineRowState(event.id, index, event.kind.title, formatter.format(event.date), event.kind.severity)
                }
                setState { it.copy(rows = rows, isLoaded = true, errorMessage = null) }
            } catch (e: Exception) {
                setState { it.copy(errorMessage = DisplayText.GENERIC_ERROR) }
            }
        }
    }
}
