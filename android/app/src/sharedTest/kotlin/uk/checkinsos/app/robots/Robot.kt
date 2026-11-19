package uk.checkinsos.app.robots

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.text.AnnotatedString

/** Base page object: finds nodes by the shared test IDs from docs/screens.md. */
open class Robot(val rule: ComposeTestRule) {
    private val timeoutMs = 5_000L

    fun node(tag: String): SemanticsNodeInteraction = rule.onNodeWithTag(tag)

    @OptIn(ExperimentalTestApi::class)
    fun waitFor(tag: String): SemanticsNodeInteraction {
        rule.waitUntilExactlyOneExists(hasTestTag(tag), timeoutMs)
        return node(tag)
    }

    /** Scrolls the node into view when it sits in scrolling content, then clicks it. */
    fun tap(tag: String) {
        val target = waitFor(tag)
        runCatching { target.performScrollTo() }
        target.performClick()
        rule.waitForIdle()
    }

    fun assertText(tag: String, vararg texts: String) {
        waitFor(tag).assertTextEquals(*texts)
    }

    fun assertLabel(tag: String, label: String) {
        waitFor(tag).assertContentDescriptionEquals(label)
    }

    fun assertMissing(tag: String) {
        rule.waitForIdle()
        node(tag).assertDoesNotExist()
    }

    fun assertTitle(title: String) = assertText("screen.title", title)

    fun assertStep(step: Int, of: Int) = assertText("stepProgress", "Step $step of $of")

    fun assertEnabled(tag: String, enabled: Boolean) {
        if (enabled) waitFor(tag).assertIsEnabled() else waitFor(tag).assertIsNotEnabled()
    }

    fun assertFieldValue(tag: String, value: String) {
        waitFor(tag).assert(SemanticsMatcher.expectValue(SemanticsProperties.EditableText, AnnotatedString(value)))
    }

    /** Up button in the top app bar (`nav.back`). */
    fun <R : Robot> back(next: (ComposeTestRule) -> R): R {
        tap("nav.back")
        return next(rule)
    }
}
