import Foundation

/// One past check-in score, for the family's "last check-in" line and trend chart.
public struct ScoreRecord: Equatable, Sendable {
    public let score: Int
    public let date: Date

    public init(score: Int, date: Date) {
        self.score = score
        self.date = date
    }
}
