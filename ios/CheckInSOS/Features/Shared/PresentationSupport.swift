import Domain
import Foundation

/// The parts of an `Alert` the help flows need, small enough to travel in a navigation `Route`.
struct AlertSummary: Hashable, Sendable {
    let id: String
    let subjectName: String
    let recipient: AlertRecipient
    let acknowledgeBy: Date

    init(id: String, subjectName: String, recipient: AlertRecipient, acknowledgeBy: Date) {
        self.id = id
        self.subjectName = subjectName
        self.recipient = recipient
        self.acknowledgeBy = acknowledgeBy
    }

    init(_ alert: Alert) {
        self.init(id: alert.id, subjectName: alert.subjectName, recipient: alert.recipient,
                  acknowledgeBy: alert.acknowledgeBy)
    }
}

/// Step counts for the multi-step flows (docs/screens.md).
enum FlowSteps {
    static let olderAdult = 2
    static let neighbour = 4
    static let hospital = 5
}

/// Copy shared by several screens (docs/screens.md).
enum DisplayText {
    static let genericError = "Something went wrong. Please try again."

    /// "Score 76" / "No score yet" — list row subtitles.
    static func score(_ score: Int?) -> String {
        score.map { "Score \($0)" } ?? "No score yet"
    }

    /// `mm:ss`, or "Time expired" once the deadline has passed.
    static func countdown(seconds: Int) -> String {
        guard seconds > 0 else { return "Time expired" }
        return String(format: "%02d:%02d", seconds / 60, seconds % 60)
    }
}
