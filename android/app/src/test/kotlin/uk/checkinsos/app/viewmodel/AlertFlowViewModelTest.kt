package uk.checkinsos.app.viewmodel

import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import uk.checkinsos.app.fakes.FakeStore
import uk.checkinsos.app.fakes.MainDispatcherRule
import uk.checkinsos.app.fakes.MutableTimeProvider
import uk.checkinsos.app.fakes.T0
import uk.checkinsos.app.fakes.collect
import uk.checkinsos.app.feature.hospital.carestep.CareStep
import uk.checkinsos.app.feature.hospital.carestep.CareStepViewModel
import uk.checkinsos.app.feature.hospital.patients.PatientsViewModel
import uk.checkinsos.app.feature.neighbour.alive.AliveViewModel
import uk.checkinsos.app.feature.neighbour.escalate.EscalateViewModel
import uk.checkinsos.app.feature.neighbour.neighbours.NeighboursViewModel
import uk.checkinsos.app.feature.neighbour.reached.ReachedViewModel
import uk.checkinsos.app.feature.shared.DisplayText
import uk.checkinsos.app.feature.shared.helpalert.HelpAlertViewModel
import uk.checkinsos.app.navigation.Routes
import uk.checkinsos.domain.model.AlertRecipient
import uk.checkinsos.domain.model.AlertStatus
import uk.checkinsos.domain.model.CareOutcome
import uk.checkinsos.domain.model.TimelineEventKind
import uk.checkinsos.domain.service.FixedTimeProvider
import uk.checkinsos.domain.usecase.AcknowledgeAlert
import uk.checkinsos.domain.usecase.EscalateAlert
import uk.checkinsos.domain.usecase.GetNeighbours
import uk.checkinsos.domain.usecase.GetPatients
import uk.checkinsos.domain.usecase.MarkArrived
import uk.checkinsos.domain.usecase.RecordCareOutcome
import uk.checkinsos.domain.usecase.SimulateAlert
import java.time.Duration
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NeighboursAndPatientsViewModelTest {
    @get:Rule val main = MainDispatcherRule()
    private val store = FakeStore()
    private val simulate = SimulateAlert(store, store, store, store, FixedTimeProvider(T0))

    @Test fun neighboursLoadAndSimulate() = runTest {
        val vm = NeighboursViewModel(GetNeighbours(store), simulate)
        val events = collect(vm.events)
        vm.load()
        assertEquals(listOf("Score 76", "Score 38"), vm.state.value.neighbours.map(vm.state.value::subtitle))
        vm.simulate()
        val alert = events.single().alert
        assertEquals("Arthur Patel", alert.subjectName)
        assertEquals(AlertRecipient.NEIGHBOUR, alert.recipient)
        assertEquals(T0.plus(Duration.ofMinutes(20)), alert.acknowledgeBy)
        assertFalse(vm.state.value.isBusy)
    }

    @Test fun patientsLoadAndSimulate() = runTest {
        val vm = PatientsViewModel(GetPatients(store), simulate)
        val events = collect(vm.events)
        vm.load()
        assertEquals(listOf("Score 38"), vm.state.value.patients.map(vm.state.value::subtitle))
        vm.simulate()
        assertEquals(AlertRecipient.HOSPITAL, events.single().alert.recipient)
    }

    @Test fun failuresShowError() = runTest {
        store.failing = true
        val neighbours = NeighboursViewModel(GetNeighbours(store), simulate)
        neighbours.load()
        assertEquals(DisplayText.GENERIC_ERROR, neighbours.state.value.errorMessage)
        neighbours.simulate()
        assertEquals(DisplayText.GENERIC_ERROR, neighbours.state.value.errorMessage)
        val patients = PatientsViewModel(GetPatients(store), simulate)
        patients.simulate()
        patients.load()
        assertEquals(DisplayText.GENERIC_ERROR, patients.state.value.errorMessage)
    }
}

class HelpAlertViewModelTest {
    @get:Rule val main = MainDispatcherRule()
    private val store = FakeStore()

    @Test fun neighbourAlertCountsDownAndAccepts() = runTest {
        val summary = store.seedAlert()
        val clock = MutableTimeProvider(T0)
        val vm = HelpAlertViewModel(summary, AcknowledgeAlert(store, store, clock), clock)
        val events = collect(vm.events)
        val state = vm.state.value
        assertEquals("Your neighbour Arthur Patel needs your help, please acknowledge the notification", state.message)
        assertEquals("alert", state.tagPrefix)
        assertEquals(1, state.step)
        assertEquals(4, state.totalSteps)
        assertEquals("20:00", state.countdownText)
        clock.instant = T0.plusSeconds(61)
        vm.tick()
        assertEquals("18:59", vm.state.value.countdownText)
        vm.tick()
        clock.instant = T0.plusSeconds(3600)
        vm.tick()
        assertEquals("Time expired", vm.state.value.countdownText)

        vm.accept()
        assertEquals(listOf(HelpAlertViewModel.Event(summary)), events)
        assertEquals(AlertStatus.ACKNOWLEDGED, store.alert(summary.id).status)
        assertEquals(listOf<TimelineEventKind>(TimelineEventKind.Acknowledged(1)), store.kinds)
    }

    @Test fun hospitalCopyAndFailure() = runTest {
        val summary = store.seedAlert(AlertRecipient.HOSPITAL)
        store.failing = true
        val vm = HelpAlertViewModel(summary, AcknowledgeAlert(store, store, FixedTimeProvider(T0)), FixedTimeProvider(T0))
        val events = collect(vm.events)
        assertEquals("The patient Arthur Patel needs your help, please acknowledge the notification", vm.state.value.message)
        assertEquals("hospitalAlert", vm.state.value.tagPrefix)
        assertEquals(5, vm.state.value.totalSteps)
        vm.accept()
        assertTrue(events.isEmpty())
        assertEquals(DisplayText.GENERIC_ERROR, vm.state.value.errorMessage)
    }

    @Test fun routesRoundTripTheAlert() = runTest {
        val summary = store.seedAlert(AlertRecipient.HOSPITAL)
        assertEquals(summary, Routes.helpAlert(summary).alert)
        assertEquals(summary, Routes.reached(summary).alert)
        assertEquals(summary, Routes.escalate(summary).alert)
        assertEquals("999", Routes.alive(summary, "999").calledNumber)
        assertEquals(CareStep.RECOVERING, Routes.careStep(CareStep.RECOVERING, summary).careStep)
    }
}

class NeighbourStepViewModelTest {
    @get:Rule val main = MainDispatcherRule()
    private val store = FakeStore()
    private val time = FixedTimeProvider(T0)
    private val record get() = RecordCareOutcome(store, store, time)

    @Test fun reachedMarksArrived() = runTest {
        val summary = store.seedAlert()
        val vm = ReachedViewModel(summary, MarkArrived(store, store, time))
        val events = collect(vm.events)
        assertEquals(
            "You have acknowledged to help your neighbour Arthur Patel, please let us know have you reached",
            vm.state.value.message,
        )
        assertEquals(2, vm.state.value.step)
        vm.confirm()
        assertEquals(listOf(ReachedViewModel.Event(summary)), events)
        assertEquals(listOf<TimelineEventKind>(TimelineEventKind.NeighbourReached), store.kinds)
        store.failing = true
        vm.confirm()
        assertEquals(DisplayText.GENERIC_ERROR, vm.state.value.errorMessage)
    }

    @Test fun escalateAllOkResolves() = runTest {
        val summary = store.seedAlert()
        val vm = EscalateViewModel(summary, record, EscalateAlert(store, store, time))
        val events = collect(vm.events)
        assertEquals("Do you want to escalate?", vm.state.value.heading)
        assertEquals(3, vm.state.value.step)
        vm.allOk()
        assertEquals(listOf<EscalateViewModel.Event>(EscalateViewModel.Event.AllOk), events)
        assertEquals(AlertStatus.RESOLVED, store.alert(summary.id).status)
    }

    @Test fun escalateOpensDialogThenDemoCallsOrCancels() = runTest {
        val summary = store.seedAlert()
        val vm = EscalateViewModel(summary, record, EscalateAlert(store, store, time))
        val events = collect(vm.events)
        vm.alertServices()
        assertTrue(vm.state.value.isEmergencyDialogShown)
        assertEquals(listOf<TimelineEventKind>(TimelineEventKind.Escalated(2)), store.kinds)
        vm.cancel()
        assertFalse(vm.state.value.isEmergencyDialogShown)
        vm.alertServices()
        assertEquals(listOf("999", "111"), vm.state.value.emergencyNumbers)
        vm.call("111")
        assertFalse(vm.state.value.isEmergencyDialogShown)
        assertEquals(listOf<EscalateViewModel.Event>(EscalateViewModel.Event.Called(summary, "111")), events)
        store.failing = true
        vm.allOk()
        assertEquals(DisplayText.GENERIC_ERROR, vm.state.value.errorMessage)
    }

    @Test fun aliveRecordsOutcome() = runTest {
        for ((alive, outcome) in listOf(true to CareOutcome.ALIVE, false to CareOutcome.DECEASED)) {
            val store = FakeStore()
            val summary = store.seedAlert()
            val vm = AliveViewModel(summary, "999", RecordCareOutcome(store, store, time))
            val events = collect(vm.events)
            assertEquals("Demo: would call 999", vm.state.value.demoMessage)
            assertEquals("Emergency services alerted, Is your neighbour alive?", vm.state.value.heading)
            assertEquals(4, vm.state.value.step)
            vm.answer(alive)
            assertEquals(1, events.size)
            assertEquals(listOf<TimelineEventKind>(TimelineEventKind.Outcome(outcome)), store.kinds)
        }
    }

    @Test fun aliveWithoutCallAndFailure() = runTest {
        val summary = store.seedAlert()
        val vm = AliveViewModel(summary, null, record)
        assertNull(vm.state.value.demoMessage)
        store.failing = true
        vm.answer(true)
        assertEquals(DisplayText.GENERIC_ERROR, vm.state.value.errorMessage)
    }
}

class CareStepViewModelTest {
    @get:Rule val main = MainDispatcherRule()
    private val store = FakeStore()

    @Test fun stepTableMatchesSpec() {
        assertEquals(listOf(2, 3, 4, 4, 5), CareStep.entries.map { it.stepNumber })
        assertEquals(
            listOf(
                "Have you taken them in your care?", "Are they recovering?", "Have they recovered?",
                "Are they in bad condition?", "Is your patient alive?",
            ),
            CareStep.entries.map { it.heading },
        )
        assertEquals(
            listOf(
                "inCare.yes", "recovering.yes", "recovering.no", "recovered.yes",
                "badCondition.yes", "badCondition.no", "patientAlive.no", "patientAlive.yes",
            ),
            CareStep.entries.flatMap { step -> step.answers.map { it.testTag } },
        )
        assertEquals(listOf(CareStep.RECOVERED, CareStep.BAD_CONDITION), CareStep.RECOVERING.answers.map { it.next })
        assertEquals(listOf(CareOutcome.DECEASED, CareOutcome.ALIVE), CareStep.PATIENT_ALIVE.answers.map { it.outcome })
    }

    @Test fun answerRecordsOutcomeAndAdvances() = runTest {
        val summary = store.seedAlert(AlertRecipient.HOSPITAL)
        val vm = CareStepViewModel(CareStep.RECOVERING, summary, RecordCareOutcome(store, store, FixedTimeProvider(T0)))
        val events = collect(vm.events)
        assertEquals(5, vm.state.value.totalSteps)
        vm.choose(CareStep.RECOVERING.answers[0])
        vm.choose(CareStep.RECOVERING.answers[1])
        assertEquals(listOf(CareStep.RECOVERED, CareStep.BAD_CONDITION), events.map { it.next })
        assertEquals(listOf<TimelineEventKind>(TimelineEventKind.Outcome(CareOutcome.RECOVERING)), store.kinds)
        store.failing = true
        vm.choose(CareStep.RECOVERING.answers[0])
        assertEquals(DisplayText.GENERIC_ERROR, vm.state.value.errorMessage)
        assertEquals(2, events.size)
    }
}
