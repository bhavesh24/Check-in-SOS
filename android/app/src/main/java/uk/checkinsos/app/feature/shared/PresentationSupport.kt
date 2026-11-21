package uk.checkinsos.app.feature.shared

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import uk.checkinsos.domain.model.Alert
import uk.checkinsos.domain.model.AlertRecipient
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** The parts of an [Alert] the help flows need; travels through navigation routes as primitives. */
data class AlertSummary(
    val id: String,
    val subjectName: String,
    val recipient: AlertRecipient,
    val acknowledgeBy: Instant,
) {
    constructor(alert: Alert) : this(alert.id, alert.subjectName, alert.recipient, alert.acknowledgeBy)
}

/** Step counts for the multi-step flows (docs/screens.md). */
object FlowSteps {
    const val OLDER_ADULT = 2
    const val NEIGHBOUR = 4
    const val HOSPITAL = 5
}

/** Copy shared by several screens (docs/screens.md). */
object DisplayText {
    const val GENERIC_ERROR = "Something went wrong. Please try again."

    /** "Score 76" / "No score yet" — list row subtitles. */
    fun score(score: Int?): String = if (score == null) "No score yet" else "Score $score"

    /** "Last check-in Today, 08:00" / "No check-in yet". */
    fun lastCheckIn(date: Instant?, now: Instant, zone: ZoneId): String =
        if (date == null) "No check-in yet" else "Last check-in ${checkIn(date, now, zone)}"

    /** "Today, 08:00", "Yesterday, 08:00", otherwise "13 Jan, 08:00". */
    fun checkIn(date: Instant, now: Instant, zone: ZoneId): String {
        val day = date.atZone(zone).toLocalDate()
        val today = now.atZone(zone).toLocalDate()
        val time = DateTimeFormatter.ofPattern("HH:mm", Locale.UK).withZone(zone).format(date)
        return when (day) {
            today -> "Today, $time"
            today.minusDays(1) -> "Yesterday, $time"
            else -> DateTimeFormatter.ofPattern("d MMM, HH:mm", Locale.UK).withZone(zone).format(date)
        }
    }

    /** "Margaret Wilson" → "MW"; a single name gives one letter. */
    fun initials(name: String): String {
        val parts = name.split(' ').filter { it.isNotEmpty() }
        return listOfNotNull(parts.firstOrNull(), parts.drop(1).lastOrNull())
            .map { it.first().uppercaseChar() }
            .joinToString("")
    }

    /** `mm:ss`, or "Time expired" once the deadline has passed. */
    fun countdown(seconds: Long): String =
        if (seconds <= 0) "Time expired" else "%02d:%02d".format(seconds / 60, seconds % 60)
}

/** `VERY_SICK` → `verySick`, matching the shared test IDs (e.g. `assessment.physical.verySick`). */
val Enum<*>.camelName: String
    get() = name.lowercase().split('_').mapIndexed { i, part ->
        if (i == 0) part else part.replaceFirstChar(Char::uppercase)
    }.joinToString("")

/**
 * Base for every screen ViewModel: immutable UI state as a [StateFlow], plus one-off navigation
 * [events]. ViewModels never hold navigation callbacks (they outlive the NavController).
 */
abstract class FlowViewModel<S, E>(initial: S) : ViewModel() {
    private val mutableState = MutableStateFlow(initial)
    val state: StateFlow<S> = mutableState.asStateFlow()

    private val eventChannel = Channel<E>(Channel.BUFFERED)
    val events: Flow<E> = eventChannel.receiveAsFlow()

    protected val currentState: S get() = mutableState.value
    protected fun setState(change: (S) -> S) = mutableState.update(change)
    protected fun send(event: E) {
        eventChannel.trySend(event)
    }
}

/** Collects a ViewModel's one-off events while the screen is shown. */
@Composable
fun <E> EventEffect(events: Flow<E>, onEvent: (E) -> Unit) {
    val handler by rememberUpdatedState(onEvent)
    LaunchedEffect(events) { events.collect { handler(it) } }
}
