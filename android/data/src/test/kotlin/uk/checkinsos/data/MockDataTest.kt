package uk.checkinsos.data

import kotlinx.coroutines.test.runTest
import uk.checkinsos.domain.model.AlertRecipient
import uk.checkinsos.domain.model.AlertStatus
import uk.checkinsos.domain.model.Assessment
import uk.checkinsos.domain.model.DomainException
import uk.checkinsos.domain.model.Mood
import uk.checkinsos.domain.model.Neighbour
import uk.checkinsos.domain.model.PhysicalWellbeing
import uk.checkinsos.domain.model.TimelineEventKind
import uk.checkinsos.domain.service.FixedTimeProvider
import uk.checkinsos.domain.usecase.SubmitAssessment
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class MockDataTest {
    private val source = MockDataSource()
    private val adults = OlderAdultRepositoryImpl(source)
    private val timeline = TimelineRepositoryImpl(source)
    private val alerts = AlertRepositoryImpl(source)
    private val patients = PatientRepositoryImpl(source)

    @Test fun seedMatchesSpec() = runTest {
        val lovedOnes = adults.lovedOnes()
        assertEquals(listOf("Margaret Wilson", "Arthur Patel", "Edith Clarke"), lovedOnes.map { it.name })
        assertEquals(listOf(76, 38, null), lovedOnes.map { it.latestScore })
        assertEquals("Alex Brown", lovedOnes[0].neighbour.displayName)
        assertEquals(listOf("oa-1", "oa-2", "oa-3"), adults.neighbours().map { it.id })
        val list = patients.patients()
        assertEquals(listOf("p-1", "p-2", "p-3"), list.map { it.id })
        assertEquals("Arthur Patel", list[0].name)
        assertEquals(38, list[0].latestScore)
        assertEquals("oa-1", MockFixtures.CURRENT_OLDER_ADULT_ID)
        assertEquals(Instant.parse("2026-01-15T09:00:00Z"), MockFixtures.FIXED_NOW)
    }

    @Test fun seededTimelineMirrorsPrototype() = runTest {
        val events = timeline.events("oa-2")
        assertEquals(
            listOf(
                "Score 38", "Notification sent to 1st level", "1st level acknowledged", "Neighbour reached",
                "Escalation to 2nd level", "2nd level acknowledged", "In care in hospital", "Recovered",
            ),
            events.map { it.kind.title },
        )
        assertEquals(MockFixtures.FIXED_NOW.minusSeconds(3 * 3600), events.first().date)
        assertTrue(timeline.events("oa-3").isEmpty())
        val added = timeline.append("oa-3", TimelineEventKind.MissedCheckIn, MockFixtures.FIXED_NOW)
        assertEquals("event-10", added.id)
        assertEquals(listOf(added), timeline.events("oa-3"))
    }

    @Test fun updatesScoreAndNeighbour() = runTest {
        adults.recordScore("oa-3", 55)
        val updated = adults.updateNeighbour(
            "oa-3", Neighbour(firstName = "Sam", lastName = "Lee", email = "sam.lee@example.com", mobile = "07700900999"),
        )
        assertEquals(55, updated.latestScore)
        assertEquals("Sam Lee", adults.olderAdult("oa-3").neighbour.displayName)
        assertEquals(55, patients.patients()[1].latestScore)
    }

    @Test fun alertLifecycle() = runTest {
        val now = MockFixtures.FIXED_NOW
        val created = alerts.create("oa-2", "Arthur Patel", AlertRecipient.NEIGHBOUR, 38, now, now.plusSeconds(1200))
        assertEquals("alert-1", created.id)
        assertEquals(AlertStatus.PENDING, created.status)
        assertEquals(AlertStatus.ACKNOWLEDGED, alerts.updateStatus(created.id, AlertStatus.ACKNOWLEDGED).status)
        assertEquals(AlertStatus.ACKNOWLEDGED, alerts.alert(created.id).status)
    }

    @Test fun missingIdsThrowNotFound() = runTest {
        assertFailsWith<DomainException.NotFound> { adults.olderAdult("x") }
        assertFailsWith<DomainException.NotFound> { adults.recordScore("x", 1) }
        assertFailsWith<DomainException.NotFound> { alerts.alert("x") }
        assertFailsWith<DomainException.NotFound> { alerts.updateStatus("x", AlertStatus.RESOLVED) }
    }

    @Test fun endToEndWithRealRepositories() = runTest {
        val submit = SubmitAssessment(adults, alerts, timeline, FixedTimeProvider(MockFixtures.FIXED_NOW))
        val result = submit(MockFixtures.CURRENT_OLDER_ADULT_ID, Assessment(PhysicalWellbeing.ILL, Mood.ANXIOUS))
        assertTrue(result.neighbourNotified)
        assertEquals(
            listOf(TimelineEventKind.Score(38), TimelineEventKind.NotificationSent(1)),
            timeline.events("oa-1").map { it.kind }.takeLast(2),
        )
    }
}
