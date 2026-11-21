package uk.checkinsos.app.designsystem

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * A single-choice answer tile (Fine / Good / …): an icon above the label. Selected tiles are filled;
 * semantics report "selected".
 */
@Composable
fun ChoiceTile(
    title: String,
    icon: ImageVector,
    selected: Boolean,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val haptics = LocalHapticFeedback.current
    Surface(
        shape = RoundedCornerShape(Dimens.cornerRadius),
        color = if (selected) colors.primary else colors.surfaceTint,
        contentColor = if (selected) colors.onPrimary else colors.primary,
        border = BorderStroke(2.dp, colors.primary),
        modifier = modifier
            .heightIn(min = 112.dp)
            .selectable(selected = selected, role = Role.RadioButton, onClick = {
                if (!selected) haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                onClick()
            })
            .testTag(testTag),
    ) {
        Box {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = Dimens.spacing),
            ) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(36.dp))
                Text(
                    title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                )
            }
            AnimatedVisibility(
                selected, enter = scaleIn() + fadeIn(), exit = scaleOut() + fadeOut(),
                modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
            ) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(24.dp))
            }
        }
    }
}

/** Lays answer tiles out two to a row (equal heights), or one per row at very large font scales. */
@Composable
fun <T> ChoiceGrid(items: List<T>, tile: @Composable (item: T, modifier: Modifier) -> Unit) {
    val columns = if (LocalDensity.current.fontScale >= 1.5f) 1 else 2
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items.chunked(columns).forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
            ) {
                row.forEach { tile(it, Modifier.weight(1f).fillMaxHeight()) }
                repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}
