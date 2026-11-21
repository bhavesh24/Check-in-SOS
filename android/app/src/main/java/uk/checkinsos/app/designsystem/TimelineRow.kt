package uk.checkinsos.app.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlarmOff
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.HeartBroken
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import uk.checkinsos.domain.model.CareOutcome
import uk.checkinsos.domain.model.TimelineEventKind

/** One timeline event: an icon badge in the event's colour, the copy and the HH:mm time. A line joins the badges. */
@Composable
fun TimelineRow(
    title: String,
    time: String,
    kind: TimelineEventKind,
    testTag: String,
    modifier: Modifier = Modifier,
    isFirst: Boolean = false,
    isLast: Boolean = false,
) {
    val colors = AppTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .heightIn(min = Dimens.minTouchTarget)
            .semantics(mergeDescendants = true) {}
            .testTag(testTag),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.width(36.dp).fillMaxHeight()) {
            Column(Modifier.fillMaxHeight().width(2.dp)) {
                Box(Modifier.weight(1f).fillMaxWidth().background(if (isFirst) Color.Transparent else colors.cardBorder))
                Box(Modifier.weight(1f).fillMaxWidth().background(if (isLast) Color.Transparent else colors.cardBorder))
            }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(36.dp).background(colors.forSeverity(kind.severity), CircleShape),
            ) {
                Icon(kind.icon, contentDescription = null, tint = colors.onRole, modifier = Modifier.size(20.dp))
            }
        }
        Text(
            title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = colors.text,
            modifier = Modifier.weight(1f).padding(vertical = 10.dp),
        )
        Text(time, style = MaterialTheme.typography.bodyMedium, color = colors.secondaryText)
    }
}

private val TimelineEventKind.icon: ImageVector
    get() = when (this) {
        is TimelineEventKind.Score -> Icons.Filled.MonitorHeart
        TimelineEventKind.MissedCheckIn -> Icons.Filled.AlarmOff
        is TimelineEventKind.NotificationSent -> Icons.Filled.NotificationsActive
        is TimelineEventKind.Acknowledged -> Icons.Filled.PanTool
        TimelineEventKind.NeighbourReached -> Icons.AutoMirrored.Filled.DirectionsWalk
        is TimelineEventKind.Escalated -> Icons.Filled.Warning
        is TimelineEventKind.Outcome -> when (outcome) {
            CareOutcome.IN_CARE -> Icons.Filled.LocalHospital
            CareOutcome.RECOVERING -> Icons.Filled.Healing
            CareOutcome.ALL_OK, CareOutcome.RECOVERED, CareOutcome.ALIVE -> Icons.Filled.Verified
            CareOutcome.BAD_CONDITION -> Icons.Filled.Warning
            CareOutcome.DECEASED -> Icons.Filled.HeartBroken
        }
    }
