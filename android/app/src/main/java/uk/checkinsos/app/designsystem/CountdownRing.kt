package uk.checkinsos.app.designsystem

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/** A ring that empties as the acknowledgement deadline approaches, with the countdown text in the middle. */
@Composable
fun CountdownRing(text: String, progress: Float, caption: String, testTag: String, modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    val animated by animateFloatAsState(progress, label = "countdown")
    val diameter = 200.dp * LocalDensity.current.fontScale.coerceIn(1f, 1.5f)
    Box(contentAlignment = Alignment.Center, modifier = modifier.fillMaxWidth()) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(diameter)) {
            Canvas(Modifier.fillMaxSize()) {
                val stroke = size.minDimension * 0.07f
                val arcSize = Size(size.width - stroke, size.height - stroke)
                val topLeft = Offset(stroke / 2, stroke / 2)
                drawArc(colors.statusCritical.copy(alpha = 0.16f), 0f, 360f, false, topLeft, arcSize, style = Stroke(stroke))
                drawArc(colors.statusCritical, -90f, 360f * animated, false, topLeft, arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                Text(
                    text, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold,
                    color = colors.statusCritical, textAlign = TextAlign.Center, modifier = Modifier.testTag(testTag),
                )
                Text(caption, style = MaterialTheme.typography.bodyMedium, color = colors.secondaryText, modifier = Modifier.clearAndSetSemantics {})
            }
        }
    }
}
