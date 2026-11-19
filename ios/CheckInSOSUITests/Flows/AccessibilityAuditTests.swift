import XCTest

/// `performAccessibilityAudit()` on each role's main screens: contrast, hit-target size, Dynamic Type
/// clipping and missing labels. Any issue fails the build.
final class AccessibilityAuditTests: UITestCase {
    func testHomeAndOlderAdultScreens() {
        home.audit()
        let robot = home.chooseOlderAdult().answer(physical: "ill", mood: "anxious")
        robot.audit()
        robot.submit().audit()
    }

    func testFamilyScreens() {
        let robot = home.chooseFamily().assertLovedOnesShown()
        robot.audit()
        robot.openStatus("oa-2", name: "Arthur Patel").audit()
        robot.openTimeline().audit()
        robot.back(to: FamilyRobot.self).editNeighbour().audit()
    }

    func testNeighbourScreens() {
        let robot = home.chooseNeighbour().assertListShown()
        robot.audit()
        robot.simulateAlert().audit()
        robot.accept().audit()
        robot.confirmReached().audit()
        robot.alertEmergencyServices().call("999").audit()
    }

    func testHospitalScreens() {
        let robot = home.chooseHospital().assertListShown()
        robot.audit()
        robot.simulateAlert().audit()
        robot.accept().audit()
    }
}
