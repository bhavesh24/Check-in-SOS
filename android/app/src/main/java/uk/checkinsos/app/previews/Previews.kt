package uk.checkinsos.app.previews

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import uk.checkinsos.app.AppContainer
import uk.checkinsos.app.designsystem.CheckInTheme
import uk.checkinsos.app.designsystem.ChoiceTile
import uk.checkinsos.app.designsystem.PrimaryButton
import uk.checkinsos.app.designsystem.RoleTheme
import uk.checkinsos.app.designsystem.SecondaryButton
import uk.checkinsos.app.designsystem.StepProgress
import uk.checkinsos.app.navigation.CheckInSosRoot
import uk.checkinsos.domain.model.Role

// Compose previews live here so they stay out of coverage (`uk.checkinsos.app.previews.*` is excluded).

@Preview(showBackground = true)
@Composable
fun AppPreview() {
    CheckInSosRoot(AppContainer.forTesting())
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AppDarkPreview() {
    CheckInSosRoot(AppContainer.forTesting())
}

@Preview(showBackground = true, heightDp = 1200)
@Composable
fun FlowThemesPreview() {
    CheckInTheme {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
            Role.entries.forEach { role ->
                RoleTheme(role) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        StepProgress(step = 1, total = 2)
                        ChoiceTile("Good", Icons.Filled.Star, selected = true, testTag = "preview", onClick = {})
                        PrimaryButton("Primary", testTag = "preview", onClick = {})
                        SecondaryButton("Secondary", testTag = "preview", onClick = {})
                    }
                }
            }
        }
    }
}
