package uk.checkinsos.app.feature.family.editneighbour

/**
 * Edit the 1st-level notified person. [mobile] is shown after a fixed "+44" prefix, so it holds the
 * national number without its leading "0" (docs/screens.md).
 */
data class EditNeighbourUiState(
    val firstName: String = "",
    val middleName: String = "",
    val lastName: String = "",
    val email: String = "",
    val mobile: String = "",
    val errorMessage: String? = null,
    val isSaving: Boolean = false,
    val isLoaded: Boolean = false,
) {
    val heading: String get() = "Your 1st level notified person"
    val mobilePrefix: String get() = "+44"
}
