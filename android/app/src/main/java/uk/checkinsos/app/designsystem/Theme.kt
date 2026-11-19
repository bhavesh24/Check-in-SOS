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
) {
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
)

val DarkAppColors = AppColors(
    primary = Color(0xFF2DD4BF), onPrimary = Color(0xFF042F2E), background = Color(0xFF0B1214),
    surfaceTint = Color(0xFF132326), text = Color(0xFFF2F4F7), secondaryText = Color(0xFFD0D5DD),
    disabledBackground = Color(0xFF344054), disabledText = Color(0xFFD0D5DD),
    statusCritical = Color(0xFFF97066), statusWarning = Color(0xFFFDB022), statusOk = Color(0xFF47CD89),
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
