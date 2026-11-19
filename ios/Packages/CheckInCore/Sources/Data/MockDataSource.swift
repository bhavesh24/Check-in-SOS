import Domain
import Foundation

/// In-memory store seeded with `MockFixtures`. Swap for an API/Firebase data source later
/// without touching Domain or Presentation.
public actor MockDataSource {
    private var olderAdults: [OlderAdult]
    private var alerts: [String: Alert] = [:]
    private var events: [TimelineEvent] = []
    private var nextAlertId = 1
    private var nextEventId = 1

    /// - Parameter now: reference time the seeded timeline is relative to.
    public init(now: Date = MockFixtures.fixedNow) {
        olderAdults = MockFixtures.olderAdults()
        let seeds = MockFixtures.timeline()
        events = seeds.enumerated().map { index, seed in
            TimelineEvent(id: "event-\(index + 1)", olderAdultId: seed.olderAdultId,
                          date: now.addingTimeInterval(-Double(seed.minutesBefore) * 60), kind: seed.kind)
        }
        nextEventId = seeds.count + 1
    }

    // MARK: Older adults

    func allOlderAdults() -> [OlderAdult] { olderAdults }

    func olderAdult(id: String) throws -> OlderAdult {
        guard let adult = olderAdults.first(where: { $0.id == id }) else { throw DomainError.notFound(id) }
        return adult
    }

    func mutateOlderAdult(id: String, _ change: (inout OlderAdult) -> Void) throws -> OlderAdult {
        guard let index = olderAdults.firstIndex(where: { $0.id == id }) else { throw DomainError.notFound(id) }
        change(&olderAdults[index])
        return olderAdults[index]
    }

    // MARK: Patients

    func patients() throws -> [Patient] {
        try MockFixtures.patients.map { seed in
            let adult = try olderAdult(id: seed.olderAdultId)
            return Patient(id: seed.id, name: adult.name, olderAdultId: adult.id, latestScore: adult.latestScore)
        }
    }

    // MARK: Alerts

    func insertAlert(_ build: (String) -> Alert) -> Alert {
        let alert = build("alert-\(nextAlertId)")
        nextAlertId += 1
        alerts[alert.id] = alert
        return alert
    }

    func alert(id: String) throws -> Alert {
        guard let alert = alerts[id] else { throw DomainError.notFound(id) }
        return alert
    }

    func setAlertStatus(id: String, status: AlertStatus) throws -> Alert {
        var alert = try alert(id: id)
        alert.status = status
        alerts[id] = alert
        return alert
    }

    // MARK: Timeline

    func events(olderAdultId: String) -> [TimelineEvent] { events.filter { $0.olderAdultId == olderAdultId } }

    func appendEvent(olderAdultId: String, kind: TimelineEventKind, date: Date) -> TimelineEvent {
        let event = TimelineEvent(id: "event-\(nextEventId)", olderAdultId: olderAdultId, date: date, kind: kind)
        nextEventId += 1
        events.append(event)
        return event
    }
}
