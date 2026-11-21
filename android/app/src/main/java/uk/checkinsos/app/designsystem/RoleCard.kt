package uk.checkinsos.app.designsystem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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

/** A full-width Home card: an icon badge in the role's colour, the title and description, and a chevron. */
@Composable
fun RoleCard(
    title: String,
    description: String,
    icon: ImageVector,
    tint: Color,
    testTag: String,
    onClick: () -> Unit,
) {
    val colors = AppTheme.colors
    val shape = RoundedCornerShape(Dimens.cornerRadius + 4.dp)
    Surface(
        onClick = onClick,
        shape = shape,
        color = colors.background,
        border = BorderStroke(1.dp, colors.cardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 80.dp)
            .testTag(testTag)
            .semantics(mergeDescendants = true) {},
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacing),
            modifier = Modifier.padding(Dimens.spacing),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(52.dp).background(tint, CircleShape),
            ) {
                Icon(icon, contentDescription = null, tint = colors.onRole, modifier = Modifier.size(28.dp))
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, color = colors.text)
                Text(description, style = MaterialTheme.typography.bodyMedium, color = colors.secondaryText)
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null,
                tint = colors.secondaryText,
            )
        }
    }
}
