package uk.checkinsos.app.feature.shared.helpalert

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import uk.checkinsos.app.feature.shared.AlertSummary
import uk.checkinsos.app.feature.shared.DisplayText
import uk.checkinsos.app.feature.shared.FlowViewModel
import uk.checkinsos.domain.service.TimeProvider
import uk.checkinsos.domain.usecase.AcknowledgeAlert
import java.time.Duration
import java.time.Instant

/** Message, Accept and a display-only countdown to `acknowledgeBy`. */
class HelpAlertViewModel(
    alert: AlertSummary,
    private val acknowledgeAlert: AcknowledgeAlert,
    private val time: TimeProvider,
) : FlowViewModel<HelpAlertUiState, HelpAlertViewModel.Event>(
    HelpAlertUiState(alert, countdown(alert, time.now())),
) {
    data class Event(val alert: AlertSummary)

    /** Called once a second by the screen. */
    fun tick() {
        val text = countdown(currentState.alert, time.now())
        if (text != currentState.countdownText) setState { it.copy(countdownText = text) }
    }

    fun accept() {
        if (currentState.isBusy) return
        setState { it.copy(isBusy = true) }
        viewModelScope.launch {
            try {
                acknowledgeAlert(currentState.alert.id)
                setState { it.copy(isBusy = false) }
                send(Event(currentState.alert))
            } catch (e: Exception) {
                setState { it.copy(isBusy = false, errorMessage = DisplayText.GENERIC_ERROR) }
            }
        }
    }

    companion object {
        fun countdown(alert: AlertSummary, now: Instant): String {
            val millis = Duration.between(now, alert.acknowledgeBy).toMillis()
            return DisplayText.countdown(if (millis <= 0) 0 else (millis + 999) / 1000)
        }
    }
}
