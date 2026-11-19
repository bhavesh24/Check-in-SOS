import XCTest

final class HospitalRobot: Robot {
    @discardableResult
    func assertListShown() -> Self {
        assertTitle("Patients")
        XCTAssertTrue(waitFor("patients.row.p-1").label.contains("Arthur Patel"))
        waitFor("patients.row.p-2")
        waitFor("patients.row.p-3")
        return self
    }

    @discardableResult
    func simulateAlert() -> Self {
        tap("patients.simulateAlert")
        assertTitle("Help")
        assertStep(1, of: 5)
        assertLabel("hospitalAlert.message",
                    "The patient Arthur Patel needs your help, please acknowledge the notification")
        assertLabel("hospitalAlert.countdown", "20:00")
        return self
    }

    @discardableResult
    func accept() -> Self {
        tap("hospitalAlert.accept")
        return assertQuestion("Have you taken them in your care?", step: 2)
    }

    /// Answers a step by test ID, e.g. "inCare.yes".
    @discardableResult
    func answer(_ id: String) -> Self { tap(id); return self }

    @discardableResult
    func assertQuestion(_ heading: String, step: Int) -> Self {
        XCTAssertTrue(app.staticTexts[heading].waitForExistence(timeout: timeout), "Missing '\(heading)'")
        assertStep(step, of: 5)
        return self
    }
}
