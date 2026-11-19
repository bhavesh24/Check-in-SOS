package uk.checkinsos.domain

import kotlinx.coroutines.test.runTest
import uk.checkinsos.domain.model.AlertRecipient
import uk.checkinsos.domain.model.AlertStatus
import uk.checkinsos.domain.model.Assessment
import uk.checkinsos.domain.model.AssessmentResult
import uk.checkinsos.domain.model.CareOutcome
import uk.checkinsos.domain.model.DomainException
import uk.checkinsos.domain.model.Mood
import uk.checkinsos.domain.model.Neighbour
import uk.checkinsos.domain.model.NeighbourValidationError
import uk.checkinsos.domain.model.Patient
import uk.checkinsos.domain.model.PhysicalWellbeing
import uk.checkinsos.domain.model.TimelineEventKind
import uk.checkinsos.domain.service.EscalationPolicy
import uk.checkinsos.domain.service.FixedTimeProvider
import uk.checkinsos.domain.usecase.AcknowledgeAlert
import uk.checkinsos.domain.usecase.EscalateAlert
import uk.checkinsos.domain.usecase.GetLovedOneStatus
import uk.checkinsos.domain.usecase.GetLovedOnes
import uk.checkinsos.domain.usecase.GetNeighbours
import uk.checkinsos.domain.usecase.GetPatients
import uk.checkinsos.domain.usecase.GetTimeline
import uk.checkinsos.domain.usecase.MarkArrived
import uk.checkinsos.domain.usecase.RecordCareOutcome
import uk.checkinsos.domain.usecase.SimulateAlert
import uk.checkinsos.domain.usecase.SubmitAssessment
import uk.checkinsos.domain.usecase.UpdateNeighbour
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

private val time = FixedTimeProvider(T0)

class SubmitAssessmentTest {
    private val store = FakeStore(listOf(adult("oa-1", "Margaret", null)))
    private val submit = SubmitAssessment(store, store, store, time)

    @Test fun goodScoreDoesNotNotify() = runTest {
        val result = submit("oa-1", Assessment(PhysicalWellbeing.GOOD, Mood.PLEASANT))
        assertEquals(AssessmentResult(76, neighbourNotified = false), result)
        assertEquals(76, store.olderAdult("oa-1").latestScore)
        assertEquals(listOf<TimelineEventKind>(TimelineEventKind.Score(76)), store.kinds)
        assertTrue(store.alerts.isEmpty())
    }

    @Test fun lowScoreNotifiesNeighbourWithPolicyTimeout() = runTest {
        val result = submit("oa-1", Assessment(PhysicalWellbeing.ILL, Mood.ANXIOUS))
        assertEquals(AssessmentResult(38, neighbourNotified = true), result)
        assertEquals(listOf(TimelineEventKind.Score(38), TimelineEventKind.NotificationSent(1)), store.kinds)
        val alert = store.alerts.values.single()
        assertEquals(AlertRecipient.NEIGHBOUR, alert.recipient)
        assertEquals("Margaret", alert.subjectName)
        assertEquals(T0.plusSeconds(20 * 60), alert.acknowledgeBy)
    }

    @Test fun unknownOlderAdultThrows() = runTest {
        val error = assertFailsWith<DomainException.NotFound> { submit("nope", Assessment(PhysicalWellbeing.FINE, Mood.HAPPY)) }
        assertEquals("nope", error.id)
    }
}

class FamilyUseCaseTest {
    private val store = FakeStore(listOf(adult("oa-1", "Margaret", 76), adult("oa-2", "Arthur", 38)))

    @Test fun lovedOnesAndStatus() = runTest {
        assertEquals(listOf("oa-1", "oa-2"), GetLovedOnes(store)().map { it.id })
        assertEquals(38, GetLovedOneStatus(store)("oa-2").latestScore)
    }

    @Test fun updateNeighbourValidatesAndSaves() = runTest {
        val update = UpdateNeighbour(store)
        val saved = update("oa-1", Neighbour(firstName = " Sam ", lastName = "Lee", email = "sam@lee.uk", mobile = "07999999999"))
        assertEquals("Sam Lee", saved.neighbour.displayName)
        val error = assertFailsWith<DomainException.InvalidNeighbour> {
            update("oa-1", Neighbour(firstName = "Sam", lastName = "Lee", email = "nope", mobile = "07999999999"))
        }
        assertEquals(NeighbourValidationError.INVALID_EMAIL, error.error)
    }

    @Test fun timelineIsSortedOldestFirst() = runTest {
        store.append("oa-1", TimelineEventKind.NeighbourReached, T0)
        store.append("oa-1", TimelineEventKind.Score(30), T0.minusSeconds(60))
        store.append("oa-2", TimelineEventKind.MissedCheckIn, T0)
        assertEquals(
            listOf(TimelineEventKind.Score(30), TimelineEventKind.NeighbourReached),
            GetTimeline(store)("oa-1").map { it.kind },
        )
    }
}

class AlertUseCaseTest {
    private val store = FakeStore(
        listOf(adult("oa-1", "Margaret", 76), adult("oa-2", "Arthur", 38), adult("oa-3", "Edith", null)),
        listOf(Patient("p-1", "Edith", "oa-3", null), Patient("p-2", "Arthur", "oa-2", 38)),
    )
    private fun simulateFor(s: FakeStore) = SimulateAlert(s, s, s, s, time)
    private val simulate = simulateFor(store)

    @Test fun listsNeighboursAndPatients() = runTest {
        assertEquals(3, GetNeighbours(store)().size)
        assertEquals(listOf("p-1", "p-2"), GetPatients(store)().map { it.id })
    }

    @Test fun simulatePicksLowestScore() = runTest {
        val neighbourAlert = simulate(AlertRecipient.NEIGHBOUR)
        assertEquals("Arthur", neighbourAlert.subjectName)
        assertEquals(T0.plusSeconds(20 * 60), neighbourAlert.acknowledgeBy)
        val hospitalAlert = simulate(AlertRecipient.HOSPITAL)
        assertEquals("oa-2", hospitalAlert.olderAdultId)
        assertEquals(AlertRecipient.HOSPITAL, hospitalAlert.recipient)
        assertEquals(listOf(TimelineEventKind.NotificationSent(1), TimelineEventKind.NotificationSent(2)), store.kinds)
    }

    @Test fun simulateFallsBackToUnscoredThenGoodScoreTimeouts() = runTest {
        val unscored = simulateFor(FakeStore(listOf(adult("oa-3", "Edith", null))))(AlertRecipient.NEIGHBOUR)
        assertNull(unscored.score)
        assertEquals(T0.plus(EscalationPolicy.MISSED_CHECK_IN_TIMEOUT), unscored.acknowledgeBy)
        val good = simulateFor(FakeStore(listOf(adult("oa-1", "Margaret", 90))))(AlertRecipient.NEIGHBOUR)
        assertEquals(T0.plus(EscalationPolicy.MISSED_CHECK_IN_TIMEOUT), good.acknowledgeBy)
    }

    @Test fun simulateWithNobodyThrows() = runTest {
        assertFailsWith<DomainException.NoAlertSubject> { simulateFor(FakeStore())(AlertRecipient.HOSPITAL) }
    }

    @Test fun neighbourFlowEscalates() = runTest {
        val alert = simulate(AlertRecipient.NEIGHBOUR)
        assertEquals(AlertStatus.ACKNOWLEDGED, AcknowledgeAlert(store, store, time)(alert.id).status)
        assertEquals(AlertStatus.ARRIVED, MarkArrived(store, store, time)(alert.id).status)
        assertEquals(AlertStatus.ESCALATED, EscalateAlert(store, store, time)(alert.id).status)
        assertEquals(AlertStatus.RESOLVED, RecordCareOutcome(store, store, time)(alert.id, CareOutcome.ALIVE).status)
        assertEquals(
            listOf(
                TimelineEventKind.NotificationSent(1), TimelineEventKind.Acknowledged(1), TimelineEventKind.NeighbourReached,
                TimelineEventKind.Escalated(2), TimelineEventKind.Outcome(CareOutcome.ALIVE),
            ),
            store.kinds,
        )
    }

    @Test fun nonTerminalOutcomeKeepsStatus() = runTest {
        val alert = simulate(AlertRecipient.HOSPITAL)
        val record = RecordCareOutcome(store, store, time)
        AcknowledgeAlert(store, store, time)(alert.id)
        assertEquals(AlertStatus.ACKNOWLEDGED, record(alert.id, CareOutcome.IN_CARE).status)
        assertEquals(AlertStatus.RESOLVED, record(alert.id, CareOutcome.RECOVERED).status)
    }

    @Test fun unknownAlertThrows() = runTest {
        assertFailsWith<DomainException.NotFound> { AcknowledgeAlert(store, store, time)("x") }
    }
}
