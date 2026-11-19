package uk.checkinsos.app.robots

import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.ComposeTestRule

class OlderAdultRobot(rule: ComposeTestRule) : Robot(rule) {
    fun assertAssessmentShown() = apply {
        assertTitle("Assessment")
        assertStep(1, of = 2)
    }

    fun answer(physical: String, mood: String) = apply {
        tap("assessment.physical.$physical")
        tap("assessment.mood.$mood")
        node("assessment.physical.$physical").assertIsSelected()
        node("assessment.mood.$mood").assertIsSelected()
    }

    fun submit() = apply {
        tap("assessment.submit")
        assertTitle("Score")
        assertStep(2, of = 2)
    }

    fun assertScore(score: Int, neighbourNotified: Boolean) = apply {
        assertLabel("score.value", "Score $score out of 100")
        if (neighbourNotified) {
            assertText("score.neighbourNotified", "A notification has been sent to your neighbour")
        } else {
            assertMissing("score.neighbourNotified")
        }
    }

    fun done() = HomeRobot(rule).also { tap("score.done") }

    fun openMissedCheckIn() = apply {
        tap("assessment.demoNoCheckIn")
        assertTitle("No Check-in")
        assertText("noCheckIn.message", "As you have not checked in, a notification has been sent to your neighbour")
    }
}
