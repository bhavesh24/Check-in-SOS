package uk.checkinsos.data

import uk.checkinsos.domain.model.Alert
import uk.checkinsos.domain.model.AlertRecipient
import uk.checkinsos.domain.model.AlertStatus
import uk.checkinsos.domain.model.Neighbour
import uk.checkinsos.domain.model.OlderAdult
import uk.checkinsos.domain.model.Patient
import uk.checkinsos.domain.model.ScoreRecord
import uk.checkinsos.domain.model.TimelineEvent
import uk.checkinsos.domain.model.TimelineEventKind
import uk.checkinsos.domain.repository.AlertRepository
import uk.checkinsos.domain.repository.OlderAdultRepository
import uk.checkinsos.domain.repository.PatientRepository
import uk.checkinsos.domain.repository.TimelineRepository
import java.time.Instant

class OlderAdultRepositoryImpl(private val source: MockDataSource) : OlderAdultRepository {
    override suspend fun lovedOnes(): List<OlderAdult> = source.allOlderAdults()
    override suspend fun neighbours(): List<OlderAdult> = source.allOlderAdults()
    override suspend fun olderAdult(id: String): OlderAdult = source.olderAdult(id)

    override suspend fun updateNeighbour(olderAdultId: String, neighbour: Neighbour): OlderAdult =
        source.mutateOlderAdult(olderAdultId) { it.copy(neighbour = neighbour) }

    override suspend fun recordScore(olderAdultId: String, score: Int, date: Instant) {
        source.recordScore(olderAdultId, score, date)
    }

    override suspend fun scoreHistory(olderAdultId: String): List<ScoreRecord> = source.scoreHistory(olderAdultId)
}

class PatientRepositoryImpl(private val source: MockDataSource) : PatientRepository {
    override suspend fun patients(): List<Patient> = source.patients()
}

class AlertRepositoryImpl(private val source: MockDataSource) : AlertRepository {
    override suspend fun create(
        olderAdultId: String,
        subjectName: String,
        recipient: AlertRecipient,
        score: Int?,
        raisedAt: Instant,
        acknowledgeBy: Instant,
    ): Alert = source.insertAlert { id ->
        Alert(id, olderAdultId, subjectName, recipient, score, raisedAt, acknowledgeBy)
    }

    override suspend fun alert(id: String): Alert = source.alert(id)

    override suspend fun updateStatus(alertId: String, status: AlertStatus): Alert =
        source.setAlertStatus(alertId, status)
}

class TimelineRepositoryImpl(private val source: MockDataSource) : TimelineRepository {
    override suspend fun events(olderAdultId: String): List<TimelineEvent> = source.events(olderAdultId)

    override suspend fun append(olderAdultId: String, kind: TimelineEventKind, date: Instant): TimelineEvent =
        source.appendEvent(olderAdultId, kind, date)
}
