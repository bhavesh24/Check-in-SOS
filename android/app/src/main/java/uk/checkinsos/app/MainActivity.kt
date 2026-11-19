package uk.checkinsos.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import uk.checkinsos.app.navigation.CheckInSosRoot

/** App entry point (excluded from coverage). */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { CheckInSosRoot(AppContainer.live) }
    }
}
