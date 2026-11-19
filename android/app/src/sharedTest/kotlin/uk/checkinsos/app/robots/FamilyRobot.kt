package uk.checkinsos.app.robots

import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.performTextReplacement

class FamilyRobot(rule: ComposeTestRule) : Robot(rule) {
    fun assertLovedOnesShown() = apply {
        assertTitle("Your loved ones")
        waitFor("lovedOnes.row.oa-1").assertTextContains("Margaret Wilson").assertTextContains("Score 76")
        waitFor("lovedOnes.row.oa-2").assertTextContains("Score 38")
        waitFor("lovedOnes.row.oa-3").assertTextContains("No score yet")
    }

    fun openStatus(id: String, name: String) = apply {
        tap("lovedOnes.row.$id")
        assertTitle(name)
    }

    fun assertStatus(scoreLabel: String, neighbour: String) = apply {
        assertLabel("status.score", scoreLabel)
        assertText("status.neighbour", "1st level: $neighbour")
    }

    fun editNeighbour() = apply {
        tap("status.editNeighbour")
        assertTitle("Edit Neighbour")
    }

    fun assertField(field: String, value: String) = apply { assertFieldValue("editNeighbour.$field", value) }

    fun replace(field: String, text: String) = apply {
        waitFor("editNeighbour.$field").performTextReplacement(text)
    }

    fun save() = apply { tap("editNeighbour.save") }

    fun assertError(message: String) = apply {
        assertText("editNeighbour.error", message)
        assertTitle("Edit Neighbour")
    }

    fun openTimeline() = apply {
        tap("status.timeline")
        assertTitle("Timeline")
    }

    /** Each event as (copy, HH:mm), oldest first. */
    fun assertTimeline(events: List<Pair<String, String>>) = apply {
        events.forEachIndexed { index, (title, time) -> assertText("timeline.event.$index", title, time) }
        assertMissing("timeline.event.${events.size}")
    }
}
