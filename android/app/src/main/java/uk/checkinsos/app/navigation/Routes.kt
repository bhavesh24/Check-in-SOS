package uk.checkinsos.app.navigation

import kotlinx.serialization.Serializable
import uk.checkinsos.app.feature.hospital.carestep.CareStep
import uk.checkinsos.app.feature.shared.AlertSummary
import uk.checkinsos.domain.model.AlertRecipient
import uk.checkinsos.domain.model.Role
import java.time.Instant

/** Type-safe navigation routes. Alert routes carry an [AlertSummary] as flat primitives. */
object Routes {
    @Serializable data object Home
    @Serializable data object Assessment
    @Serializable data class Score(val score: Int, val neighbourNotified: Boolean)
    @Serializable data object NoCheckIn

    @Serializable data object LovedOnes
    @Serializable data class Status(val olderAdultId: String)
    @Serializable data class EditNeighbour(val olderAdultId: String)
    @Serializable data class Timeline(val olderAdultId: String)

    @Serializable data object Neighbours
    @Serializable data object Patients

    /** Shared by every route of the help flows. */
    sealed interface AlertRoute {
        val alertId: String
        val subjectName: String
        val recipient: String
        val acknowledgeByMillis: Long

        val alert: AlertSummary
            get() = AlertSummary(alertId, subjectName, AlertRecipient.valueOf(recipient), Instant.ofEpochMilli(acknowledgeByMillis))
    }

    @Serializable
    data class HelpAlert(
        override val alertId: String, override val subjectName: String,
        override val recipient: String, override val acknowledgeByMillis: Long,
    ) : AlertRoute

    @Serializable
    data class Reached(
        override val alertId: String, override val subjectName: String,
        override val recipient: String, override val acknowledgeByMillis: Long,
    ) : AlertRoute

    @Serializable
    data class Escalate(
        override val alertId: String, override val subjectName: String,
        override val recipient: String, override val acknowledgeByMillis: Long,
    ) : AlertRoute

    @Serializable
    data class Alive(
        override val alertId: String, override val subjectName: String,
        override val recipient: String, override val acknowledgeByMillis: Long,
        val calledNumber: String? = null,
    ) : AlertRoute

    @Serializable
    data class CareStepRoute(
        val step: String,
        override val alertId: String, override val subjectName: String,
        override val recipient: String, override val acknowledgeByMillis: Long,
    ) : AlertRoute {
        val careStep: CareStep get() = CareStep.valueOf(step)
    }

    fun forRole(role: Role): Any = when (role) {
        Role.OLDER_ADULT -> Assessment
        Role.FAMILY -> LovedOnes
        Role.HOSPITAL -> Patients
        Role.NEIGHBOUR -> Neighbours
    }

    fun helpAlert(a: AlertSummary) = HelpAlert(a.id, a.subjectName, a.recipient.name, a.acknowledgeBy.toEpochMilli())
    fun reached(a: AlertSummary) = Reached(a.id, a.subjectName, a.recipient.name, a.acknowledgeBy.toEpochMilli())
    fun escalate(a: AlertSummary) = Escalate(a.id, a.subjectName, a.recipient.name, a.acknowledgeBy.toEpochMilli())
    fun alive(a: AlertSummary, calledNumber: String?) =
        Alive(a.id, a.subjectName, a.recipient.name, a.acknowledgeBy.toEpochMilli(), calledNumber)
    fun careStep(step: CareStep, a: AlertSummary) =
        CareStepRoute(step.name, a.id, a.subjectName, a.recipient.name, a.acknowledgeBy.toEpochMilli())
}
