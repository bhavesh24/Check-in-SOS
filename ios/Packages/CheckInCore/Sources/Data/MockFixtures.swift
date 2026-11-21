import Domain
import Foundation

/// Seed values shared with Android and docs/screens.md ("Mock data" + "Fixed values for tests").
public enum MockFixtures {
    /// Fixed clock for tests and `-ui-testing`: 2026-01-15T09:00:00Z.
    public static let fixedNow = Date(timeIntervalSince1970: 1_768_467_600)
    /// The older adult who uses the older-adult role.
    public static let currentOlderAdultId = "oa-1"

    static func olderAdults() -> [OlderAdult] {
        [
            OlderAdult(id: "oa-1", name: "Margaret Wilson", latestScore: 76, neighbour: Neighbour(
                firstName: "Alex", lastName: "Brown", email: "alex.brown@example.com", mobile: "07700900123")),
            OlderAdult(id: "oa-2", name: "Arthur Patel", latestScore: 38, neighbour: Neighbour(
                firstName: "Priya", lastName: "Shah", email: "priya.shah@example.com", mobile: "07700900456")),
            OlderAdult(id: "oa-3", name: "Edith Clarke", latestScore: nil, neighbour: Neighbour(
                firstName: "Tom", middleName: "J", lastName: "Hughes", email: "tom.hughes@example.com", mobile: "07700900789")),
        ]
    }

    /// Patient id → older adult id, in list order.
    static let patients: [(id: String, olderAdultId: String)] = [("p-1", "oa-2"), ("p-2", "oa-3"), ("p-3", "oa-1")]

    /// Daily check-ins for the trend chart, oldest first. Each list's last score is the adult's latest score,
    /// taken at the same time as their "Score N" timeline event; earlier ones are one day apart.
    static func scoreHistory() -> [(olderAdultId: String, minutesBefore: Int, score: Int)] {
        let day = 24 * 60
        let margaret = [72, 70, 74, 68, 73, 78, 76]
        let arthur = [74, 71, 66, 60, 52, 45, 38]
        return margaret.enumerated().map { ("oa-1", 60 + (margaret.count - 1 - $0) * day, $1) }
            + arthur.enumerated().map { ("oa-2", 180 + (arthur.count - 1 - $0) * day, $1) }
    }

    static func timeline() -> [(olderAdultId: String, minutesBefore: Int, kind: TimelineEventKind)] {
        [
            ("oa-1", 60, .score(76)),
            ("oa-2", 180, .score(38)),
            ("oa-2", 179, .notificationSent(level: 1)),
            ("oa-2", 170, .acknowledged(level: 1)),
            ("oa-2", 155, .neighbourReached),
            ("oa-2", 150, .escalated(level: 2)),
            ("oa-2", 140, .acknowledged(level: 2)),
            ("oa-2", 90, .careOutcome(.inCare)),
            ("oa-2", 10, .careOutcome(.recovered)),
        ]
    }
}
