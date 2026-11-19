import Foundation

/// Who an alert is sent to. Raw value = escalation level.
public enum AlertRecipient: Int, Sendable {
    /// Level 1.
    case neighbour = 1
    /// Level 2 (emergency services / hospital).
    case hospital = 2

    public var level: Int { rawValue }
}

public enum AlertStatus: String, Sendable {
    case pending, acknowledged, arrived, escalated, resolved
}

/// A help request ("Your neighbour X needs your help").
public struct Alert: Equatable, Identifiable, Sendable {
    public let id: String
    /// The older adult the alert is about.
    public let olderAdultId: String
    public let subjectName: String
    public let recipient: AlertRecipient
    /// Score that triggered the alert; nil for a missed check-in.
    public let score: Int?
    public let raisedAt: Date
    /// Deadline shown as the countdown.
    public let acknowledgeBy: Date
    public var status: AlertStatus

    public init(
        id: String, olderAdultId: String, subjectName: String, recipient: AlertRecipient,
        score: Int?, raisedAt: Date, acknowledgeBy: Date, status: AlertStatus = .pending
    ) {
        self.id = id
        self.olderAdultId = olderAdultId
        self.subjectName = subjectName
        self.recipient = recipient
        self.score = score
        self.raisedAt = raisedAt
        self.acknowledgeBy = acknowledgeBy
        self.status = status
    }

    /// Seconds left before the acknowledgement deadline (never negative).
    public func secondsRemaining(at now: Date) -> Int {
        max(0, Int(acknowledgeBy.timeIntervalSince(now).rounded(.up)))
    }
}
