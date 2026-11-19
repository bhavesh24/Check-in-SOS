package uk.checkinsos.app.robots

import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.ComposeTestRule

class NeighbourRobot(rule: ComposeTestRule) : Robot(rule) {
    fun assertListShown() = apply {
        assertTitle("Neighbours")
        listOf("oa-1", "oa-2", "oa-3").forEach { waitFor("neighbours.row.$it") }
    }

    fun simulateAlert() = apply {
        tap("neighbours.simulateAlert")
        assertTitle("Help")
        assertStep(1, of = 4)
        assertText("alert.message", "Your neighbour Arthur Patel needs your help, please acknowledge the notification")
        assertText("alert.countdown", "20:00")
    }

    fun accept() = apply {
        tap("alert.accept")
        assertStep(2, of = 4)
        assertText(
            "reached.message",
            "You have acknowledged to help your neighbour Arthur Patel, please let us know have you reached",
        )
    }

    fun confirmReached() = apply {
        tap("reached.confirm")
        assertStep(3, of = 4)
        waitFor("escalate.allOk")
    }

    fun allOk() = apply {
        tap("escalate.allOk")
        assertListShown()
    }

    fun alertEmergencyServices() = apply {
        tap("escalate.alertServices")
        waitFor("emergency.call999")
        waitFor("emergency.call111")
    }

    fun cancelDialog() = apply {
        tap("emergency.cancel")
        assertMissing("emergency.call999")
        waitFor("escalate.alertServices")
    }

    /** 999/111 never dial: the Alive screen shows the demo message instead. */
    fun call(number: String) = apply {
        tap("emergency.call$number")
        assertStep(4, of = 4)
        waitFor("emergency.demoMessage").assertTextContains("Demo: would call $number")
    }

    fun answerAlive(yes: Boolean) = apply {
        tap(if (yes) "alive.yes" else "alive.no")
        assertListShown()
    }
}
