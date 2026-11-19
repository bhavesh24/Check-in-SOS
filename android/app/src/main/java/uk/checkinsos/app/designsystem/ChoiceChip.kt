package uk.checkinsos.app.designsystem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** A single-choice answer (Fine / Good / …). Selected chips are filled; semantics report "selected". */
@Composable
fun ChoiceChip(title: String, selected: Boolean, testTag: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    Surface(
        shape = RoundedCornerShape(Dimens.cornerRadius),
        color = if (selected) colors.primary else colors.surfaceTint,
        contentColor = if (selected) colors.onPrimary else colors.primary,
        border = BorderStroke(2.dp, colors.primary),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = Dimens.primaryButtonHeight)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .testTag(testTag),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = Dimens.spacing, vertical = 12.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            if (selected) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(24.dp))
            } else {
                Box(Modifier.size(22.dp).border(2.dp, colors.primary, CircleShape))
            }
        }
    }
}
