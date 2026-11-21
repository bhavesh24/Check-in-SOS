package uk.checkinsos.app.feature.family.status

import uk.checkinsos.app.feature.shared.ScoreBand
import uk.checkinsos.domain.model.OlderAdult
import uk.checkinsos.domain.model.ScoreRecord

/** Current status of one loved one: latest score, last check-in, recent scores and the 1st-level neighbour. */
data class StatusUiState(
    val adult: OlderAdult? = null,
    val history: List<ScoreRecord> = emptyList(),
    /** "Last check-in Today, 08:00" / "No check-in yet", worked out against the clock at load. */
    val lastCheckInText: String = "",
    val errorMessage: String? = null,
) {
    val title: String get() = adult?.name.orEmpty()
    val neighbourText: String get() = adult?.let { "1st level: ${it.neighbour.displayName}" }.orEmpty()
    val neighbourName: String get() = adult?.neighbour?.displayName.orEmpty()
    val scoreCaption: String get() = "Current score"
    val trendTitle: String get() = "Recent check-ins"
    val neighbourCaption: String get() = "1st level neighbour"
    val band: ScoreBand? get() = adult?.latestScore?.let(ScoreBand::of)

    /** The chart needs at least two points to show a trend. */
    val showsTrend: Boolean get() = history.size >= 2
    val trendAccessibilityText: String get() = "$trendTitle: " + history.joinToString(", ") { it.score.toString() }
}
