import XCTest

final class OlderAdultFlowTests: UITestCase {
    func testHealthyAnswersScoreAboveThresholdWithoutNotification() {
        let robot = home.chooseOlderAdult().assertAssessmentShown()
        robot.assertEnabled("assessment.submit", false)
        robot.answer(physical: "good", mood: "pleasant")
            .submit()
            .assertScore(76, neighbourNotified: false)
            .done()
            .assertShown()
    }

    func testUnwellAnswersNotifyNeighbour() {
        home.chooseOlderAdult()
            .answer(physical: "ill", mood: "anxious")
            .submit()
            .assertScore(38, neighbourNotified: true)
    }

    func testChangingAnswersBeforeSubmitting() {
        home.chooseOlderAdult()
            .answer(physical: "verySick", mood: "depressed")
            .answer(physical: "fine", mood: "happy")
            .submit()
            .assertScore(100, neighbourNotified: false)
    }

    func testNoCheckInScreen() {
        home.chooseOlderAdult().openMissedCheckIn()
            .back(to: OlderAdultRobot.self)
            .assertAssessmentShown()
    }
}
