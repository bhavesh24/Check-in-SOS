package uk.checkinsos.app.designsystem

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import uk.checkinsos.app.feature.shared.ScoreBand
import uk.checkinsos.domain.model.ScoreRecord
import uk.checkinsos.domain.service.ScoreCalculator

/**
 * The last few check-in scores as a line on a 0–100 axis, with a dashed line at the good threshold (70).
 * Each point takes its score band colour.
 */
@Composable
fun ScoreTrendChart(records: List<ScoreRecord>, accessibilityText: String, testTag: String, modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    Box(modifier.fillMaxWidth().height(160.dp).testTag(testTag).clearAndSetSemantics { contentDescription = accessibilityText }) {
        Column(Modifier.fillMaxHeight()) {
            Text("100", style = MaterialTheme.typography.labelSmall, color = colors.secondaryText)
            Box(Modifier.weight(1f))
            Text("0", style = MaterialTheme.typography.labelSmall, color = colors.secondaryText)
        }
        Canvas(Modifier.fillMaxWidth().fillMaxHeight().padding(start = 32.dp, end = 8.dp, top = 8.dp, bottom = 8.dp)) {
            fun y(score: Int) = size.height * (1 - score / 100f)
            fun x(index: Int) = if (records.size <= 1) size.width / 2 else size.width * index / (records.size - 1)
            val threshold = y(ScoreCalculator.GOOD_THRESHOLD)
            drawLine(
                colors.secondaryText.copy(alpha = 0.6f), Offset(0f, threshold), Offset(size.width, threshold),
                strokeWidth = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f)),
            )
            val path = Path()
            records.forEachIndexed { i, record ->
                if (i == 0) path.moveTo(x(i), y(record.score)) else path.lineTo(x(i), y(record.score))
            }
            drawPath(path, colors.primary, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
            records.forEachIndexed { i, record ->
                drawCircle(colors.forScoreBand(ScoreBand.of(record.score)), radius = 5.dp.toPx(), center = Offset(x(i), y(record.score)))
            }
        }
    }
}
