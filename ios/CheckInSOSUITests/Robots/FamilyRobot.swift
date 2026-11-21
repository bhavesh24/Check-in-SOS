import XCTest

final class FamilyRobot: Robot {
    @discardableResult
    func assertLovedOnesShown() -> Self {
        assertTitle("Your loved ones")
        XCTAssertTrue(waitFor("lovedOnes.row.oa-1").label.contains("Margaret Wilson"))
        XCTAssertTrue(element("lovedOnes.row.oa-1").label.contains("Score 76"))
        XCTAssertTrue(element("lovedOnes.row.oa-1").label.contains("Last check-in Today, 08:00"))
        XCTAssertTrue(element("lovedOnes.row.oa-2").label.contains("Score 38"))
        XCTAssertTrue(element("lovedOnes.row.oa-3").label.contains("No score yet"))
        return self
    }

    @discardableResult
    func openStatus(_ id: String, name: String) -> Self {
        tap("lovedOnes.row.\(id)")
        assertTitle(name)
        return self
    }

    @discardableResult
    func assertStatus(scoreLabel: String, neighbour: String) -> Self {
        assertLabel("status.score", scoreLabel)
        assertLabel("status.neighbour", "1st level: \(neighbour)")
        return self
    }

    /// "Last check-in …" line, and the trend chart's label (nil when it is hidden).
    @discardableResult
    func assertCheckIns(lastCheckIn: String, trend: String?) -> Self {
        assertLabel("status.lastCheckIn", lastCheckIn)
        if let trend { assertLabel("status.trend", trend) } else { assertMissing("status.trend") }
        return self
    }

    @discardableResult
    func editNeighbour() -> Self {
        tap("status.editNeighbour")
        assertTitle("Edit Neighbour")
        return self
    }

    @discardableResult
    func assertField(_ field: String, _ value: String) -> Self {
        XCTAssertEqual(waitFor("editNeighbour.\(field)").value as? String, value, field)
        return self
    }

    @discardableResult
    func replace(_ field: String, with text: String) -> Self {
        closeKeyboard()
        let input = scrollIntoView("editNeighbour.\(field)", barId: "editNeighbour.save")
        // Tap the trailing edge so the cursor lands after the existing text.
        input.coordinate(withNormalizedOffset: CGVector(dx: 0.97, dy: 0.5)).tap()
        let current = input.value as? String ?? ""
        input.typeText(String(repeating: XCUIKeyboardKey.delete.rawValue, count: current.count) + text)
        return self
    }

    @discardableResult
    func save() -> Self {
        closeKeyboard()
        tap("editNeighbour.save")
        return self
    }

    /// Taps the keyboard's "Done" (`keyboard.done`) when a keyboard is showing.
    func closeKeyboard() {
        let done = element("keyboard.done")
        if done.exists {
            done.tap()
            XCTAssertTrue(done.waitForNonExistence(timeout: timeout))
        }
    }

    @discardableResult
    func assertError(_ message: String) -> Self {
        assertLabel("editNeighbour.error", message)
        assertTitle("Edit Neighbour")
        return self
    }

    @discardableResult
    func openTimeline() -> Self {
        tap("status.timeline")
        assertTitle("Timeline")
        return self
    }

    @discardableResult
    func assertTimeline(_ events: [String]) -> Self {
        for (index, text) in events.enumerated() {
            assertLabel("timeline.event.\(index)", text)
        }
        assertMissing("timeline.event.\(events.count)")
        return self
    }
}
