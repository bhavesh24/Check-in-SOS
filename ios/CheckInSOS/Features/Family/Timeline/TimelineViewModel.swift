import Domain
import Foundation
import Observation

@MainActor @Observable
final class TimelineViewModel {
    struct Row: Identifiable, Equatable {
        let id: String
        let index: Int
        let title: String
        let time: String
        let severity: TimelineSeverity
        let kind: TimelineEventKind
        var testID: String { "timeline.event.\(index)" }
    }

    private(set) var rows: [Row] = []
    private(set) var isLoaded = false
    private(set) var errorMessage: String?

    let emptyText = "No events yet"
    var isEmpty: Bool { isLoaded && rows.isEmpty }

    private let olderAdultId: String
    private let getTimeline: GetTimeline
    private let formatter: DateFormatter

    init(olderAdultId: String, getTimeline: GetTimeline, timeZone: TimeZone) {
        self.olderAdultId = olderAdultId
        self.getTimeline = getTimeline
        formatter = DateFormatter()
        formatter.locale = Locale(identifier: "en_GB_POSIX")
        formatter.timeZone = timeZone
        formatter.dateFormat = "HH:mm"
    }

    func load() async {
        do {
            let events = try await getTimeline(olderAdultId: olderAdultId)
            rows = events.enumerated().map { index, event in
                Row(id: event.id, index: index, title: event.kind.title,
                    time: formatter.string(from: event.date), severity: event.kind.severity, kind: event.kind)
            }
            isLoaded = true
            errorMessage = nil
        } catch {
            errorMessage = DisplayText.genericError
        }
    }
}
