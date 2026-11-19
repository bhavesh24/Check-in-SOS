package uk.checkinsos.domain.usecase

import uk.checkinsos.domain.model.Alert
import uk.checkinsos.domain.model.AlertRecipient
import uk.checkinsos.domain.model.AlertStatus
import uk.checkinsos.domain.model.CareOutcome
import uk.checkinsos.domain.model.DomainException
import uk.checkinsos.domain.model.OlderAdult
import uk.checkinsos.domain.model.Patient
import uk.checkinsos.domain.model.TimelineEventKind
import uk.checkinsos.domain.repository.AlertRepository
import uk.checkinsos.domain.repository.OlderAdultRepository
import uk.checkinsos.domain.repository.PatientRepository
import uk.checkinsos.domain.repository.TimelineRepository
import uk.checkinsos.domain.service.EscalationPolicy
import uk.checkinsos.domain.service.TimeProvider

class GetNeighbours(private val olderAdults: OlderAdultRepository) {
    suspend operator fun invoke(): List<OlderAdult> = olderAdults.neighbours()
}

class GetPatients(private val patients: PatientRepository) {
    suspend operator fun invoke(): List<Patient> = patients.patients()
}

/**
 * Demo stand-in for a push notification: raises an alert about the lowest-scoring person in the
 * recipient's list (people without a score are only picked if nobody has one).
 */
class SimulateAlert(
    private val olderAdults: OlderAdultRepository,
    private val patients: PatientRepository,
    private val alerts: AlertRepository,
    private val timeline: TimelineRepository,
    private val time: TimeProvider,
) {
    private data class Candidate(val olderAdultId: String, val name: String, val score: Int?)

    suspend operator fun invoke(recipient: AlertRecipient): Alert {
        val candidates = when (recipient) {
            AlertRecipient.NEIGHBOUR -> olderAdults.neighbours().map { Candidate(it.id, it.name, it.latestScore) }
            AlertRecipient.HOSPITAL -> patients.patients().map { Candidate(it.olderAdultId, it.name, it.latestScore) }
        }
        val subject = candidates.filter { it.score != null }.minByOrNull { it.score!! }
            ?: candidates.firstOrNull()
            ?: throw DomainException.NoAlertSubject
        val now = time.now()
        val timeout = EscalationPolicy.acknowledgementTimeout(subject.score) ?: EscalationPolicy.MISSED_CHECK_IN_TIMEOUT
        val alert = alerts.create(subject.olderAdultId, subject.name, recipient, subject.score, now, now.plus(timeout))
        timeline.append(subject.olderAdultId, TimelineEventKind.NotificationSent(recipient.level), now)
        return alert
    }
}

/** Shared by every alert-status use case: change status, then log a timeline event. */
internal class AlertTransition(
    private val alerts: AlertRepository,
    private val timeline: TimelineRepository,
    private val time: TimeProvider,
) {
    suspend fun apply(alertId: String, status: AlertStatus, event: (Alert) -> TimelineEventKind): Alert {
        val updated = alerts.updateStatus(alertId, status)
        timeline.append(updated.olderAdultId, event(updated), time.now())
        return updated
    }
}

class AcknowledgeAlert(alerts: AlertRepository, timeline: TimelineRepository, time: TimeProvider) {
    private val transition = AlertTransition(alerts, timeline, time)
    suspend operator fun invoke(alertId: String): Alert =
        transition.apply(alertId, AlertStatus.ACKNOWLEDGED) { TimelineEventKind.Acknowledged(it.recipient.level) }
}

class MarkArrived(alerts: AlertRepository, timeline: TimelineRepository, time: TimeProvider) {
    private val transition = AlertTransition(alerts, timeline, time)
    suspend operator fun invoke(alertId: String): Alert =
        transition.apply(alertId, AlertStatus.ARRIVED) { TimelineEventKind.NeighbourReached }
}

/** Neighbour escalates to the next level (emergency services). */
class EscalateAlert(alerts: AlertRepository, timeline: TimelineRepository, time: TimeProvider) {
    private val transition = AlertTransition(alerts, timeline, time)
    suspend operator fun invoke(alertId: String): Alert =
        transition.apply(alertId, AlertStatus.ESCALATED) { TimelineEventKind.Escalated(it.recipient.level + 1) }
}

/** Records a neighbour/hospital answer; terminal outcomes resolve the alert. */
class RecordCareOutcome(
    private val alerts: AlertRepository,
    timeline: TimelineRepository,
    time: TimeProvider,
) {
    private val transition = AlertTransition(alerts, timeline, time)
    suspend operator fun invoke(alertId: String, outcome: CareOutcome): Alert {
        val current = alerts.alert(alertId)
        val status = if (outcome.resolvesAlert) AlertStatus.RESOLVED else current.status
        return transition.apply(alertId, status) { TimelineEventKind.Outcome(outcome) }
    }
}
