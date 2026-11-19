package uk.checkinsos.app.robots

import androidx.compose.ui.test.junit4.ComposeTestRule

class HomeRobot(rule: ComposeTestRule) : Robot(rule) {
    fun assertShown() = apply {
        assertTitle("Home")
        listOf("olderAdult", "family", "hospital", "neighbour").forEach { waitFor("home.role.$it") }
        assertMissing("nav.back")
    }

    fun chooseOlderAdult() = OlderAdultRobot(rule).also { tap("home.role.olderAdult") }
    fun chooseFamily() = FamilyRobot(rule).also { tap("home.role.family") }
    fun chooseNeighbour() = NeighbourRobot(rule).also { tap("home.role.neighbour") }
    fun chooseHospital() = HospitalRobot(rule).also { tap("home.role.hospital") }
}
