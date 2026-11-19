package uk.checkinsos.domain.model

sealed class DomainException(message: String) : Exception(message) {
    data class NotFound(val id: String) : DomainException("Not found: $id")
    data object NoAlertSubject : DomainException("Nobody to raise an alert about") {
        private fun readResolve(): Any = NoAlertSubject
    }
    data class InvalidNeighbour(val error: NeighbourValidationError) : DomainException(error.message)
}

/** [message] is the copy shown in `editNeighbour.error`. */
enum class NeighbourValidationError(val message: String) {
    FIRST_NAME_REQUIRED("First name is required"),
    LAST_NAME_REQUIRED("Last name is required"),
    INVALID_EMAIL("Enter a valid email"),
    INVALID_MOBILE("Enter a valid UK mobile number"),
}
