package uk.checkinsos.app.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import uk.checkinsos.app.feature.shared.DisplayText
import uk.checkinsos.app.feature.shared.ScoreBand

/** A compact score badge: the number on a tint of its score band colour, or "No score". Reads as "Score 76" / "No score yet". */
@Composable
fun ScorePill(score: Int?, modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    val tint = if (score == null) colors.secondaryText else colors.forScoreBand(ScoreBand.of(score))
    val label = DisplayText.score(score)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
            .background(tint.copy(alpha = 0.14f), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp)
            .clearAndSetSemantics { text = AnnotatedString(label) },
    ) {
        Box(Modifier.size(8.dp).background(tint, CircleShape))
        Text(score?.toString() ?: "No score", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = colors.text)
    }
}
