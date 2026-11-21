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
    HelpAlertUiState(alert, countdown(alert, time.now()), if (secondsLeft(alert, time.now()) > 0) 1f else 0f),
) {
    data class Event(val alert: AlertSummary)

    /** Seconds left when the screen opened: the full ring. */
    private val initialSeconds = secondsLeft(alert, time.now())

    /** Called once a second by the screen. */
    fun tick() {
        val now = time.now()
        val text = countdown(currentState.alert, now)
        val progress = if (initialSeconds > 0) secondsLeft(currentState.alert, now).toFloat() / initialSeconds else 0f
        if (text != currentState.countdownText || progress != currentState.countdownProgress) {
            setState { it.copy(countdownText = text, countdownProgress = progress) }
        }
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
        fun countdown(alert: AlertSummary, now: Instant): String = DisplayText.countdown(secondsLeft(alert, now))

        fun secondsLeft(alert: AlertSummary, now: Instant): Long {
            val millis = Duration.between(now, alert.acknowledgeBy).toMillis()
            return if (millis <= 0) 0 else (millis + 999) / 1000
        }
    }
}
