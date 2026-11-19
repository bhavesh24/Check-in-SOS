package uk.checkinsos.app.flows

import org.junit.Test
import uk.checkinsos.app.robots.FamilyRobot
import uk.checkinsos.app.robots.HomeRobot
import uk.checkinsos.app.robots.HospitalRobot
import uk.checkinsos.app.robots.NeighbourRobot
import uk.checkinsos.app.robots.OlderAdultRobot

/** Back works from every screen (up button and system back), and the step indicator follows it. */
class NavigationTest : FlowTest() {
    @Test fun backFromOlderAdultScreens() {
        val robot = home.chooseOlderAdult().answer("good", "happy").submit()
        robot.back(::OlderAdultRobot).assertAssessmentShown()
        robot.back(::HomeRobot).assertShown()
    }

    @Test fun backFromFamilyScreens() {
        val robot = home.chooseFamily().openStatus("oa-1", "Margaret Wilson").openTimeline()
        robot.back(::FamilyRobot).assertTitle("Margaret Wilson")
        robot.editNeighbour().back(::FamilyRobot).assertTitle("Margaret Wilson")
        robot.back(::FamilyRobot).assertLovedOnesShown()
        robot.back(::HomeRobot).assertShown()
    }

    @Test fun backThroughNeighbourFlow() {
        val robot = home.chooseNeighbour().simulateAlert().accept().confirmReached().alertEmergencyServices().call("999")
        robot.back(::NeighbourRobot).assertStep(3, of = 4)
        robot.back(::NeighbourRobot).assertStep(2, of = 4)
        robot.back(::NeighbourRobot).assertStep(1, of = 4)
        robot.back(::NeighbourRobot).assertListShown()
        robot.back(::HomeRobot).assertShown()
    }

    @Test fun backThroughHospitalFlow() {
        val robot = home.chooseHospital().simulateAlert().accept()
            .answer("inCare.yes").answer("recovering.no").answer("badCondition.yes")
        robot.back(::HospitalRobot).assertQuestion("Are they in bad condition?", step = 4)
        robot.back(::HospitalRobot).assertQuestion("Are they recovering?", step = 3)
        robot.back(::HospitalRobot).assertQuestion("Have you taken them in your care?", step = 2)
        robot.back(::HospitalRobot).assertStep(1, of = 5)
        robot.back(::HospitalRobot).assertListShown()
        robot.back(::HomeRobot).assertShown()
    }

    @Test fun systemBackPopsScreens() {
        val robot = home.chooseFamily().openStatus("oa-2", "Arthur Patel").openTimeline()
        systemBack()
        robot.assertTitle("Arthur Patel")
        systemBack()
        robot.assertLovedOnesShown()
        systemBack()
        HomeRobot(rule).assertShown()
    }
}
