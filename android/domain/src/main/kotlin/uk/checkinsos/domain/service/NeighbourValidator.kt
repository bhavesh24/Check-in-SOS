package uk.checkinsos.domain.service

import uk.checkinsos.domain.model.Neighbour
import uk.checkinsos.domain.model.NeighbourValidationError

/** Validation rules for the Edit neighbour form. */
object NeighbourValidator {
    private val EMAIL = Regex("""[^@\s]+@[^@\s]+\.[^@\s]+""")

    sealed interface Result {
        data class Valid(val neighbour: Neighbour) : Result
        data class Invalid(val error: NeighbourValidationError) : Result
    }

    /** Returns a trimmed copy of [neighbour], or the first rule it breaks. */
    fun validate(neighbour: Neighbour): Result {
        val trimmed = neighbour.copy(
            firstName = neighbour.firstName.trim(),
            middleName = neighbour.middleName.trim(),
            lastName = neighbour.lastName.trim(),
            email = neighbour.email.trim(),
            mobile = neighbour.mobile.trim(),
        )
        val error = when {
            trimmed.firstName.isEmpty() -> NeighbourValidationError.FIRST_NAME_REQUIRED
            trimmed.lastName.isEmpty() -> NeighbourValidationError.LAST_NAME_REQUIRED
            !isValidEmail(trimmed.email) -> NeighbourValidationError.INVALID_EMAIL
            !isValidMobile(trimmed.mobile) -> NeighbourValidationError.INVALID_MOBILE
            else -> return Result.Valid(trimmed)
        }
        return Result.Invalid(error)
    }

    internal fun isValidEmail(email: String): Boolean = EMAIL.matches(email)

    /** Spaces/dashes removed, then a leading "+44" or "0" removed; 9–10 digits must remain. */
    internal fun isValidMobile(mobile: String): Boolean {
        var digits = mobile.filter { it != ' ' && it != '-' }
        digits = when {
            digits.startsWith("+44") -> digits.drop(3)
            digits.startsWith("0") -> digits.drop(1)
            else -> digits
        }
        return digits.length in 9..10 && digits.all { it in '0'..'9' }
    }
}
