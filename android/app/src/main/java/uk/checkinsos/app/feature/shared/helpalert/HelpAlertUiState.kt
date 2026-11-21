package uk.checkinsos.app.feature.shared.helpalert

import uk.checkinsos.app.feature.shared.AlertSummary
import uk.checkinsos.app.feature.shared.FlowSteps
import uk.checkinsos.domain.model.AlertRecipient

/** Neighbour "Help" alert (`alert.*`) and Hospital alert (`hospitalAlert.*`). */
data class HelpAlertUiState(
    val alert: AlertSummary,
    val countdownText: String,
    /** 1 when the screen opened, 0 at the deadline. */
    val countdownProgress: Float = 1f,
    val isBusy: Boolean = false,
    val errorMessage: String? = null,
) {
    val step: Int get() = 1
    val title: String get() = "Urgent alert"
    val bannerText: String get() = "Urgent: someone needs help now"
    val countdownCaption: String get() = "left to respond"
    val totalSteps: Int get() = if (alert.recipient == AlertRecipient.NEIGHBOUR) FlowSteps.NEIGHBOUR else FlowSteps.HOSPITAL
    val tagPrefix: String get() = if (alert.recipient == AlertRecipient.NEIGHBOUR) "alert" else "hospitalAlert"
    val message: String
        get() = when (alert.recipient) {
            AlertRecipient.NEIGHBOUR ->
                "Your neighbour ${alert.subjectName} needs your help, please acknowledge the notification"
            AlertRecipient.HOSPITAL ->
                "The patient ${alert.subjectName} needs your help, please acknowledge the notification"
        }
}
