package uk.checkinsos.app.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import uk.checkinsos.app.feature.shared.DisplayText

/** A circle with someone's initials ("MW"). Decorative: the row it sits in already reads the name. */
@Composable
fun InitialsAvatar(name: String, modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(44.dp).background(colors.primary, CircleShape).clearAndSetSemantics {},
    ) {
        Text(DisplayText.initials(name), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = colors.onPrimary)
    }
}
