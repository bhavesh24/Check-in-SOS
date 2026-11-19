package uk.checkinsos.app.previews

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import uk.checkinsos.app.AppContainer
import uk.checkinsos.app.navigation.CheckInSosRoot

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
