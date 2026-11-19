package uk.checkinsos.domain.repository

import uk.checkinsos.domain.model.Alert
import uk.checkinsos.domain.model.AlertRecipient
import uk.checkinsos.domain.model.AlertStatus
import uk.checkinsos.domain.model.Neighbour
import uk.checkinsos.domain.model.OlderAdult
import uk.checkinsos.domain.model.Patient
import uk.checkinsos.domain.model.TimelineEvent
import uk.checkinsos.domain.model.TimelineEventKind
import java.time.Instant

/** Older adults visible to the current user (family "loved ones", neighbour "neighbours"). */
interface OlderAdultRepository {
    suspend fun lovedOnes(): List<OlderAdult>
    suspend fun neighbours(): List<OlderAdult>
    suspend fun olderAdult(id: String): OlderAdult
    suspend fun updateNeighbour(olderAdultId: String, neighbour: Neighbour): OlderAdult
    suspend fun recordScore(olderAdultId: String, score: Int)
}

interface PatientRepository {
    suspend fun patients(): List<Patient>
}

interface AlertRepository {
    suspend fun create(
        olderAdultId: String,
        subjectName: String,
        recipient: AlertRecipient,
        score: Int?,
        raisedAt: Instant,
        acknowledgeBy: Instant,
    ): Alert

    suspend fun alert(id: String): Alert
    suspend fun updateStatus(alertId: String, status: AlertStatus): Alert
}

interface TimelineRepository {
    suspend fun events(olderAdultId: String): List<TimelineEvent>
    suspend fun append(olderAdultId: String, kind: TimelineEventKind, date: Instant): TimelineEvent
}
