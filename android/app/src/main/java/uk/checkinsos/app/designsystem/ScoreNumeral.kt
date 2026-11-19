package uk.checkinsos.app.designsystem

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** Big score rendered like the prototype: "76/" over "100" ("—" when there is no score yet). */
@Composable
fun ScoreNumeral(score: Int?, testTag: String, modifier: Modifier = Modifier) {
    val label = scoreAccessibilityLabel(score)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth().testTag(testTag).clearAndSetSemantics { contentDescription = label },
    ) {
        Text(
            if (score == null) "—" else "$score/",
            fontSize = 72.sp, lineHeight = 80.sp, fontWeight = FontWeight.Bold, color = AppTheme.colors.primary,
        )
        if (score != null) {
            Text("100", fontSize = 36.sp, fontWeight = FontWeight.SemiBold, color = AppTheme.colors.primary)
        }
    }
}

/** "Score 76 out of 100" / "No score yet". */
fun scoreAccessibilityLabel(score: Int?): String = if (score == null) "No score yet" else "Score $score out of 100"
