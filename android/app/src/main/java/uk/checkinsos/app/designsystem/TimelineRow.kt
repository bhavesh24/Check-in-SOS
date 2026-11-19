package uk.checkinsos.app.designsystem

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import uk.checkinsos.domain.model.TimelineSeverity

/** One timeline event: coloured ring, copy and HH:mm time. */
@Composable
fun TimelineRow(title: String, time: String, severity: TimelineSeverity, testTag: String, modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = Dimens.minTouchTarget)
            .padding(vertical = 12.dp)
            .semantics(mergeDescendants = true) {}
            .testTag(testTag),
    ) {
        Box(Modifier.size(22.dp).border(4.dp, colors.forSeverity(severity), CircleShape))
        Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = colors.text, modifier = Modifier.weight(1f))
        Text(time, style = MaterialTheme.typography.bodyMedium, color = colors.secondaryText)
    }
}
