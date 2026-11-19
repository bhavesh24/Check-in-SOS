import XCTest

/// Landscape support (docs/screens.md, Global rules → Orientation): each role's flow still works at the
/// short landscape height, including the pinned action bar screens, and passes the accessibility audit.
final class LandscapeTests: UITestCase {
    override func setUpWithError() throws {
        try super.setUpWithError()
        XCUIDevice.shared.orientation = .landscapeLeft
    }

    override func tearDownWithError() throws {
        XCUIDevice.shared.orientation = .portrait
        try super.tearDownWithError()
    }

    func testOlderAdultInLandscape() {
        home.audit()
        let robot = home.chooseOlderAdult().assertAssessmentShown().answer(physical: "ill", mood: "anxious")
        robot.audit(barId: "assessment.submit")
        robot.submit().assertScore(38, neighbourNotified: true).audit()
    }

    func testNeighbourInLandscape() {
        let robot = home.chooseNeighbour().assertListShown()
        robot.simulateAlert().audit()
        robot.accept().confirmReached().audit()
    }

    func testRotatingKeepsState() {
        let robot = home.chooseOlderAdult().answer(physical: "ill", mood: "anxious")
        XCUIDevice.shared.orientation = .portrait
        XCTAssertTrue(robot.element("assessment.physical.ill").isSelected)
        XCTAssertTrue(robot.element("assessment.mood.anxious").isSelected)
        robot.submit().assertScore(38, neighbourNotified: true)
    }
}
