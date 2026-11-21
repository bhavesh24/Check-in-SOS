import XCTest

final class NeighbourRobot: Robot {
    @discardableResult
    func assertListShown() -> Self {
        assertTitle("Neighbours")
        for id in ["oa-1", "oa-2", "oa-3"] { waitFor("neighbours.row.\(id)") }
        return self
    }

    @discardableResult
    func simulateAlert() -> Self {
        tap("neighbours.simulateAlert")
        assertTitle("Urgent alert")
        assertStep(1, of: 4)
        assertLabel("alert.message",
                    "Your neighbour Arthur Patel needs your help, please acknowledge the notification")
        assertLabel("alert.countdown", "20:00")
        return self
    }

    @discardableResult
    func accept() -> Self {
        tap("alert.accept")
        assertTitle("On your way")
        assertStep(2, of: 4)
        assertLabel("reached.message",
                    "You have acknowledged to help your neighbour Arthur Patel, please let us know have you reached")
        return self
    }

    @discardableResult
    func confirmReached() -> Self {
        tap("reached.confirm")
        assertTitle("Escalate")
        assertStep(3, of: 4)
        waitFor("escalate.allOk")
        return self
    }

    @discardableResult
    func allOk() -> Self {
        tap("escalate.allOk")
        return assertListShown()
    }

    @discardableResult
    func alertEmergencyServices() -> Self {
        tap("escalate.alertServices")
        XCTAssertTrue(sheetButton("999").waitForExistence(timeout: timeout), "Emergency sheet not shown")
        XCTAssertTrue(app.staticTexts["Alert emergency services"].exists)
        return self
    }

    /// Cancel button on older iOS; on iOS 26+ the dialog is a popover without one, so tap outside it.
    @discardableResult
    func cancelSheet() -> Self {
        let cancel = sheetButton("Cancel", id: "emergency.cancel")
        if cancel.exists {
            cancel.tap()
        } else {
            app.otherElements["PopoverDismissRegion"].tap()
        }
        XCTAssertTrue(sheetButton("999").waitForNonExistence(timeout: timeout))
        waitFor("escalate.alertServices")
        return self
    }

    /// 999/111 never dial: the Alive screen shows the demo message instead.
    @discardableResult
    func call(_ number: String) -> Self {
        sheetButton(number).tap()
        assertTitle("Emergency")
        assertStep(4, of: 4)
        XCTAssertTrue(waitFor("emergency.demoMessage").label.contains("Demo: would call \(number)"))
        return self
    }

    @discardableResult
    func answerAlive(_ yes: Bool) -> Self {
        tap(yes ? "alive.yes" : "alive.no")
        return assertListShown()
    }

    private func sheetButton(_ label: String, id: String? = nil) -> XCUIElement {
        let byId = app.buttons.matching(identifier: id ?? "emergency.call\(label)").firstMatch
        return byId.exists ? byId : app.buttons.matching(NSPredicate(format: "label == %@", label)).firstMatch
    }
}
