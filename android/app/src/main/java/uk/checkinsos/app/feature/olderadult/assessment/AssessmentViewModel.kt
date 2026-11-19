package uk.checkinsos.app.feature.olderadult.assessment

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import uk.checkinsos.app.feature.shared.DisplayText
import uk.checkinsos.app.feature.shared.FlowViewModel
import uk.checkinsos.domain.model.Assessment
import uk.checkinsos.domain.model.AssessmentResult
import uk.checkinsos.domain.model.Mood
import uk.checkinsos.domain.model.PhysicalWellbeing
import uk.checkinsos.domain.usecase.SubmitAssessment

/** Daily check-in: one physical answer + one mood answer, then [SubmitAssessment]. */
class AssessmentViewModel(
    private val submitAssessment: SubmitAssessment,
    private val olderAdultId: String,
) : FlowViewModel<AssessmentUiState, AssessmentViewModel.Event>(AssessmentUiState()) {

    sealed interface Event {
        data class Submitted(val result: AssessmentResult) : Event
        data object MissedCheckIn : Event
    }

    fun select(physical: PhysicalWellbeing) = setState { it.copy(physical = physical) }
    fun select(mood: Mood) = setState { it.copy(mood = mood) }

    fun submit() {
        val state = currentState
        val physical = state.physical ?: return
        val mood = state.mood ?: return
        if (state.isSubmitting) return
        setState { it.copy(isSubmitting = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                val result = submitAssessment(olderAdultId, Assessment(physical, mood))
                setState { it.copy(isSubmitting = false) }
                send(Event.Submitted(result))
            } catch (e: Exception) {
                setState { it.copy(isSubmitting = false, errorMessage = DisplayText.GENERIC_ERROR) }
            }
        }
    }

    fun showMissedCheckIn() = send(Event.MissedCheckIn)
}
