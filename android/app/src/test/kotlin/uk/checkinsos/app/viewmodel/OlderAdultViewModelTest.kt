package uk.checkinsos.app.viewmodel

import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import uk.checkinsos.app.fakes.FakeStore
import uk.checkinsos.app.fakes.MainDispatcherRule
import uk.checkinsos.app.fakes.T0
import uk.checkinsos.app.fakes.collect
import uk.checkinsos.app.feature.home.HomeViewModel
import uk.checkinsos.app.feature.olderadult.assessment.AssessmentViewModel
import uk.checkinsos.app.feature.olderadult.assessment.testTag
import uk.checkinsos.app.feature.olderadult.assessment.title
import uk.checkinsos.app.feature.olderadult.nocheckin.NoCheckInViewModel
import uk.checkinsos.app.feature.olderadult.score.ScoreUiState
import uk.checkinsos.app.feature.olderadult.score.ScoreViewModel
import uk.checkinsos.app.feature.shared.DisplayText
import uk.checkinsos.app.navigation.Routes
import uk.checkinsos.domain.model.AssessmentResult
import uk.checkinsos.domain.model.Mood
import uk.checkinsos.domain.model.PhysicalWellbeing
import uk.checkinsos.domain.model.Role
import uk.checkinsos.app.feature.shared.ScoreBand
import uk.checkinsos.domain.service.FixedTimeProvider
import uk.checkinsos.domain.usecase.SubmitAssessment
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HomeViewModelTest {
    @get:Rule val main = MainDispatcherRule()

    @Test fun optionsMatchSpecOrderAndTags() = runTest {
        val vm = HomeViewModel()
        val events = collect(vm.events)
        assertEquals(listOf("Old age adult", "Concerned family", "Hospital", "Neighbour"), vm.state.value.options.map { it.title })
        assertEquals(
            listOf("home.role.olderAdult", "home.role.family", "home.role.hospital", "home.role.neighbour"),
            vm.state.value.options.map { it.testTag },
        )
        assertEquals(
            listOf("Do your daily check-in", "See how your loved ones are doing", "Respond to patient alerts", "Help someone living nearby"),
            vm.state.value.options.map { it.description },
        )
        vm.select(Role.HOSPITAL)
        assertEquals(listOf(HomeViewModel.Event(Role.HOSPITAL)), events)
    }

    @Test fun rolesMapToFirstScreens() {
        assertEquals(Routes.Assessment, Routes.forRole(Role.OLDER_ADULT))
        assertEquals(Routes.LovedOnes, Routes.forRole(Role.FAMILY))
        assertEquals(Routes.Patients, Routes.forRole(Role.HOSPITAL))
        assertEquals(Routes.Neighbours, Routes.forRole(Role.NEIGHBOUR))
    }
}

class AssessmentViewModelTest {
    @get:Rule val main = MainDispatcherRule()
    private val store = FakeStore()
    private fun vm() = AssessmentViewModel(SubmitAssessment(store, store, store, FixedTimeProvider(T0)), "oa-1")

    @Test fun submitDisabledUntilBothAnswered() = runTest {
        val vm = vm()
        val events = collect(vm.events)
        assertFalse(vm.state.value.canSubmit)
        vm.select(PhysicalWellbeing.GOOD)
        assertFalse(vm.state.value.canSubmit)
        vm.submit()
        assertTrue(events.isEmpty())
        vm.select(Mood.PLEASANT)
        assertTrue(vm.state.value.canSubmit)
        assertEquals(1, vm.state.value.step)
        assertEquals(2, vm.state.value.totalSteps)
    }

    @Test fun goodAnswersScore76WithoutNotification() = runTest {
        val vm = vm()
        val events = collect(vm.events)
        vm.select(PhysicalWellbeing.GOOD)
        vm.select(Mood.PLEASANT)
        vm.submit()
        assertEquals(listOf(AssessmentViewModel.Event.Submitted(AssessmentResult(76, false))), events)
        assertFalse(vm.state.value.isSubmitting)
    }

    @Test fun unwellAnswersScore38AndNotify() = runTest {
        val vm = vm()
        val events = collect(vm.events)
        vm.select(PhysicalWellbeing.ILL)
        vm.select(Mood.ANXIOUS)
        vm.submit()
        assertEquals(listOf(AssessmentViewModel.Event.Submitted(AssessmentResult(38, true))), events)
    }

    @Test fun failureShowsError() = runTest {
        store.failing = true
        val vm = vm()
        val events = collect(vm.events)
        vm.select(PhysicalWellbeing.FINE)
        vm.select(Mood.HAPPY)
        vm.submit()
        assertTrue(events.isEmpty())
        assertEquals(DisplayText.GENERIC_ERROR, vm.state.value.errorMessage)
    }

    @Test fun missedCheckInLinkAndCopy() = runTest {
        val vm = vm()
        val events = collect(vm.events)
        vm.showMissedCheckIn()
        assertEquals(listOf<AssessmentViewModel.Event>(AssessmentViewModel.Event.MissedCheckIn), events)
        assertEquals(listOf("Fine", "Good", "Ill", "Very sick"), PhysicalWellbeing.entries.map { it.title })
        assertEquals("assessment.physical.verySick", PhysicalWellbeing.VERY_SICK.testTag)
        assertEquals(listOf("Happy", "Pleasant", "Anxious", "Depressed"), Mood.entries.map { it.title })
        assertEquals("assessment.mood.depressed", Mood.DEPRESSED.testTag)
    }
}

class ScoreAndSharedTest {
    @get:Rule val main = MainDispatcherRule()

    @Test fun scoreDone() = runTest {
        val vm = ScoreViewModel(38, neighbourNotified = true)
        val events = collect(vm.events)
        assertEquals("A notification has been sent to your neighbour", vm.state.value.notificationText)
        assertEquals("You're doing well today.", vm.state.value.reassuranceText)
        assertEquals(2, vm.state.value.step)
        assertEquals(ScoreBand.MODERATE, vm.state.value.band)
        assertFalse(vm.state.value.showsReassurance)
        val ring = { score: Int -> ScoreUiState(score, neighbourNotified = false) }
        assertEquals(
            listOf(ScoreBand.GOOD, ScoreBand.GOOD, ScoreBand.MILD, ScoreBand.MILD, ScoreBand.MODERATE, ScoreBand.MODERATE, ScoreBand.SEVERE, ScoreBand.SEVERE),
            listOf(100, 70, 69, 50, 49, 30, 29, 0).map { ring(it).band },
        )
        assertTrue(ring(70).showsReassurance && !ring(69).showsReassurance)
        vm.done()
        assertEquals(1, events.size)
    }

    @Test fun noCheckInCopy() {
        assertEquals(
            "As you have not checked in, a notification has been sent to your neighbour",
            NoCheckInViewModel().state.value.message,
        )
    }

    @Test fun sharedCopy() {
        assertEquals("Score 76", DisplayText.score(76))
        assertEquals("No score yet", DisplayText.score(null))
        assertEquals("MW", DisplayText.initials("Margaret Wilson"))
        assertEquals("TH", DisplayText.initials("Tom J Hughes"))
        assertEquals("C", DisplayText.initials("cher"))
        assertEquals("20:00", DisplayText.countdown(1200))
        assertEquals("01:01", DisplayText.countdown(61))
        assertEquals("Time expired", DisplayText.countdown(0))
        assertEquals("Step 2 of 4", uk.checkinsos.app.designsystem.stepText(2, 4))
        assertEquals("Score 38 out of 100", uk.checkinsos.app.designsystem.scoreAccessibilityLabel(38))
        assertEquals("No score yet", uk.checkinsos.app.designsystem.scoreAccessibilityLabel(null))
        assertNull(null)
    }
}
