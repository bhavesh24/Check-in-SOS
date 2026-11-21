package uk.checkinsos.app.designsystem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/** Full-width filled teal button, at least 56 dp high. */
@Composable
fun PrimaryButton(text: String, testTag: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    val colors = AppTheme.colors
    val haptics = LocalHapticFeedback.current
    Button(
        onClick = {
            haptics.performHapticFeedback(HapticFeedbackType.ContextClick)
            onClick()
        },
        enabled = enabled,
        shape = RoundedCornerShape(Dimens.cornerRadius),
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.primary, contentColor = colors.onPrimary,
            disabledContainerColor = colors.disabledBackground, disabledContentColor = colors.disabledText,
        ),
        contentPadding = PaddingValues(horizontal = Dimens.spacing, vertical = 12.dp),
        modifier = modifier.fillMaxWidth().heightIn(min = Dimens.primaryButtonHeight).testTag(testTag),
    ) {
        ButtonLabel(text)
    }
}

/** Full-width outlined button for the secondary choice. */
@Composable
fun SecondaryButton(text: String, testTag: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    val colors = AppTheme.colors
    val haptics = LocalHapticFeedback.current
    OutlinedButton(
        onClick = {
            haptics.performHapticFeedback(HapticFeedbackType.ContextClick)
            onClick()
        },
        enabled = enabled,
        shape = RoundedCornerShape(Dimens.cornerRadius),
        border = BorderStroke(2.dp, colors.primary),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = colors.surfaceTint, contentColor = colors.primary,
            disabledContainerColor = colors.disabledBackground, disabledContentColor = colors.disabledText,
        ),
        contentPadding = PaddingValues(horizontal = Dimens.spacing, vertical = 12.dp),
        modifier = modifier.fillMaxWidth().heightIn(min = Dimens.primaryButtonHeight).testTag(testTag),
    ) {
        ButtonLabel(text)
    }
}

@Composable
private fun ButtonLabel(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
}
