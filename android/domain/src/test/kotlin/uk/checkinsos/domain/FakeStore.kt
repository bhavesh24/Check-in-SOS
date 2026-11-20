package uk.checkinsos.domain

import uk.checkinsos.domain.model.Alert
import uk.checkinsos.domain.model.AlertRecipient
import uk.checkinsos.domain.model.AlertStatus
import uk.checkinsos.domain.model.DomainException
import uk.checkinsos.domain.model.Neighbour
import uk.checkinsos.domain.model.OlderAdult
import uk.checkinsos.domain.model.Patient
import uk.checkinsos.domain.model.TimelineEvent
import uk.checkinsos.domain.model.TimelineEventKind
import uk.checkinsos.domain.repository.AlertRepository
import uk.checkinsos.domain.repository.OlderAdultRepository
import uk.checkinsos.domain.repository.PatientRepository
import uk.checkinsos.domain.repository.TimelineRepository
import java.time.Instant

val T0: Instant = Instant.parse("2026-01-15T09:00:00Z")

fun adult(id: String, name: String, score: Int?) = OlderAdult(
    id, name, score, Neighbour(firstName = "N", lastName = id, email = "n@example.com", mobile = "07700900123"),
)

/** One in-memory fake that implements every repository interface (test-only). */
class FakeStore(
    adults: List<OlderAdult> = emptyList(),
    private val patientList: List<Patient> = emptyList(),
) : OlderAdultRepository, PatientRepository, AlertRepository, TimelineRepository {
    val adults = adults.toMutableList()
    val alerts = linkedMapOf<String, Alert>()
    val events = mutableListOf<TimelineEvent>()
    val kinds get() = events.map { it.kind }

    override suspend fun lovedOnes() = adults.toList()
    override suspend fun neighbours() = adults.toList()
    override suspend fun olderAdult(id: String) = adults.firstOrNull { it.id == id } ?: throw DomainException.NotFound(id)
    override suspend fun updateNeighbour(olderAdultId: String, neighbour: Neighbour): OlderAdult {
        val i = indexOf(olderAdultId)
        return adults[i].copy(neighbour = neighbour).also { adults[i] = it }
    }
    override suspend fun recordScore(olderAdultId: String, score: Int) {
        val i = indexOf(olderAdultId)
        adults[i] = adults[i].copy(latestScore = score)
    }
    override suspend fun patients() = patientList
    override suspend fun create(
        olderAdultId: String, subjectName: String, recipient: AlertRecipient, score: Int?,
        raisedAt: Instant, acknowledgeBy: Instant,
    ) = Alert("a${alerts.size + 1}", olderAdultId, subjectName, recipient, score, raisedAt, acknowledgeBy)
        .also { alerts[it.id] = it }
    override suspend fun alert(id: String) = alerts[id] ?: throw DomainException.NotFound(id)
    override suspend fun updateStatus(alertId: String, status: AlertStatus) =
        alert(alertId).copy(status = status).also { alerts[alertId] = it }
    override suspend fun events(olderAdultId: String) = events.filter { it.olderAdultId == olderAdultId }
    override suspend fun append(olderAdultId: String, kind: TimelineEventKind, date: Instant) =
        TimelineEvent("e${events.size}", olderAdultId, date, kind).also { events.add(it) }

    private fun indexOf(id: String) = adults.indexOfFirst { it.id == id }.also { if (it < 0) throw DomainException.NotFound(id) }
}
