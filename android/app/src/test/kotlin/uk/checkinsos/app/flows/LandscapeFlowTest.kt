package uk.checkinsos.app.flows

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import uk.checkinsos.app.AppContainer
import uk.checkinsos.app.navigation.CheckInSosRoot
import uk.checkinsos.app.robots.OlderAdultRobot
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

/**
 * Rotating recreates the activity: composition state is saved and restored while ViewModels survive.
 * `StateRestorationTester` emulates that with the test `AppContainer` (fixed clock, fresh mock data).
 * Robolectric's `setQualifiers` would recreate the activity for real, but drops content set by the rule.
 */
@RunWith(AndroidJUnit4::class)
@Config(qualifiers = "w874dp-h402dp-land")
class RotationTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test fun rotatingKeepsState() {
        val container = AppContainer.forTesting()
        val restoration = StateRestorationTester(rule)
        restoration.setContent { CheckInSosRoot(container) }
        HomeRobot(rule).assertShown().chooseOlderAdult().answer("ill", "anxious")

        restoration.emulateSavedInstanceStateRestore()

        OlderAdultRobot(rule).assertAssessmentShown()
        rule.onNodeWithTag("assessment.physical.ill").assertIsSelected()
        rule.onNodeWithTag("assessment.mood.anxious").assertIsSelected()
    }
}
