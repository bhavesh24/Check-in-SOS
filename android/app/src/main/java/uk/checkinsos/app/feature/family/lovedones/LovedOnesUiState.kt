package uk.checkinsos.app.feature.family.lovedones

import uk.checkinsos.app.feature.shared.DisplayText
import uk.checkinsos.domain.model.OlderAdult
import java.time.Instant
import java.time.ZoneOffset

data class LovedOnesUiState(
    val lovedOnes: List<OlderAdult> = emptyList(),
    val errorMessage: String? = null,
    /** "Last check-in …" text by older adult id, worked out against the clock when the list loaded. */
    val checkInTexts: Map<String, String> = emptyMap(),
) {
    /** "Last check-in Today, 08:00" / "No check-in yet". */
    fun subtitle(adult: OlderAdult): String = checkInTexts[adult.id] ?: NO_CHECK_IN

    private companion object {
        val NO_CHECK_IN = DisplayText.lastCheckIn(null, Instant.EPOCH, ZoneOffset.UTC)
    }
}
