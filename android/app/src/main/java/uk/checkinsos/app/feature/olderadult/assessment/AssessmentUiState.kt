package uk.checkinsos.app.feature.olderadult.assessment

import uk.checkinsos.app.feature.shared.FlowSteps
import uk.checkinsos.app.feature.shared.camelName
import uk.checkinsos.domain.model.Mood
import uk.checkinsos.domain.model.PhysicalWellbeing

data class AssessmentUiState(
    val physical: PhysicalWellbeing? = null,
    val mood: Mood? = null,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
) {
    val step: Int get() = 1
    val totalSteps: Int get() = FlowSteps.OLDER_ADULT
    val canSubmit: Boolean get() = physical != null && mood != null && !isSubmitting
}

val PhysicalWellbeing.title: String
    get() = when (this) {
        PhysicalWellbeing.FINE -> "Fine"
        PhysicalWellbeing.GOOD -> "Good"
        PhysicalWellbeing.ILL -> "Ill"
        PhysicalWellbeing.VERY_SICK -> "Very sick"
    }

val PhysicalWellbeing.testTag: String get() = "assessment.physical.$camelName"

val Mood.title: String
    get() = when (this) {
        Mood.HAPPY -> "Happy"
        Mood.PLEASANT -> "Pleasant"
        Mood.ANXIOUS -> "Anxious"
        Mood.DEPRESSED -> "Depressed"
    }

val Mood.testTag: String get() = "assessment.mood.$camelName"
