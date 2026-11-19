package uk.checkinsos.app.flows

import org.junit.Test
import uk.checkinsos.app.robots.OlderAdultRobot

class OlderAdultFlowTest : FlowTest() {
    @Test fun healthyAnswersScoreAboveThresholdWithoutNotification() {
        val robot = home.chooseOlderAdult().assertAssessmentShown()
        robot.assertEnabled("assessment.submit", false)
        robot.answer("good", "pleasant").submit().assertScore(76, neighbourNotified = false).done().assertShown()
    }

    @Test fun unwellAnswersNotifyNeighbour() {
        home.chooseOlderAdult().answer("ill", "anxious").submit().assertScore(38, neighbourNotified = true)
    }

    @Test fun changingAnswersBeforeSubmitting() {
        home.chooseOlderAdult()
            .answer("verySick", "depressed")
            .answer("fine", "happy")
            .submit()
            .assertScore(100, neighbourNotified = false)
    }

    @Test fun noCheckInScreen() {
        home.chooseOlderAdult().openMissedCheckIn().back(::OlderAdultRobot).assertAssessmentShown()
    }
}
