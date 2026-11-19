package uk.checkinsos.app.feature.home

import uk.checkinsos.app.feature.shared.FlowViewModel
import uk.checkinsos.domain.model.Role

/** Role picker — the demo entry point. */
class HomeViewModel : FlowViewModel<HomeUiState, HomeViewModel.Event>(
    HomeUiState(Role.entries.map { HomeOption(it, title(it)) }),
) {
    data class Event(val role: Role)

    fun select(role: Role) = send(Event(role))

    companion object {
        fun title(role: Role): String = when (role) {
            Role.OLDER_ADULT -> "Old age adult"
            Role.FAMILY -> "Concerned family"
            Role.HOSPITAL -> "Hospital"
            Role.NEIGHBOUR -> "Neighbour"
        }
    }
}
