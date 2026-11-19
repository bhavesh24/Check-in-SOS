package uk.checkinsos.app.feature.family.timeline

import uk.checkinsos.domain.model.TimelineSeverity

data class TimelineRowState(
    val id: String,
    val index: Int,
    val title: String,
    val time: String,
    val severity: TimelineSeverity,
) {
    val testTag: String get() = "timeline.event.$index"
}

data class TimelineUiState(
    val rows: List<TimelineRowState> = emptyList(),
    val isLoaded: Boolean = false,
    val errorMessage: String? = null,
) {
    val emptyText: String get() = "No events yet"
    val isEmpty: Boolean get() = isLoaded && rows.isEmpty()
}
