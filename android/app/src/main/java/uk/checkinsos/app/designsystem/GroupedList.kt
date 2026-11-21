package uk.checkinsos.app.designsystem

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Rows on one rounded panel with dividers between them, like an iOS inset grouped list. */
@Composable
fun <T> GroupedList(items: List<T>, row: @Composable (T) -> Unit) {
    val colors = AppTheme.colors
    Surface(shape = RoundedCornerShape(Dimens.cornerRadius + 4.dp), color = colors.surfaceTint, modifier = Modifier.fillMaxWidth()) {
        Column {
            items.forEachIndexed { index, item ->
                row(item)
                if (index < items.lastIndex) HorizontalDivider(Modifier.padding(start = 72.dp), color = colors.cardBorder)
            }
        }
    }
}
