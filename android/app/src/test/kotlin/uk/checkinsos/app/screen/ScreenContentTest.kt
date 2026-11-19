package uk.checkinsos.app.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.checkinsos.app.designsystem.CheckInTheme
import uk.checkinsos.app.fakes.T0
import uk.checkinsos.app.fakes.adult
import uk.checkinsos.app.feature.family.editneighbour.EditNeighbourActions
import uk.checkinsos.app.feature.family.editneighbour.EditNeighbourContent
import uk.checkinsos.app.feature.family.editneighbour.EditNeighbourUiState
import uk.checkinsos.app.feature.family.lovedones.LovedOnesContent
import uk.checkinsos.app.feature.family.lovedones.LovedOnesUiState
import uk.checkinsos.app.feature.family.status.StatusContent
import uk.checkinsos.app.feature.family.status.StatusUiState
import uk.checkinsos.app.feature.family.timeline.TimelineContent
import uk.checkinsos.app.feature.family.timeline.TimelineUiState
import uk.checkinsos.app.feature.hospital.carestep.CareStep
import uk.checkinsos.app.feature.hospital.carestep.CareStepContent
import uk.checkinsos.app.feature.hospital.carestep.CareStepUiState
import uk.checkinsos.app.feature.hospital.patients.PatientsContent
import uk.checkinsos.app.feature.hospital.patients.PatientsUiState
import uk.checkinsos.app.feature.neighbour.alive.AliveContent
import uk.checkinsos.app.feature.neighbour.alive.AliveUiState
import uk.checkinsos.app.feature.neighbour.escalate.EscalateActions
import uk.checkinsos.app.feature.neighbour.escalate.EscalateContent
import uk.checkinsos.app.feature.neighbour.escalate.EscalateUiState
import uk.checkinsos.app.feature.neighbour.neighbours.NeighboursContent
import uk.checkinsos.app.feature.neighbour.neighbours.NeighboursUiState
import uk.checkinsos.app.feature.neighbour.reached.ReachedContent
import uk.checkinsos.app.feature.neighbour.reached.ReachedUiState
import uk.checkinsos.app.feature.olderadult.assessment.AssessmentContent
import uk.checkinsos.app.feature.olderadult.assessment.AssessmentUiState
import uk.checkinsos.app.feature.shared.AlertSummary
import uk.checkinsos.app.feature.shared.DisplayText
import uk.checkinsos.app.feature.shared.helpalert.HelpAlertContent
import uk.checkinsos.app.feature.shared.helpalert.HelpAlertUiState
import uk.checkinsos.domain.model.AlertRecipient
import uk.checkinsos.domain.model.PhysicalWellbeing
import kotlin.test.assertEquals

/** Each screen rendered from a fixed UI state (Robolectric): error, busy and dark-theme states the flows don't reach. */
@RunWith(AndroidJUnit4::class)
class ScreenContentTest {
    @get:Rule val rule = createComposeRule()

    private val alert = AlertSummary("a1", "Arthur Patel", AlertRecipient.NEIGHBOUR, T0.plusSeconds(1200))
    private val error = DisplayText.GENERIC_ERROR

    private fun show(dark: Boolean = false, content: @Composable () -> Unit) =
        rule.setContent { CheckInTheme(darkTheme = dark) { content() } }

    private fun assertError(tag: String = "error.message") {
        rule.onNodeWithTag(tag).assertTextContains(error)
    }

    @Test fun assessmentShowsErrorAndDisablesSubmitWhileSubmitting() {
        show(dark = true) {
            AssessmentContent(
                AssessmentUiState(PhysicalWellbeing.GOOD, null, isSubmitting = true, errorMessage = error),
                {}, {}, {}, {}, {},
            )
        }
        assertError()
        rule.onNodeWithTag("assessment.submit").assertIsNotEnabled()
    }

    @Test fun familyScreensShowErrors() {
        var selected = ""
        show { LovedOnesContent(LovedOnesUiState(listOf(adult("oa-9", "Test Person", null)), error), {}) { selected = it } }
        assertError()
        rule.onNodeWithTag("lovedOnes.row.oa-9").performClick()
        assertEquals("oa-9", selected)
    }

    @Test fun statusWithoutDataShowsOnlyError() {
        show { StatusContent(StatusUiState(adult = null, errorMessage = error), {}, {}, {}) }
        assertError()
        rule.onNodeWithTag("status.score").assertDoesNotExist()
    }

    @Test fun timelineError() {
        show { TimelineContent(TimelineUiState(errorMessage = error), {}) }
        assertError()
        rule.onNodeWithTag("timeline.empty").assertDoesNotExist()
    }

    @Test fun editNeighbourShowsErrorAndDisablesSaveWhileSaving() {
        var saved = false
        val actions = EditNeighbourActions({}, {}, {}, {}, {}, { saved = true })
        show { EditNeighbourContent(EditNeighbourUiState(isSaving = true, errorMessage = error), {}, actions) }
        assertError("editNeighbour.error")
        rule.onNodeWithTag("editNeighbour.save").assertIsNotEnabled()
        assertEquals(false, saved)
    }

    @Test fun listsShowErrorsAndBusyButtons() {
        show {
            androidx.compose.foundation.layout.Column {
                NeighboursContent(NeighboursUiState(isBusy = true, errorMessage = error), {}, {})
            }
        }
        assertError()
        rule.onNodeWithTag("neighbours.simulateAlert").assertIsNotEnabled()
    }

    @Test fun patientsBusy() {
        show { PatientsContent(PatientsUiState(isBusy = true, errorMessage = error), {}, {}) }
        assertError()
        rule.onNodeWithTag("patients.simulateAlert").assertIsNotEnabled()
    }

    @Test fun helpAlertExpiredAndBusy() {
        show { HelpAlertContent(HelpAlertUiState(alert, "Time expired", isBusy = true, errorMessage = error), {}, {}) }
        rule.onNodeWithTag("alert.countdown").assertTextContains("Time expired")
        rule.onNodeWithTag("alert.accept").assertIsNotEnabled()
        assertError()
    }

    @Test fun neighbourStepsShowErrors() {
        show { ReachedContent(ReachedUiState(alert, isBusy = true, errorMessage = error), {}, {}) }
        assertError()
        rule.onNodeWithTag("reached.confirm").assertIsNotEnabled()
    }

    @Test fun escalateDialogCancel() {
        var cancelled = false
        val actions = EscalateActions({}, {}, {}, { cancelled = true })
        show { EscalateContent(EscalateUiState(alert, isEmergencyDialogShown = true, errorMessage = error), {}, actions) }
        rule.onNodeWithText("Alert emergency services").assertExists()
        rule.onNodeWithText("Do you want to call").assertExists()
        rule.onNodeWithTag("emergency.cancel").performClick()
        assertEquals(true, cancelled)
    }

    @Test fun aliveWithoutCallHasNoDemoBanner() {
        show { AliveContent(AliveUiState(alert, calledNumber = null, isBusy = true, errorMessage = error), {}, {}) }
        rule.onNodeWithTag("emergency.demoMessage").assertDoesNotExist()
        rule.onNodeWithTag("alive.yes").assertIsNotEnabled()
        assertError()
    }

    @Test fun careStepBusyAndError() {
        show { CareStepContent(CareStepUiState(CareStep.PATIENT_ALIVE, alert, isBusy = true, errorMessage = error), {}, {}) }
        rule.onNodeWithTag("patientAlive.no").assertIsNotEnabled()
        rule.onNodeWithTag("patientAlive.yes").assertIsNotEnabled()
        assertError()
    }
}
