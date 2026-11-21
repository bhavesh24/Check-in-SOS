package uk.checkinsos.app.viewmodel

import kotlinx.coroutines.test.runTest
import uk.checkinsos.app.fakes.FakeStore
import uk.checkinsos.app.fakes.MainDispatcherRule
import uk.checkinsos.app.fakes.T0
import uk.checkinsos.app.fakes.adult
import uk.checkinsos.app.fakes.collect
import uk.checkinsos.app.feature.family.editneighbour.EditNeighbourViewModel
import uk.checkinsos.app.feature.family.lovedones.LovedOnesViewModel
import uk.checkinsos.app.feature.family.status.StatusViewModel
import uk.checkinsos.app.feature.family.timeline.TimelineViewModel
import uk.checkinsos.app.feature.shared.DisplayText
import uk.checkinsos.app.feature.shared.ScoreBand
import uk.checkinsos.domain.model.CareOutcome
import uk.checkinsos.domain.model.TimelineEventKind
import uk.checkinsos.domain.model.TimelineSeverity
import uk.checkinsos.domain.service.FixedTimeProvider
import uk.checkinsos.domain.usecase.GetLovedOneStatus
import uk.checkinsos.domain.usecase.GetLovedOnes
import uk.checkinsos.domain.usecase.GetScoreHistory
import uk.checkinsos.domain.usecase.GetTimeline
import uk.checkinsos.domain.usecase.UpdateNeighbour
import java.time.Duration
import java.time.ZoneOffset
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.junit.Rule
import org.junit.Test

class LovedOnesViewModelTest {
    @get:Rule val main = MainDispatcherRule()
    private val store = FakeStore(
        listOf(
            adult("oa-1", "Margaret Wilson", 76).copy(lastCheckIn = T0.minusSeconds(3600)),
            adult("oa-2", "Arthur Patel", 38).copy(lastCheckIn = T0.minusSeconds(86_400)),
            adult("oa-4", "Ada Lane", 50).copy(lastCheckIn = T0.minusSeconds(2 * 86_400L)),
            adult("oa-3", "Edith Clarke", null),
        ),
    )

    private fun makeVM() = LovedOnesViewModel(GetLovedOnes(store), FixedTimeProvider(T0), ZoneOffset.UTC)

    @Test fun loadsAndSelects() = runTest {
        val vm = makeVM()
        val events = collect(vm.events)
        assertEquals("No check-in yet", vm.state.value.subtitle(adult("x", "X", null)))
        vm.load()
        val state = vm.state.value
        assertEquals(listOf("oa-1", "oa-2", "oa-4", "oa-3"), state.lovedOnes.map { it.id })
        assertEquals(
            listOf("Last check-in Today, 08:00", "Last check-in Yesterday, 09:00", "Last check-in 13 Jan, 09:00", "No check-in yet"),
            state.lovedOnes.map(state::subtitle),
        )
        vm.select("oa-3")
        assertEquals(listOf(LovedOnesViewModel.Event("oa-3")), events)
    }

    @Test fun failureShowsError() = runTest {
        store.failing = true
        val vm = makeVM()
        vm.load()
        assertEquals(DisplayText.GENERIC_ERROR, vm.state.value.errorMessage)
    }
}

class StatusViewModelTest {
    @get:Rule val main = MainDispatcherRule()
    private val store = FakeStore()

    private fun makeVM(id: String = "oa-1") =
        StatusViewModel(id, GetLovedOneStatus(store), GetScoreHistory(store), FixedTimeProvider(T0), ZoneOffset.UTC)

    @Test fun loadsStatusAndNavigates() = runTest {
        val vm = makeVM()
        val events = collect(vm.events)
        assertEquals("", vm.state.value.title)
        assertEquals("", vm.state.value.neighbourText)
        vm.load()
        assertEquals("Margaret Wilson", vm.state.value.title)
        assertEquals("1st level: Alex Brown", vm.state.value.neighbourText)
        assertEquals("Current score", vm.state.value.scoreCaption)
        assertEquals(ScoreBand.GOOD, vm.state.value.band)
        assertEquals("Alex Brown", vm.state.value.neighbourName)
        assertEquals("1st level neighbour", vm.state.value.neighbourCaption)
        assertEquals("No check-in yet", vm.state.value.lastCheckInText)
        assertFalse(vm.state.value.showsTrend)
        vm.editNeighbour()
        vm.openTimeline()
        assertEquals(listOf(StatusViewModel.Event.EditNeighbour("oa-1"), StatusViewModel.Event.Timeline("oa-1")), events)
    }

    @Test fun loadsLastCheckInAndTrend() = runTest {
        store.recordScore("oa-1", 72, T0.minusSeconds(86_400))
        store.recordScore("oa-1", 64, T0.minusSeconds(3600))
        val vm = makeVM()
        vm.load()
        val state = vm.state.value
        assertEquals("Last check-in Today, 08:00", state.lastCheckInText)
        assertEquals(ScoreBand.MILD, state.band)
        assertTrue(state.showsTrend)
        assertEquals(listOf(72, 64), state.history.map { it.score })
        assertEquals("Recent check-ins: 72, 64", state.trendAccessibilityText)
    }

    @Test fun missingPersonShowsError() = runTest {
        val vm = makeVM("nope")
        vm.load()
        assertEquals(DisplayText.GENERIC_ERROR, vm.state.value.errorMessage)
    }
}

class EditNeighbourViewModelTest {
    @get:Rule val main = MainDispatcherRule()
    private val store = FakeStore()
    private fun vm(id: String = "oa-1") = EditNeighbourViewModel(id, GetLovedOneStatus(store), UpdateNeighbour(store))

    @Test fun prefillsOnceWithNationalNumber() = runTest {
        val vm = vm()
        vm.load()
        val s = vm.state.value
        assertEquals(listOf("Alex", "", "Brown", "alex@example.com", "7700900123"), listOf(s.firstName, s.middleName, s.lastName, s.email, s.mobile))
        vm.onFirstName("Sam")
        vm.load()
        assertEquals("Sam", vm.state.value.firstName)
        assertEquals("Your 1st level notified person", s.heading)
        assertEquals("+44", s.mobilePrefix)
    }

    @Test fun savesValidNeighbour() = runTest {
        val vm = vm()
        val events = collect(vm.events)
        vm.load()
        vm.onFirstName("Sam")
        vm.onMiddleName("J")
        vm.onLastName("Green")
        vm.onEmail("sam@example.com")
        vm.onMobile("7700 900321")
        vm.save()
        assertEquals(1, events.size)
        assertNull(vm.state.value.errorMessage)
        val saved = store.olderAdult("oa-1").neighbour
        assertEquals("Sam Green", saved.displayName)
        assertEquals("07700 900321", saved.mobile)
    }

    @Test fun validationErrors() = runTest {
        val cases = listOf(
            listOf("", "Brown", "a@example.com", "7700900123") to "First name is required",
            listOf("Sam", " ", "a@example.com", "7700900123") to "Last name is required",
            listOf("Sam", "Brown", "nope", "7700900123") to "Enter a valid email",
            listOf("Sam", "Brown", "a@example.com", "12") to "Enter a valid UK mobile number",
        )
        for ((fields, message) in cases) {
            val vm = vm()
            val events = collect(vm.events)
            vm.load()
            vm.onFirstName(fields[0])
            vm.onLastName(fields[1])
            vm.onEmail(fields[2])
            vm.onMobile(fields[3])
            vm.save()
            assertEquals(message, vm.state.value.errorMessage)
            assertTrue(events.isEmpty())
        }
    }

    @Test fun failuresShowGenericError() = runTest {
        val missing = vm("nope")
        missing.load()
        assertEquals(DisplayText.GENERIC_ERROR, missing.state.value.errorMessage)
        assertFalse(missing.state.value.isLoaded)

        val vm = vm()
        vm.load()
        store.failing = true
        vm.save()
        assertEquals(DisplayText.GENERIC_ERROR, vm.state.value.errorMessage)
    }

    @Test fun mobileNumberConversion() {
        assertEquals("7700900123", EditNeighbourViewModel.nationalNumber("07700900123"))
        assertEquals("+447700900123", EditNeighbourViewModel.nationalNumber("+447700900123"))
        assertEquals("07700 900123", EditNeighbourViewModel.storedNumber(" 7700 900123 "))
        assertEquals("07700900123", EditNeighbourViewModel.storedNumber("07700900123"))
        assertEquals("+447700900123", EditNeighbourViewModel.storedNumber("+447700900123"))
        assertEquals("", EditNeighbourViewModel.storedNumber(""))
    }
}

class TimelineViewModelTest {
    @get:Rule val main = MainDispatcherRule()

    @Test fun rowsOldestFirstWithTimesAndColours() = runTest {
        val store = FakeStore()
        store.append("oa-2", TimelineEventKind.Outcome(CareOutcome.RECOVERED), T0)
        store.append("oa-2", TimelineEventKind.Score(38), T0.minus(Duration.ofHours(3)))
        val vm = TimelineViewModel("oa-2", GetTimeline(store), ZoneOffset.UTC)
        assertFalse(vm.state.value.isEmpty)
        vm.load()
        val rows = vm.state.value.rows
        assertEquals(listOf("Score 38", "Recovered"), rows.map { it.title })
        assertEquals(listOf("06:00", "09:00"), rows.map { it.time })
        assertEquals(listOf(TimelineSeverity.CRITICAL, TimelineSeverity.OK), rows.map { it.severity })
        assertEquals(listOf(TimelineEventKind.Score(38), TimelineEventKind.Outcome(CareOutcome.RECOVERED)), rows.map { it.kind })
        assertEquals(listOf("timeline.event.0", "timeline.event.1"), rows.map { it.testTag })
        assertFalse(vm.state.value.isEmpty)
    }

    @Test fun emptyAndFailure() = runTest {
        val store = FakeStore()
        val vm = TimelineViewModel("oa-3", GetTimeline(store), ZoneOffset.UTC)
        vm.load()
        assertTrue(vm.state.value.isEmpty)
        assertEquals("No events yet", vm.state.value.emptyText)
        store.failing = true
        val failing = TimelineViewModel("oa-3", GetTimeline(store), ZoneOffset.UTC)
        failing.load()
        assertEquals(DisplayText.GENERIC_ERROR, failing.state.value.errorMessage)
    }
}
