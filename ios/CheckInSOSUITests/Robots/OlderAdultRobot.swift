import XCTest

final class OlderAdultRobot: Robot {
    @discardableResult
    func assertAssessmentShown() -> Self {
        assertTitle("Assessment")
        assertStep(1, of: 2)
        return self
    }

    @discardableResult
    func answer(physical: String, mood: String) -> Self {
        tapAboveBar("assessment.physical.\(physical)", barId: "assessment.submit")
        tapAboveBar("assessment.mood.\(mood)", barId: "assessment.submit")
        XCTAssertTrue(element("assessment.physical.\(physical)").isSelected)
        XCTAssertTrue(element("assessment.mood.\(mood)").isSelected)
        return self
    }

    @discardableResult
    func submit() -> Self {
        tap("assessment.submit")
        assertTitle("Score")
        assertStep(2, of: 2)
        return self
    }

    @discardableResult
    func assertScore(_ score: Int, neighbourNotified: Bool) -> Self {
        assertLabel("score.value", "Score \(score) out of 100")
        if neighbourNotified {
            assertLabel("score.neighbourNotified", "A notification has been sent to your neighbour")
        } else {
            assertMissing("score.neighbourNotified")
        }
        if score >= 70 {
            assertLabel("score.reassurance", "You're doing well today.")
        } else {
            assertMissing("score.reassurance")
        }
        return self
    }

    func done() -> HomeRobot { tap("score.done"); return HomeRobot(app) }

    @discardableResult
    func openMissedCheckIn() -> Self {
        tapAboveBar("assessment.demoNoCheckIn", barId: "assessment.submit")
        assertTitle("No Check-in")
        assertLabel("noCheckIn.message",
                    "As you have not checked in, a notification has been sent to your neighbour")
        return self
    }
}
