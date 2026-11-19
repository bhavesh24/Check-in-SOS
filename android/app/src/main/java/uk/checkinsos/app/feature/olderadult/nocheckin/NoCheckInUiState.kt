package uk.checkinsos.app.feature.olderadult.nocheckin

/** Shown when the older adult has not checked in by the threshold time. Static copy; back only. */
data class NoCheckInUiState(
    val message: String = "As you have not checked in, a notification has been sent to your neighbour",
)
