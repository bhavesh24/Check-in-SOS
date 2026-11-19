package uk.checkinsos.app.flows

import android.os.Build
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.accessibility.enableAccessibilityChecks
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import uk.checkinsos.app.AppContainer
import uk.checkinsos.app.navigation.CheckInSosRoot
import uk.checkinsos.app.robots.HomeRobot

/**
 * End-to-end flows shared by the JVM (Robolectric) and the emulator. Each test gets the test
 * AppContainer: fresh mock data, the fixed clock and UTC times. On a device, every action also runs
 * the Accessibility Test Framework checks (contrast, touch-target size, labels).
 */
@RunWith(AndroidJUnit4::class)
abstract class FlowTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun launch() {
        if (onDevice) rule.enableAccessibilityChecks()
        rule.setContent { CheckInSosRoot(AppContainer.forTesting()) }
    }

    protected val home: HomeRobot get() = HomeRobot(rule).assertShown()

    /** System back (gesture / button), as opposed to the top-app-bar up button. */
    protected fun systemBack() {
        rule.runOnUiThread { rule.activity.onBackPressedDispatcher.onBackPressed() }
        rule.waitForIdle()
    }

    companion object {
        val onDevice: Boolean = Build.FINGERPRINT != "robolectric"
    }
}
