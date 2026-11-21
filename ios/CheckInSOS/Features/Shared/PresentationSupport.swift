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

    /// "Last check-in Today, 08:00" / "No check-in yet".
    static func lastCheckIn(_ date: Date?, now: Date, timeZone: TimeZone) -> String {
        date.map { "Last check-in \(checkIn($0, now: now, timeZone: timeZone))" } ?? "No check-in yet"
    }

    /// "Today, 08:00", "Yesterday, 08:00", otherwise "13 Jan, 08:00".
    static func checkIn(_ date: Date, now: Date, timeZone: TimeZone) -> String {
        var calendar = Calendar(identifier: .gregorian)
        calendar.timeZone = timeZone
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "en_GB_POSIX")
        formatter.timeZone = timeZone
        formatter.dateFormat = "HH:mm"
        let time = formatter.string(from: date)
        if calendar.isDate(date, inSameDayAs: now) { return "Today, \(time)" }
        if let yesterday = calendar.date(byAdding: .day, value: -1, to: now),
           calendar.isDate(date, inSameDayAs: yesterday) { return "Yesterday, \(time)" }
        formatter.dateFormat = "d MMM, HH:mm"
        return formatter.string(from: date)
    }

    /// "Margaret Wilson" → "MW"; a single name gives one letter.
    static func initials(_ name: String) -> String {
        let parts = name.split(separator: " ")
        return [parts.first, parts.count > 1 ? parts.last : nil]
            .compactMap { $0?.first.map(String.init) }
            .joined()
            .uppercased()
    }

    /// `mm:ss`, or "Time expired" once the deadline has passed.
    static func countdown(seconds: Int) -> String {
        guard seconds > 0 else { return "Time expired" }
        return String(format: "%02d:%02d", seconds / 60, seconds % 60)
    }
}
