package uk.checkinsos.app.feature.family.lovedones

import uk.checkinsos.app.feature.shared.DisplayText
import uk.checkinsos.domain.model.OlderAdult

data class LovedOnesUiState(
    val lovedOnes: List<OlderAdult> = emptyList(),
    val errorMessage: String? = null,
) {
    fun subtitle(adult: OlderAdult): String = DisplayText.score(adult.latestScore)
}
