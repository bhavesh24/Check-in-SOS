package uk.checkinsos.app.designsystem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Standard screen: top app bar (title `screen.title`, up button `nav.back` unless [onBack] is null),
 * optional step progress, scrolling content and actions pinned at the bottom.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlowScreen(
    title: String,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    step: Int? = null,
    totalSteps: Int? = null,
    errorMessage: String? = null,
    actions: (@Composable ColumnScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = AppTheme.colors
    Scaffold(
        containerColor = colors.background,
        modifier = modifier.imePadding(),
        topBar = {
            TopAppBar(
                title = {
                    Text(title, fontWeight = FontWeight.Bold, modifier = Modifier.semantics { heading() }.testTag("screen.title"))
                },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack, modifier = Modifier.testTag("nav.back")) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = colors.primary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.background, titleContentColor = colors.text),
            )
        },
        bottomBar = {
            if (actions != null) {
                Surface(color = colors.background) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(Dimens.spacing),
                        content = actions,
                    )
                }
            }
        },
    ) { padding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(Dimens.spacing),
        ) {
            if (step != null && totalSteps != null) StepProgress(step, totalSteps)
            content()
            if (errorMessage != null) ErrorBanner(errorMessage)
        }
    }
}

/** Large body copy used for the help-flow messages and questions. */
@Composable
fun MessageText(text: String, modifier: Modifier = Modifier, testTag: String? = null, isHeading: Boolean = false) {
    Text(
        text,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.SemiBold,
        color = AppTheme.colors.text,
        modifier = modifier
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
            .then(if (isHeading) Modifier.semantics { heading() } else Modifier),
    )
}

/** Inline error message (test tag `error.message` unless a screen gives its own). */
@Composable
fun ErrorBanner(message: String, testTag: String = "error.message") {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.semantics(mergeDescendants = true) {}.testTag(testTag),
    ) {
        Icon(Icons.Filled.Warning, contentDescription = null, tint = AppTheme.colors.statusCritical)
        Text(message, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = AppTheme.colors.statusCritical)
    }
}
