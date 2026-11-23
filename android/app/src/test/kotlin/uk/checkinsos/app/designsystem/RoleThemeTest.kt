package uk.checkinsos.app.designsystem

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import org.junit.Test
import uk.checkinsos.app.feature.shared.AlertSummary
import uk.checkinsos.app.navigation.Routes
import uk.checkinsos.domain.model.AlertRecipient
import uk.checkinsos.domain.model.Role
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

/** Flow themes (docs/screens.md, "Flow themes"): each screen takes its Home card's role colour. */
class RoleThemeTest {
    private fun alert(recipient: AlertRecipient) = AlertSummary("a-1", "Arthur Patel", recipient, Instant.EPOCH)

    @Test
    fun alertRoutesMapToTheirFlowRole() {
        assertEquals(Role.NEIGHBOUR, Routes.helpAlert(alert(AlertRecipient.NEIGHBOUR)).role)
        assertEquals(Role.HOSPITAL, Routes.helpAlert(alert(AlertRecipient.HOSPITAL)).role)
    }

    @Test
    fun flowColoursUseTheRoleColour() {
        assertSame(LightAppColors, LightAppColors.forFlow(Role.OLDER_ADULT))
        val family = LightAppColors.forFlow(Role.FAMILY)
        assertEquals(Color(0xFF6941C6), family.primary)
        assertEquals(Color(0xFFFFFFFF), family.onPrimary)
        assertEquals(Color(0xFFF4F3FF), family.surfaceTint)
        assertEquals(Color(0xFF84ADFF), DarkAppColors.forFlow(Role.HOSPITAL).primary)
        assertEquals(Color(0xFF2A1A12), DarkAppColors.forFlow(Role.NEIGHBOUR).surfaceTint)
    }

    @Test
    fun everyThemePassesAAContrast() {
        for (base in listOf(LightAppColors, DarkAppColors)) {
            for (role in Role.entries) {
                val c = base.forFlow(role)
                assertTrue(contrast(c.primary, c.background) >= 4.5, "$role primary on background")
                assertTrue(contrast(c.primary, c.surfaceTint) >= 4.5, "$role primary on surfaceTint")
                assertTrue(contrast(c.onPrimary, c.primary) >= 4.5, "$role onPrimary on primary")
            }
        }
    }

    /** WCAG 2.x contrast ratio. */
    private fun contrast(a: Color, b: Color): Double {
        val (l1, l2) = a.luminance().toDouble() to b.luminance().toDouble()
        return (maxOf(l1, l2) + 0.05) / (minOf(l1, l2) + 0.05)
    }
}
