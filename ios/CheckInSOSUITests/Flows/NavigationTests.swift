import XCTest

/// Back works from every screen, and the step indicator follows it.
final class NavigationTests: UITestCase {
    func testBackFromOlderAdultScreens() {
        let robot = home.chooseOlderAdult()
            .answer(physical: "good", mood: "happy")
            .submit()
        robot.back(to: OlderAdultRobot.self).assertAssessmentShown()
        robot.back(to: HomeRobot.self).assertShown()
    }

    func testBackFromFamilyScreens() {
        let robot = home.chooseFamily().openStatus("oa-1", name: "Margaret Wilson").openTimeline()
        robot.back(to: FamilyRobot.self).assertTitle("Margaret Wilson")
        robot.editNeighbour().back(to: FamilyRobot.self).assertTitle("Margaret Wilson")
        robot.back(to: FamilyRobot.self).assertLovedOnesShown()
        robot.back(to: HomeRobot.self).assertShown()
    }

    func testBackThroughNeighbourFlow() {
        let robot = home.chooseNeighbour().simulateAlert().accept().confirmReached()
            .alertEmergencyServices().call("999")
        robot.back(to: NeighbourRobot.self).assertStep(3, of: 4)
        robot.back(to: NeighbourRobot.self).assertStep(2, of: 4)
        robot.back(to: NeighbourRobot.self).assertStep(1, of: 4)
        robot.back(to: NeighbourRobot.self).assertListShown()
        robot.back(to: HomeRobot.self).assertShown()
    }

    func testBackThroughHospitalFlow() {
        let robot = home.chooseHospital().simulateAlert().accept()
            .answer("inCare.yes").answer("recovering.no").answer("badCondition.yes")
        robot.back(to: HospitalRobot.self).assertQuestion("Are they in bad condition?", step: 4)
        robot.back(to: HospitalRobot.self).assertQuestion("Are they recovering?", step: 3)
        robot.back(to: HospitalRobot.self).assertQuestion("Have you taken them in your care?", step: 2)
        robot.back(to: HospitalRobot.self).assertStep(1, of: 5)
        robot.back(to: HospitalRobot.self).assertListShown()
        robot.back(to: HomeRobot.self).assertShown()
    }
}
