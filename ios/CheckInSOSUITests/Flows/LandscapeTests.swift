import XCTest

/// Landscape support (docs/screens.md, Global rules → Orientation): each role's flow still works at the
/// short landscape height, including the pinned action bar screens, and passes the accessibility audit.
final class LandscapeTests: UITestCase {
    override func setUpWithError() throws {
        try super.setUpWithError()
        rotate(to: .landscapeLeft)
    }

    override func tearDownWithError() throws {
        XCUIDevice.shared.orientation = .portrait
        try super.tearDownWithError()
    }

    func testOlderAdultInLandscape() {
        home.audit()
        let robot = home.chooseOlderAdult().assertAssessmentShown().answer(physical: "ill", mood: "anxious")
        robot.audit(ignoringCutByBar: true)
        robot.submit().assertScore(38, neighbourNotified: true).audit(ignoringCutByBar: true)
    }

    func testNeighbourInLandscape() {
        let robot = home.chooseNeighbour().assertListShown()
        robot.simulateAlert().audit(ignoringCutByBar: true)
        robot.accept().confirmReached().audit(ignoringCutByBar: true)
    }

    func testRotatingKeepsState() {
        let robot = home.chooseOlderAdult().answer(physical: "ill", mood: "anxious")
        rotate(to: .portrait)
        XCTAssertTrue(robot.element("assessment.physical.ill").isSelected)
        XCTAssertTrue(robot.element("assessment.mood.anxious").isSelected)
        robot.submit().assertScore(38, neighbourNotified: true)
    }

    /// Rotates and waits until the window has re-laid out in the new orientation.
    private func rotate(to orientation: UIDeviceOrientation) {
        XCUIDevice.shared.orientation = orientation
        let window = app.windows.firstMatch
        let laidOut = expectation(for: NSPredicate { _, _ in
            let size = window.frame.size
            return orientation.isLandscape ? size.width > size.height : size.height > size.width
        }, evaluatedWith: nil)
        wait(for: [laidOut], timeout: 5)
    }
}
