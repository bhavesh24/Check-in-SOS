import Domain
import Observation

/// Result of a check-in. Below 70 the neighbour has been notified.
@MainActor @Observable
final class ScoreViewModel {
    let score: Int
    let neighbourNotified: Bool
    let step = 2
    let totalSteps = FlowSteps.olderAdult
    let notificationText = "A notification has been sent to your neighbour"
    let reassuranceText = "You're doing well today."

    /// Ring colour: green when good, then deepening reds for 50–69, 30–49 and 0–29.
    var band: ScoreBand { ScoreBand(score: score) }

    /// Shown when the score is good, in place of the neighbour notification.
    var showsReassurance: Bool { ScoreCalculator.isGood(score) }

    private let onDone: () -> Void

    init(score: Int, neighbourNotified: Bool, onDone: @escaping () -> Void) {
        self.score = score
        self.neighbourNotified = neighbourNotified
        self.onDone = onDone
    }

    func done() { onDone() }
}
