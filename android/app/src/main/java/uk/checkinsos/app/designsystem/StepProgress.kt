package uk.checkinsos.app.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** "Step N of M" with a segmented bar (test tag `stepProgress`). */
@Composable
fun StepProgress(step: Int, total: Int, modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxWidth().semantics(mergeDescendants = true) {}.testTag("stepProgress"),
    ) {
        Text(stepText(step, total), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = colors.secondaryText)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            for (index in 1..maxOf(total, 1)) {
                Box(
                    Modifier
                        .weight(1f)
                        .height(6.dp)
                        .background(if (index <= step) colors.primary else colors.disabledBackground, RoundedCornerShape(3.dp)),
                )
            }
        }
    }
}

fun stepText(step: Int, total: Int): String = "Step $step of $total"
