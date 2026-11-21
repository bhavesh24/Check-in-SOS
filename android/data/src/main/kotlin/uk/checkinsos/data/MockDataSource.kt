package uk.checkinsos.data

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import uk.checkinsos.domain.model.Alert
import uk.checkinsos.domain.model.AlertStatus
import uk.checkinsos.domain.model.DomainException
import uk.checkinsos.domain.model.OlderAdult
import uk.checkinsos.domain.model.Patient
import uk.checkinsos.domain.model.ScoreRecord
import uk.checkinsos.domain.model.TimelineEvent
import uk.checkinsos.domain.model.TimelineEventKind
import java.time.Duration
import java.time.Instant

/**
 * In-memory store seeded with [MockFixtures]. Swap for an API/Firebase data source later
 * without touching Domain or Presentation. All access is serialised by a [Mutex].
 *
 * @param now reference time the seeded timeline is relative to.
 */
class MockDataSource(now: Instant = MockFixtures.FIXED_NOW) {
    private val mutex = Mutex()
    private val scores: MutableMap<String, MutableList<ScoreRecord>> = MockFixtures.scoreHistory()
        .groupByTo(mutableMapOf(), { it.olderAdultId }) {
            ScoreRecord(it.score, now.minus(Duration.ofMinutes(it.minutesBefore)))
        }
    private val olderAdults = MockFixtures.olderAdults()
        .map { it.copy(lastCheckIn = scores[it.id]?.lastOrNull()?.date) }
        .toMutableList()
    private val alerts = linkedMapOf<String, Alert>()
    private val events: MutableList<TimelineEvent> = MockFixtures.timeline().mapIndexed { index, seed ->
        TimelineEvent("event-${index + 1}", seed.olderAdultId, now.minus(Duration.ofMinutes(seed.minutesBefore)), seed.kind)
    }.toMutableList()
    private var nextAlertId = 1
    private var nextEventId = events.size + 1

    // Older adults

    suspend fun allOlderAdults(): List<OlderAdult> = mutex.withLock { olderAdults.toList() }

    suspend fun olderAdult(id: String): OlderAdult = mutex.withLock { findOlderAdult(id) }

    suspend fun mutateOlderAdult(id: String, change: (OlderAdult) -> OlderAdult): OlderAdult = mutex.withLock {
        val index = olderAdults.indexOfFirst { it.id == id }
        if (index < 0) throw DomainException.NotFound(id)
        change(olderAdults[index]).also { olderAdults[index] = it }
    }

    suspend fun recordScore(olderAdultId: String, score: Int, date: Instant) = mutex.withLock {
        val index = olderAdults.indexOfFirst { it.id == olderAdultId }
        if (index < 0) throw DomainException.NotFound(olderAdultId)
        olderAdults[index] = olderAdults[index].copy(latestScore = score, lastCheckIn = date)
        scores.getOrPut(olderAdultId) { mutableListOf() }.add(ScoreRecord(score, date))
    }

    suspend fun scoreHistory(olderAdultId: String): List<ScoreRecord> = mutex.withLock {
        findOlderAdult(olderAdultId)
        scores[olderAdultId].orEmpty().toList()
    }

    // Patients

    suspend fun patients(): List<Patient> = mutex.withLock {
        MockFixtures.patients.map { (id, olderAdultId) ->
            val adult = findOlderAdult(olderAdultId)
            Patient(id, adult.name, adult.id, adult.latestScore)
        }
    }

    // Alerts

    suspend fun insertAlert(build: (String) -> Alert): Alert = mutex.withLock {
        build("alert-${nextAlertId++}").also { alerts[it.id] = it }
    }

    suspend fun alert(id: String): Alert = mutex.withLock { alerts[id] ?: throw DomainException.NotFound(id) }

    suspend fun setAlertStatus(id: String, status: AlertStatus): Alert = mutex.withLock {
        val alert = alerts[id] ?: throw DomainException.NotFound(id)
        alert.copy(status = status).also { alerts[id] = it }
    }

    // Timeline

    suspend fun events(olderAdultId: String): List<TimelineEvent> =
        mutex.withLock { events.filter { it.olderAdultId == olderAdultId } }

    suspend fun appendEvent(olderAdultId: String, kind: TimelineEventKind, date: Instant): TimelineEvent =
        mutex.withLock {
            TimelineEvent("event-${nextEventId++}", olderAdultId, date, kind).also { events.add(it) }
        }

    private fun findOlderAdult(id: String): OlderAdult =
        olderAdults.firstOrNull { it.id == id } ?: throw DomainException.NotFound(id)
}
