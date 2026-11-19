package uk.checkinsos.app.feature.neighbour.neighbours

import uk.checkinsos.app.feature.shared.DisplayText
import uk.checkinsos.domain.model.OlderAdult

data class NeighboursUiState(
    val neighbours: List<OlderAdult> = emptyList(),
    val isBusy: Boolean = false,
    val errorMessage: String? = null,
) {
    fun subtitle(adult: OlderAdult): String = DisplayText.score(adult.latestScore)
}
