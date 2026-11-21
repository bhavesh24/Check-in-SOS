package uk.checkinsos.app.fakes

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import uk.checkinsos.app.feature.shared.AlertSummary
import uk.checkinsos.domain.model.Alert
import uk.checkinsos.domain.model.AlertRecipient
import uk.checkinsos.domain.model.AlertStatus
import uk.checkinsos.domain.model.DomainException
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
import uk.checkinsos.domain.service.TimeProvider
import java.time.Duration
import java.time.Instant

val T0: Instant = Instant.parse("2026-01-15T09:00:00Z")

class TestFailure : Exception("test failure")

fun adult(id: String, name: String, score: Int?) = OlderAdult(
    id, name, score, Neighbour(firstName = "Alex", lastName = "Brown", email = "alex@example.com", mobile = "07700900123"),
)

/** In-memory fake for every repository (test-only). Set [failing] to make every call throw. */
class FakeStore(
    adults: List<OlderAdult> = listOf(adult("oa-1", "Margaret Wilson", 76), adult("oa-2", "Arthur Patel", 38)),
    private val patientList: List<Patient> = listOf(Patient("p-1", "Arthur Patel", "oa-2", 38)),
) : OlderAdultRepository, PatientRepository, AlertRepository, TimelineRepository {
    val adults = adults.toMutableList()
    val alerts = linkedMapOf<String, Alert>()
    val events = mutableListOf<TimelineEvent>()
    val history = mutableListOf<ScoreRecord>()
    var failing = false

    val kinds: List<TimelineEventKind> get() = events.map { it.kind }

    private fun check() {
        if (failing) throw TestFailure()
    }

    override suspend fun lovedOnes(): List<OlderAdult> = adults.toList().also { check() }
    override suspend fun neighbours(): List<OlderAdult> = adults.toList().also { check() }
    override suspend fun olderAdult(id: String): OlderAdult {
        check()
        return adults.firstOrNull { it.id == id } ?: throw DomainException.NotFound(id)
    }
    override suspend fun updateNeighbour(olderAdultId: String, neighbour: Neighbour): OlderAdult {
        val updated = olderAdult(olderAdultId).copy(neighbour = neighbour)
        adults[adults.indexOfFirst { it.id == olderAdultId }] = updated
        return updated
    }
    override suspend fun recordScore(olderAdultId: String, score: Int, date: Instant) {
        val updated = olderAdult(olderAdultId).copy(latestScore = score, lastCheckIn = date)
        adults[adults.indexOfFirst { it.id == olderAdultId }] = updated
        history.add(ScoreRecord(score, date))
    }
    override suspend fun scoreHistory(olderAdultId: String): List<ScoreRecord> = history.toList().also { check() }
    override suspend fun patients(): List<Patient> = patientList.also { check() }
    override suspend fun create(
        olderAdultId: String, subjectName: String, recipient: AlertRecipient, score: Int?,
        raisedAt: Instant, acknowledgeBy: Instant,
    ): Alert {
        check()
        val alert = Alert("a${alerts.size + 1}", olderAdultId, subjectName, recipient, score, raisedAt, acknowledgeBy)
        alerts[alert.id] = alert
        return alert
    }
    override suspend fun alert(id: String): Alert {
        check()
        return alerts[id] ?: throw DomainException.NotFound(id)
    }
    override suspend fun updateStatus(alertId: String, status: AlertStatus): Alert {
        val updated = alert(alertId).copy(status = status)
        alerts[alertId] = updated
        return updated
    }
    override suspend fun events(olderAdultId: String): List<TimelineEvent> {
        check()
        return events.filter { it.olderAdultId == olderAdultId }
    }
    override suspend fun append(olderAdultId: String, kind: TimelineEventKind, date: Instant): TimelineEvent {
        check()
        return TimelineEvent("e${events.size}", olderAdultId, date, kind).also { events += it }
    }

    /** Seeds a pending alert about Arthur Patel (20-minute deadline) and returns its summary. */
    suspend fun seedAlert(recipient: AlertRecipient = AlertRecipient.NEIGHBOUR): AlertSummary =
        AlertSummary(create("oa-2", "Arthur Patel", recipient, 38, T0, T0.plus(Duration.ofMinutes(20))))
}

/** Mutable clock for countdown tests. */
class MutableTimeProvider(var instant: Instant) : TimeProvider {
    override fun now(): Instant = instant
}

/** Runs `viewModelScope` work eagerly on an unconfined test dispatcher. */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule : TestWatcher() {
    override fun starting(description: Description) = Dispatchers.setMain(UnconfinedTestDispatcher())
    override fun finished(description: Description) = Dispatchers.resetMain()
}

/** Collects every event a ViewModel sends during the test. */
@OptIn(ExperimentalCoroutinesApi::class)
fun <E> TestScope.collect(events: Flow<E>): List<E> {
    val received = mutableListOf<E>()
    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { events.collect { received += it } }
    return received
}


