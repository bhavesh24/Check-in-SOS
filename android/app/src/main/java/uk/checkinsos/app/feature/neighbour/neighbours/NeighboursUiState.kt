package uk.checkinsos.app.feature.neighbour.neighbours

import uk.checkinsos.domain.model.OlderAdult

data class NeighboursUiState(
    val neighbours: List<OlderAdult> = emptyList(),
    val isBusy: Boolean = false,
    val errorMessage: String? = null,
)
