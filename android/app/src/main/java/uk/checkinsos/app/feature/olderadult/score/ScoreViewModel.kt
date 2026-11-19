package uk.checkinsos.app.feature.olderadult.score

import uk.checkinsos.app.feature.shared.FlowViewModel

class ScoreViewModel(score: Int, neighbourNotified: Boolean) :
    FlowViewModel<ScoreUiState, ScoreViewModel.Event>(ScoreUiState(score, neighbourNotified)) {

    data object Event

    fun done() = send(Event)
}
