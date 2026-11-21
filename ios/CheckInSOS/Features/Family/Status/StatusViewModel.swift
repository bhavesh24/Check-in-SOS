import Domain
import Foundation
import Observation

/// Current status of one loved one: latest score, last check-in, recent scores and the 1st-level neighbour.
/// Reloads on every appearance so an edited neighbour shows straight away.
@MainActor @Observable
final class StatusViewModel {
    private(set) var adult: OlderAdult?
    private(set) var history: [ScoreRecord] = []
    private(set) var errorMessage: String?

    let scoreCaption = "Current score"
    let trendTitle = "Recent check-ins"
    let neighbourCaption = "1st level neighbour"

    var title: String { adult?.name ?? "" }
    var neighbourText: String { adult.map { "1st level: \($0.neighbour.displayName)" } ?? "" }
    var neighbourName: String { adult?.neighbour.displayName ?? "" }
    var band: ScoreBand? { adult?.latestScore.map(ScoreBand.init(score:)) }
    var lastCheckInText: String {
        DisplayText.lastCheckIn(adult?.lastCheckIn, now: time.now(), timeZone: timeZone)
    }

    /// The chart needs at least two points to show a trend.
    var showsTrend: Bool { history.count >= 2 }
    var trendAccessibilityText: String {
        "\(trendTitle): " + history.map { String($0.score) }.joined(separator: ", ")
    }

    private let olderAdultId: String
    private let getStatus: GetLovedOneStatus
    private let getScoreHistory: GetScoreHistory
    private let time: any TimeProvider
    private let timeZone: TimeZone
    private let onEditNeighbour: (String) -> Void
    private let onTimeline: (String) -> Void

    init(olderAdultId: String, getStatus: GetLovedOneStatus, getScoreHistory: GetScoreHistory,
         time: any TimeProvider, timeZone: TimeZone,
         onEditNeighbour: @escaping (String) -> Void, onTimeline: @escaping (String) -> Void) {
        self.olderAdultId = olderAdultId
        self.getStatus = getStatus
        self.getScoreHistory = getScoreHistory
        self.time = time
        self.timeZone = timeZone
        self.onEditNeighbour = onEditNeighbour
        self.onTimeline = onTimeline
    }

    func load() async {
        do {
            adult = try await getStatus(olderAdultId: olderAdultId)
            history = try await getScoreHistory(olderAdultId: olderAdultId)
            errorMessage = nil
        } catch {
            errorMessage = DisplayText.genericError
        }
    }

    func editNeighbour() { onEditNeighbour(olderAdultId) }
    func openTimeline() { onTimeline(olderAdultId) }
}
