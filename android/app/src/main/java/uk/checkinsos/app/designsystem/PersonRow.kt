package uk.checkinsos.app.designsystem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** List row: name + subtitle on a tinted card. Pass [onClick] to make it navigate (shows a chevron). */
@Composable
fun PersonRow(name: String, subtitle: String, testTag: String, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    val colors = AppTheme.colors
    val shape = RoundedCornerShape(Dimens.cornerRadius)
    val rowModifier = modifier.fillMaxWidth().heightIn(min = Dimens.primaryButtonHeight)
    val content: @Composable () -> Unit = {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(Dimens.spacing),
        ) {
            Icon(Icons.Filled.Person, contentDescription = null, tint = colors.primary, modifier = Modifier.size(32.dp))
            Column(Modifier.weight(1f)) {
                Text(name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = colors.text)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = colors.secondaryText)
            }
            if (onClick != null) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = colors.secondaryText)
            }
        }
    }
    if (onClick != null) {
        Surface(onClick = onClick, shape = shape, color = colors.surfaceTint, modifier = rowModifier.testTag(testTag), content = content)
    } else {
        Surface(
            shape = shape, color = colors.surfaceTint,
            modifier = rowModifier.semantics(mergeDescendants = true) {}.testTag(testTag), content = content,
        )
    }
}
