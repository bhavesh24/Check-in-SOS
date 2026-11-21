package uk.checkinsos.app.designsystem

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uk.checkinsos.app.feature.shared.ScoreBand

/** The check-in result: a circular gauge filled to score/100 in the score band's colour, number in the middle. */
@Composable
fun ScoreRing(score: Int, band: ScoreBand, testTag: String, modifier: Modifier = Modifier, ringSize: Dp = 200.dp) {
    val label = scoreAccessibilityLabel(score)
    val color = AppTheme.colors.forScoreBand(band)
    val diameter = ringSize * LocalDensity.current.fontScale.coerceIn(1f, 1.5f)
    val progress = remember { Animatable(0f) }
    LaunchedEffect(score) { progress.animateTo(score.coerceIn(0, 100) / 100f, tween(durationMillis = 1000, delayMillis = 150)) }
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxWidth().testTag(testTag).clearAndSetSemantics { contentDescription = label },
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(diameter)) {
            Canvas(Modifier.fillMaxSize()) {
                val stroke = size.minDimension * 0.09f
                val inset = stroke / 2
                val arcSize = androidx.compose.ui.geometry.Size(size.width - stroke, size.height - stroke)
                val topLeft = androidx.compose.ui.geometry.Offset(inset, inset)
                drawArc(color.copy(alpha = 0.18f), 0f, 360f, false, topLeft, arcSize, style = Stroke(stroke))
                drawArc(color, -90f, 360f * progress.value, false, topLeft, arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("$score", fontSize = (ringSize.value * 0.32f).sp, lineHeight = (ringSize.value * 0.35f).sp, fontWeight = FontWeight.Bold, color = AppTheme.colors.text)
                Text("out of 100", style = MaterialTheme.typography.titleMedium, color = AppTheme.colors.secondaryText)
            }
        }
    }
}
