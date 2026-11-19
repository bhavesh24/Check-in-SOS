package uk.checkinsos.domain

import uk.checkinsos.domain.model.Alert
import uk.checkinsos.domain.model.AlertRecipient
import uk.checkinsos.domain.model.Assessment
import uk.checkinsos.domain.model.CareOutcome
import uk.checkinsos.domain.model.DomainException
import uk.checkinsos.domain.model.Mood
import uk.checkinsos.domain.model.Neighbour
import uk.checkinsos.domain.model.NeighbourValidationError
import uk.checkinsos.domain.model.PhysicalWellbeing
import uk.checkinsos.domain.model.Role
import uk.checkinsos.domain.model.TimelineEventKind
import uk.checkinsos.domain.model.TimelineSeverity
import uk.checkinsos.domain.service.EscalationPolicy
import uk.checkinsos.domain.service.FixedTimeProvider
import uk.checkinsos.domain.service.NeighbourValidator
import uk.checkinsos.domain.service.ScoreCalculator
import uk.checkinsos.domain.service.SystemTimeProvider
import java.time.Duration
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScoreCalculatorTest {
    @Test fun reproducesPrototypeExamples() {
        assertEquals(76, ScoreCalculator.score(Assessment(PhysicalWellbeing.GOOD, Mood.PLEASANT)))
        assertEquals(38, ScoreCalculator.score(Assessment(PhysicalWellbeing.ILL, Mood.ANXIOUS)))
    }

    @Test fun scoresStayWithinZeroToHundred() {
        for (p in PhysicalWellbeing.entries) for (m in Mood.entries) {
            assertTrue(ScoreCalculator.score(Assessment(p, m)) in 0..100)
        }
        assertEquals(100, ScoreCalculator.score(Assessment(PhysicalWellbeing.FINE, Mood.HAPPY)))
        assertEquals(10, ScoreCalculator.score(Assessment(PhysicalWellbeing.VERY_SICK, Mood.DEPRESSED)))
    }

    @Test fun goodThreshold() {
        for ((score, good) in listOf(69 to false, 70 to true, 100 to true, 0 to false)) {
            assertEquals(good, ScoreCalculator.isGood(score), "score $score")
            assertEquals(!good, EscalationPolicy.shouldAlert(score), "score $score")
        }
    }
}

class EscalationPolicyTest {
    @Test fun timeoutByScore() {
        val cases = listOf(100 to null, 70 to null, 69 to 30L, 50 to 30L, 49 to 20L, 30 to 20L, 29 to 15L, 0 to 15L)
        for ((score, minutes) in cases) {
            assertEquals(minutes?.let { Duration.ofMinutes(it) }, EscalationPolicy.acknowledgementTimeout(score), "score $score")
        }
    }

    @Test fun missedCheckInUsesPlaceholderTimeout() {
        assertEquals(Duration.ofMinutes(15), EscalationPolicy.acknowledgementTimeout(null))
    }
}

class NeighbourValidatorTest {
    private fun n(first: String = "A", last: String = "B", email: String = "a@b.co", mobile: String = "07123456789") =
        Neighbour(firstName = first, lastName = last, email = email, mobile = mobile)

    @Test fun trimsAndAccepts() {
        val result = NeighbourValidator.validate(n(first = " Alex ", last = "Brown", mobile = "07123 456789"))
        val valid = (result as NeighbourValidator.Result.Valid).neighbour
        assertEquals("Alex", valid.firstName)
        assertEquals("Alex Brown", valid.displayName)
    }

    @Test fun rejects() {
        val cases = listOf(
            n(first = "  ") to NeighbourValidationError.FIRST_NAME_REQUIRED,
            n(last = "") to NeighbourValidationError.LAST_NAME_REQUIRED,
            n(email = "ABC@mail") to NeighbourValidationError.INVALID_EMAIL,
            n(mobile = "0712") to NeighbourValidationError.INVALID_MOBILE,
            n(mobile = "07123abc789") to NeighbourValidationError.INVALID_MOBILE,
        )
        for ((neighbour, error) in cases) {
            assertEquals(NeighbourValidator.Result.Invalid(error), NeighbourValidator.validate(neighbour))
        }
    }

    @Test fun acceptsUkMobileFormats() {
        for (mobile in listOf("+44 7123 456789", "07123-456-789", "0123456789", "7123456789")) {
            assertTrue(NeighbourValidator.isValidMobile(mobile), mobile)
        }
    }

    @Test fun errorMessagesMatchSpec() {
        assertEquals("First name is required", NeighbourValidationError.FIRST_NAME_REQUIRED.message)
        assertEquals("Last name is required", NeighbourValidationError.LAST_NAME_REQUIRED.message)
        assertEquals("Enter a valid email", NeighbourValidationError.INVALID_EMAIL.message)
        assertEquals("Enter a valid UK mobile number", NeighbourValidationError.INVALID_MOBILE.message)
        assertEquals("Enter a valid email", DomainException.InvalidNeighbour(NeighbourValidationError.INVALID_EMAIL).message)
    }
}

class EntityTest {
    @Test fun timelineCopyAndSeverity() {
        val cases = listOf(
            Triple(TimelineEventKind.Score(38), "Score 38", TimelineSeverity.CRITICAL),
            Triple(TimelineEventKind.Score(76), "Score 76", TimelineSeverity.OK),
            Triple(TimelineEventKind.MissedCheckIn, "Missed check-in", TimelineSeverity.CRITICAL),
            Triple(TimelineEventKind.NotificationSent(1), "Notification sent to 1st level", TimelineSeverity.CRITICAL),
            Triple(TimelineEventKind.Acknowledged(2), "2nd level acknowledged", TimelineSeverity.WARNING),
            Triple(TimelineEventKind.NeighbourReached, "Neighbour reached", TimelineSeverity.OK),
            Triple(TimelineEventKind.Escalated(3), "Escalation to 3rd level", TimelineSeverity.CRITICAL),
            Triple(TimelineEventKind.Escalated(4), "Escalation to 4th level", TimelineSeverity.CRITICAL),
            Triple(TimelineEventKind.Outcome(CareOutcome.ALL_OK), "Neighbour reports all OK", TimelineSeverity.OK),
            Triple(TimelineEventKind.Outcome(CareOutcome.IN_CARE), "In care in hospital", TimelineSeverity.WARNING),
            Triple(TimelineEventKind.Outcome(CareOutcome.RECOVERING), "Recovering", TimelineSeverity.OK),
            Triple(TimelineEventKind.Outcome(CareOutcome.RECOVERED), "Recovered", TimelineSeverity.OK),
            Triple(TimelineEventKind.Outcome(CareOutcome.BAD_CONDITION), "In bad condition", TimelineSeverity.CRITICAL),
            Triple(TimelineEventKind.Outcome(CareOutcome.ALIVE), "Confirmed alive", TimelineSeverity.OK),
            Triple(TimelineEventKind.Outcome(CareOutcome.DECEASED), "Reported deceased", TimelineSeverity.CRITICAL),
        )
        for ((kind, title, severity) in cases) {
            assertEquals(title, kind.title)
            assertEquals(severity, kind.severity, title)
        }
    }

    @Test fun careOutcomeResolution() {
        assertEquals(
            setOf(CareOutcome.ALL_OK, CareOutcome.RECOVERED, CareOutcome.ALIVE, CareOutcome.DECEASED),
            CareOutcome.entries.filter { it.resolvesAlert }.toSet(),
        )
    }

    @Test fun alertCountdown() {
        val alert = Alert("a", "o", "X", AlertRecipient.NEIGHBOUR, 38, T0, T0.plusSeconds(1200))
        assertEquals(1200, alert.secondsRemaining(T0))
        assertEquals(1, alert.secondsRemaining(T0.plusMillis(1_199_500)))
        assertEquals(0, alert.secondsRemaining(T0.plusSeconds(5000)))
        assertEquals(2, AlertRecipient.HOSPITAL.level)
        assertEquals(4, Role.entries.size)
    }

    @Test fun timeProviders() {
        assertEquals(T0, FixedTimeProvider(T0).now())
        assertTrue(abs(Duration.between(SystemTimeProvider.now(), java.time.Instant.now()).seconds) < 5)
    }
}
