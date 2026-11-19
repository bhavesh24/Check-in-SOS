package uk.checkinsos.app.feature.home

import uk.checkinsos.app.feature.shared.camelName
import uk.checkinsos.domain.model.Role

data class HomeOption(val role: Role, val title: String) {
    val testTag: String get() = "home.role.${role.camelName}"
}

/** Role picker options, in the order shown. */
data class HomeUiState(val options: List<HomeOption>)
