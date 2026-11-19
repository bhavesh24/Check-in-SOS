import Foundation

public enum TimelineSeverity: String, Sendable {
    case critical, warning, ok
}

public enum TimelineEventKind: Equatable, Sendable {
    case score(Int)
    case missedCheckIn
    case notificationSent(level: Int)
    case acknowledged(level: Int)
    case neighbourReached
    case escalated(level: Int)
    case careOutcome(CareOutcome)

    /// Copy shown on the Timeline screen (see docs/screens.md).
    public var title: String {
        switch self {
        case .score(let value): "Score \(value)"
        case .missedCheckIn: "Missed check-in"
        case .notificationSent(let level): "Notification sent to \(Self.ordinal(level)) level"
        case .acknowledged(let level): "\(Self.ordinal(level)) level acknowledged"
        case .neighbourReached: "Neighbour reached"
        case .escalated(let level): "Escalation to \(Self.ordinal(level)) level"
        case .careOutcome(let outcome):
            switch outcome {
            case .allOk: "Neighbour reports all OK"
            case .inCare: "In care in hospital"
            case .recovering: "Recovering"
            case .recovered: "Recovered"
            case .badCondition: "In bad condition"
            case .alive: "Confirmed alive"
            case .deceased: "Reported deceased"
            }
        }
    }

    public var severity: TimelineSeverity {
        switch self {
        case .score(let value): ScoreCalculator.isGood(value) ? .ok : .critical
        case .missedCheckIn, .notificationSent, .escalated: .critical
        case .acknowledged: .warning
        case .neighbourReached: .ok
        case .careOutcome(let outcome):
            switch outcome {
            case .inCare: .warning
            case .badCondition, .deceased: .critical
            case .allOk, .recovering, .recovered, .alive: .ok
            }
        }
    }

    static func ordinal(_ level: Int) -> String {
        switch level {
        case 1: "1st"
        case 2: "2nd"
        case 3: "3rd"
        default: "\(level)th"
        }
    }
}

public struct TimelineEvent: Equatable, Identifiable, Sendable {
    public let id: String
    public let olderAdultId: String
    public let date: Date
    public let kind: TimelineEventKind

    public init(id: String, olderAdultId: String, date: Date, kind: TimelineEventKind) {
        self.id = id
        self.olderAdultId = olderAdultId
        self.date = date
        self.kind = kind
    }
}
