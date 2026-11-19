package uk.checkinsos.app.feature.hospital.carestep

import uk.checkinsos.app.feature.shared.camelName
import uk.checkinsos.domain.model.CareOutcome

/** The hospital's question steps after the alert (table in docs/screens.md). */
enum class CareStep {
    IN_CARE, RECOVERING, RECOVERED, BAD_CONDITION, PATIENT_ALIVE;

    /** [outcome] null records nothing; [next] null returns to the Patients list. */
    data class Answer(val title: String, val testTag: String, val outcome: CareOutcome?, val next: CareStep?)

    val heading: String
        get() = when (this) {
            IN_CARE -> "Have you taken them in your care?"
            RECOVERING -> "Are they recovering?"
            RECOVERED -> "Have they recovered?"
            BAD_CONDITION -> "Are they in bad condition?"
            PATIENT_ALIVE -> "Is your patient alive?"
        }

    /** Position in the 5-step hospital flow (the alert is step 1). */
    val stepNumber: Int
        get() = when (this) {
            IN_CARE -> 2
            RECOVERING -> 3
            RECOVERED, BAD_CONDITION -> 4
            PATIENT_ALIVE -> 5
        }

    val answers: List<Answer>
        get() = when (this) {
            IN_CARE -> listOf(answer("Yes", CareOutcome.IN_CARE, RECOVERING))
            RECOVERING -> listOf(answer("Yes", CareOutcome.RECOVERING, RECOVERED), answer("No", null, BAD_CONDITION))
            RECOVERED -> listOf(answer("Yes", CareOutcome.RECOVERED, null))
            BAD_CONDITION -> listOf(answer("Yes", CareOutcome.BAD_CONDITION, PATIENT_ALIVE), answer("No", null, null))
            PATIENT_ALIVE -> listOf(answer("No", CareOutcome.DECEASED, null), answer("Yes", CareOutcome.ALIVE, null))
        }

    private fun answer(title: String, outcome: CareOutcome?, next: CareStep?) =
        Answer(title, "$camelName.${title.lowercase()}", outcome, next)
}
