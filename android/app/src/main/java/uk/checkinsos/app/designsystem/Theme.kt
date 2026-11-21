package uk.checkinsos.app.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import uk.checkinsos.app.feature.shared.ScoreBand
import uk.checkinsos.domain.model.Role
import uk.checkinsos.domain.model.TimelineSeverity

/** Design tokens from docs/screens.md. The teal is darkened so white text passes WCAG AA (5.5:1). */
@Immutable
data class AppColors(
    val primary: Color,
    val onPrimary: Color,
    val background: Color,
    val surfaceTint: Color,
    val text: Color,
    val secondaryText: Color,
    val disabledBackground: Color,
    val disabledText: Color,
    val statusCritical: Color,
    val statusWarning: Color,
    val statusOk: Color,
    val roleFamily: Color,
    val roleHospital: Color,
    val roleNeighbour: Color,
    /** Icon colour on a role badge; passes 4.5:1 on every role colour. */
    val onRole: Color,
    val cardBorder: Color,
    val scoreMild: Color,
    val scoreModerate: Color,
    val scoreSevere: Color,
) {
    /** Score ring colour: green when good, then reds that deepen as the score drops. All ≥3:1 on `background`. */
    fun forScoreBand(band: ScoreBand): Color = when (band) {
        ScoreBand.GOOD -> statusOk
        ScoreBand.MILD -> scoreMild
        ScoreBand.MODERATE -> scoreModerate
        ScoreBand.SEVERE -> scoreSevere
    }

    /** Icon badge colour for each role on Home. */
    fun forRole(role: Role): Color = when (role) {
        Role.OLDER_ADULT -> primary
        Role.FAMILY -> roleFamily
        Role.HOSPITAL -> roleHospital
        Role.NEIGHBOUR -> roleNeighbour
    }

    fun forSeverity(severity: TimelineSeverity): Color = when (severity) {
        TimelineSeverity.CRITICAL -> statusCritical
        TimelineSeverity.WARNING -> statusWarning
        TimelineSeverity.OK -> statusOk
    }
}

val LightAppColors = AppColors(
    primary = Color(0xFF0F766E), onPrimary = Color(0xFFFFFFFF), background = Color(0xFFFFFFFF),
    surfaceTint = Color(0xFFF0FDFA), text = Color(0xFF101828), secondaryText = Color(0xFF475467),
    disabledBackground = Color(0xFFE4E7EC), disabledText = Color(0xFF475467),
    statusCritical = Color(0xFFB42318), statusWarning = Color(0xFFB54708), statusOk = Color(0xFF067647),
    roleFamily = Color(0xFF6941C6), roleHospital = Color(0xFF175CD3), roleNeighbour = Color(0xFFB93815),
    onRole = Color(0xFFFFFFFF), cardBorder = Color(0xFFE4E7EC),
    scoreMild = Color(0xFFF04438), scoreModerate = Color(0xFFD92D20), scoreSevere = Color(0xFF912018),
)

val DarkAppColors = AppColors(
    primary = Color(0xFF2DD4BF), onPrimary = Color(0xFF042F2E), background = Color(0xFF0B1214),
    surfaceTint = Color(0xFF132326), text = Color(0xFFF2F4F7), secondaryText = Color(0xFFD0D5DD),
    disabledBackground = Color(0xFF344054), disabledText = Color(0xFFD0D5DD),
    statusCritical = Color(0xFFF97066), statusWarning = Color(0xFFFDB022), statusOk = Color(0xFF47CD89),
    roleFamily = Color(0xFFB692F6), roleHospital = Color(0xFF84ADFF), roleNeighbour = Color(0xFFF38744),
    onRole = Color(0xFF0B1214), cardBorder = Color(0xFF344054),
    scoreMild = Color(0xFFFDA29B), scoreModerate = Color(0xFFF97066), scoreSevere = Color(0xFFF04438),
)

/** Sizes shared by every screen. */
object Dimens {
    /** Minimum touch target. */
    val minTouchTarget = 48.dp
    /** Primary buttons are full width and at least this tall. */
    val primaryButtonHeight = 56.dp
    val cornerRadius = 14.dp
    val spacing = 16.dp
}

val LocalAppColors = staticCompositionLocalOf { LightAppColors }

/** The app's colours: `AppTheme.colors.primary` etc. */
object AppTheme {
    val colors: AppColors
        @Composable get() = LocalAppColors.current
}

@Composable
fun CheckInTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) DarkAppColors else LightAppColors
    val base = if (darkTheme) darkColorScheme() else lightColorScheme()
    val scheme = base.copy(
        primary = colors.primary, onPrimary = colors.onPrimary, background = colors.background,
        onBackground = colors.text, surface = colors.background, onSurface = colors.text,
        onSurfaceVariant = colors.secondaryText, surfaceContainer = colors.surfaceTint,
        surfaceContainerHigh = colors.surfaceTint, error = colors.statusCritical,
    )
    CompositionLocalProvider(LocalAppColors provides colors) {
        MaterialTheme(colorScheme = scheme, content = content)
    }
}
