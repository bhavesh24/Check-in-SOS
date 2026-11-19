package uk.checkinsos.app.robots

import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithText

class HospitalRobot(rule: ComposeTestRule) : Robot(rule) {
    fun assertListShown() = apply {
        assertTitle("Patients")
        waitFor("patients.row.p-1").assertTextContains("Arthur Patel")
        waitFor("patients.row.p-2")
        waitFor("patients.row.p-3")
    }

    fun simulateAlert() = apply {
        tap("patients.simulateAlert")
        assertTitle("Help")
        assertStep(1, of = 5)
        assertText("hospitalAlert.message", "The patient Arthur Patel needs your help, please acknowledge the notification")
        assertText("hospitalAlert.countdown", "20:00")
    }

    fun accept() = apply {
        tap("hospitalAlert.accept")
        assertQuestion("Have you taken them in your care?", step = 2)
    }

    /** Answers a step by test tag, e.g. "inCare.yes". */
    fun answer(tag: String) = apply { tap(tag) }

    fun assertQuestion(heading: String, step: Int) = apply {
        rule.waitForIdle()
        rule.onNodeWithText(heading).assertExists()
        assertStep(step, of = 5)
    }
}
