import XCTest

/// Base page object: finds elements by the shared test IDs from docs/screens.md.
class Robot {
    let app: XCUIApplication
    let timeout: TimeInterval = 5

    required init(_ app: XCUIApplication) { self.app = app }

    func element(_ id: String) -> XCUIElement {
        app.descendants(matching: .any).matching(identifier: id).firstMatch
    }

    @discardableResult
    func waitFor(_ id: String, file: StaticString = #filePath, line: UInt = #line) -> XCUIElement {
        let el = element(id)
        XCTAssertTrue(el.waitForExistence(timeout: timeout), "Missing element '\(id)'", file: file, line: line)
        return el
    }

    func tap(_ id: String, file: StaticString = #filePath, line: UInt = #line) {
        waitFor(id, file: file, line: line).tap()
    }

    /// Scrolls until `id` sits clear of the pinned action bar (whose top button is `barId`) and the
    /// keyboard, then returns it.
    @discardableResult
    func scrollIntoView(_ id: String, barId: String, file: StaticString = #filePath, line: UInt = #line) -> XCUIElement {
        let target = waitFor(id, file: file, line: line)
        var attempts = 0
        // The keyboard's suggestion bar is also a scroll view, so pick the one holding the target.
        let scrollView = app.scrollViews.containing(.any, identifier: id).firstMatch
        while target.frame.maxY > visibleBottom(barId: barId) - 24, attempts < 6 {
            scrollView.swipeUp(velocity: .slow)
            attempts += 1
        }
        return target
    }

    func tapAboveBar(_ id: String, barId: String, file: StaticString = #filePath, line: UInt = #line) {
        scrollIntoView(id, barId: barId, file: file, line: line).tap()
    }

    private func visibleBottom(barId: String) -> CGFloat {
        var bottom = app.windows.firstMatch.frame.maxY
        let bar = element(barId)
        if bar.exists { bottom = min(bottom, bar.frame.minY) }
        let keyboard = app.keyboards.firstMatch
        if keyboard.exists { bottom = min(bottom, keyboard.frame.minY) }
        return bottom
    }

    func assertLabel(_ id: String, _ expected: String, file: StaticString = #filePath, line: UInt = #line) {
        XCTAssertEqual(waitFor(id, file: file, line: line).label, expected, file: file, line: line)
    }

    func assertMissing(_ id: String, file: StaticString = #filePath, line: UInt = #line) {
        XCTAssertFalse(element(id).exists, "Unexpected element '\(id)'", file: file, line: line)
    }

    func assertTitle(_ title: String, file: StaticString = #filePath, line: UInt = #line) {
        XCTAssertTrue(app.navigationBars[title].waitForExistence(timeout: timeout),
                      "Expected screen '\(title)'", file: file, line: line)
    }

    func assertStep(_ step: Int, of total: Int, file: StaticString = #filePath, line: UInt = #line) {
        assertLabel("stepProgress", "Step \(step) of \(total)", file: file, line: line)
    }

    func assertEnabled(_ id: String, _ enabled: Bool, file: StaticString = #filePath, line: UInt = #line) {
        XCTAssertEqual(waitFor(id, file: file, line: line).isEnabled, enabled, id, file: file, line: line)
    }

    /// Taps `nav.back` and hands over to the previous screen's robot.
    @discardableResult
    func back<R: Robot>(to _: R.Type = R.self) -> R {
        tap("nav.back")
        return R(app)
    }

    /// Fails the test on any accessibility issue (contrast, hit size, Dynamic Type clipping, missing labels).
    func audit(file: StaticString = #filePath, line: UInt = #line) {
        // Content scrolled under the translucent navigation bar would fail contrast, so audit from the top.
        let scrollView = app.scrollViews.firstMatch
        if scrollView.exists {
            scrollView.swipeDown(velocity: .fast)
            scrollView.swipeDown(velocity: .fast)
        }
        do {
            try app.performAccessibilityAudit()
        } catch {
            XCTFail("Accessibility audit failed: \(error)", file: file, line: line)
        }
    }
}
