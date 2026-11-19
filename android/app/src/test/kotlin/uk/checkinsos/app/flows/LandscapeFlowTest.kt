package uk.checkinsos.app.flows

import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import uk.checkinsos.app.MainActivity
import uk.checkinsos.app.robots.HomeRobot

/**
 * Landscape support (docs/screens.md, Global rules → Orientation), JVM only: Robolectric renders at a
 * landscape phone size. Mirrors iOS `LandscapeTests`.
 */
@Config(qualifiers = "w874dp-h402dp-land")
class LandscapeFlowTest : FlowTest() {
    @Test fun olderAdultInLandscape() {
        home.chooseOlderAdult().assertAssessmentShown().answer("ill", "anxious").submit()
            .assertScore(38, neighbourNotified = true)
    }

    @Test fun neighbourInLandscape() {
        home.chooseNeighbour().assertListShown().simulateAlert().accept().confirmReached()
    }
}

/** Rotating recreates the activity; the ViewModel and back stack must keep the answers. */
@RunWith(AndroidJUnit4::class)
@Config(qualifiers = "w874dp-h402dp-land")
class RotationTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Test fun rotatingKeepsState() {
        HomeRobot(rule).assertShown().chooseOlderAdult().answer("ill", "anxious")
        RuntimeEnvironment.setQualifiers("w402dp-h874dp-port")
        rule.activityRule.scenario.recreate()
        rule.onNodeWithTag("assessment.physical.ill").assertIsSelected()
        rule.onNodeWithTag("assessment.mood.anxious").assertIsSelected()
    }
}
