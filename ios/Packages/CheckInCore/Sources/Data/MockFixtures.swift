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
                firstName: "Alex", lastName: "Brown", email: "alex.brown@example.com", mobile: "07123456789")),
            OlderAdult(id: "oa-2", name: "Arthur Patel", latestScore: 38, neighbour: Neighbour(
                firstName: "Priya", lastName: "Shah", email: "priya.shah@example.com", mobile: "07234567890")),
            OlderAdult(id: "oa-3", name: "Edith Clarke", latestScore: nil, neighbour: Neighbour(
                firstName: "Tom", middleName: "J", lastName: "Hughes", email: "tom.hughes@example.com", mobile: "07345678901")),
        ]
    }

    /// Patient id → older adult id, in list order.
    static let patients: [(id: String, olderAdultId: String)] = [("p-1", "oa-2"), ("p-2", "oa-3"), ("p-3", "oa-1")]

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
