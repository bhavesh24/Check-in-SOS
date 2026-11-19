import XCTest

final class HomeRobot: Robot {
    @discardableResult
    func assertShown() -> Self {
        assertTitle("Home")
        for id in ["home.role.olderAdult", "home.role.family", "home.role.hospital", "home.role.neighbour"] {
            waitFor(id)
        }
        assertMissing("nav.back")
        return self
    }

    func chooseOlderAdult() -> OlderAdultRobot { tap("home.role.olderAdult"); return OlderAdultRobot(app) }
    func chooseFamily() -> FamilyRobot { tap("home.role.family"); return FamilyRobot(app) }
    func chooseNeighbour() -> NeighbourRobot { tap("home.role.neighbour"); return NeighbourRobot(app) }
    func chooseHospital() -> HospitalRobot { tap("home.role.hospital"); return HospitalRobot(app) }
}
