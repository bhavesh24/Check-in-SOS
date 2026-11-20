import Domain
import Foundation
@testable import CheckInSOS

let t0 = Date(timeIntervalSince1970: 1_768_467_600)

struct TestFailure: Error {}

func adult(_ id: String, _ name: String, score: Int?) -> OlderAdult {
    OlderAdult(id: id, name: name, latestScore: score,
               neighbour: Neighbour(firstName: "Alex", lastName: "Brown", email: "alex@example.com",
                                    mobile: "07700900123"))
}

/// In-memory fake for every repository (test-only). Set `failing = true` to make every call throw.
actor FakeStore: OlderAdultRepository, PatientRepository, AlertRepository, TimelineRepository {
    var adults: [OlderAdult]
    var patientList: [Patient]
    var alerts: [String: Alert] = [:]
    var events: [TimelineEvent] = []
    var failing = false

    init(adults: [OlderAdult] = [adult("oa-1", "Margaret Wilson", score: 76), adult("oa-2", "Arthur Patel", score: 38)],
         patients: [Patient] = [Patient(id: "p-1", name: "Arthur Patel", olderAdultId: "oa-2", latestScore: 38)]) {
        self.adults = adults
        self.patientList = patients
    }

    func setFailing(_ value: Bool) { failing = value }
    func kinds() -> [TimelineEventKind] { events.map(\.kind) }

    private func check() throws { if failing { throw TestFailure() } }

    func lovedOnes() async throws -> [OlderAdult] { try check(); return adults }
    func neighbours() async throws -> [OlderAdult] { try check(); return adults }
    func olderAdult(id: String) async throws -> OlderAdult {
        try check()
        guard let a = adults.first(where: { $0.id == id }) else { throw DomainError.notFound(id) }
        return a
    }
    func updateNeighbour(olderAdultId: String, neighbour: Neighbour) async throws -> OlderAdult {
        try check()
        guard let i = adults.firstIndex(where: { $0.id == olderAdultId }) else { throw DomainError.notFound(olderAdultId) }
        adults[i].neighbour = neighbour
        return adults[i]
    }
    func recordScore(olderAdultId: String, score: Int) async throws {
        try check()
        guard let i = adults.firstIndex(where: { $0.id == olderAdultId }) else { throw DomainError.notFound(olderAdultId) }
        adults[i].latestScore = score
    }
    func patients() async throws -> [Patient] { try check(); return patientList }
    func create(olderAdultId: String, subjectName: String, recipient: AlertRecipient, score: Int?,
                raisedAt: Date, acknowledgeBy: Date) async throws -> Alert {
        try check()
        let a = Alert(id: "a\(alerts.count + 1)", olderAdultId: olderAdultId, subjectName: subjectName,
                      recipient: recipient, score: score, raisedAt: raisedAt, acknowledgeBy: acknowledgeBy)
        alerts[a.id] = a
        return a
    }
    func alert(id: String) async throws -> Alert {
        try check()
        guard let a = alerts[id] else { throw DomainError.notFound(id) }
        return a
    }
    func updateStatus(alertId: String, status: AlertStatus) async throws -> Alert {
        var a = try await alert(id: alertId)
        a.status = status
        alerts[alertId] = a
        return a
    }
    func events(olderAdultId: String) async throws -> [TimelineEvent] {
        try check()
        return events.filter { $0.olderAdultId == olderAdultId }
    }
    func append(olderAdultId: String, kind: TimelineEventKind, date: Date) async throws -> TimelineEvent {
        try check()
        let e = TimelineEvent(id: "e\(events.count)", olderAdultId: olderAdultId, date: date, kind: kind)
        events.append(e)
        return e
    }

    /// Seeds a pending alert about Arthur Patel and returns its summary.
    func seedAlert(recipient: AlertRecipient = .neighbour) async throws -> AlertSummary {
        AlertSummary(try await create(olderAdultId: "oa-2", subjectName: "Arthur Patel", recipient: recipient,
                                      score: 38, raisedAt: t0, acknowledgeBy: t0.addingTimeInterval(20 * 60)))
    }
}

/// Mutable clock for countdown tests.
final class MutableTimeProvider: TimeProvider, @unchecked Sendable {
    var date: Date
    init(_ date: Date) { self.date = date }
    func now() -> Date { date }
}

/// Records navigation callbacks.
@MainActor
final class Recorder<Value> {
    private(set) var values: [Value] = []
    func record(_ value: Value) { values.append(value) }
}
