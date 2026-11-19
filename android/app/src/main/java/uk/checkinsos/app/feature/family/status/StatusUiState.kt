package uk.checkinsos.app.feature.family.status

import uk.checkinsos.domain.model.OlderAdult

/** Current status of one loved one: latest score + 1st-level neighbour. */
data class StatusUiState(
    val adult: OlderAdult? = null,
    val errorMessage: String? = null,
) {
    val title: String get() = adult?.name.orEmpty()
    val neighbourText: String get() = adult?.let { "1st level: ${it.neighbour.displayName}" }.orEmpty()
    val scoreCaption: String get() = "Current score"
}
