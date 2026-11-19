import Foundation
@testable import Domain

let t0 = Date(timeIntervalSince1970: 1_768_467_600)

func adult(_ id: String, _ name: String, score: Int?) -> OlderAdult {
    OlderAdult(id: id, name: name, latestScore: score,
               neighbour: Neighbour(firstName: "N", lastName: id, email: "n@x.io", mobile: "07123456789"))
}

/// One in-memory fake that implements every repository protocol (test-only, excluded from coverage).
actor FakeStore: OlderAdultRepository, PatientRepository, AlertRepository, TimelineRepository {
    var adults: [OlderAdult]
    var patientList: [Patient]
    var alerts: [String: Alert] = [:]
    var events: [TimelineEvent] = []

    init(adults: [OlderAdult] = [], patients: [Patient] = []) {
        self.adults = adults
        self.patientList = patients
    }

    func lovedOnes() async throws -> [OlderAdult] { adults }
    func neighbours() async throws -> [OlderAdult] { adults }
    func olderAdult(id: String) async throws -> OlderAdult {
        guard let a = adults.first(where: { $0.id == id }) else { throw DomainError.notFound(id) }
        return a
    }
    func updateNeighbour(olderAdultId: String, neighbour: Neighbour) async throws -> OlderAdult {
        guard let i = adults.firstIndex(where: { $0.id == olderAdultId }) else { throw DomainError.notFound(olderAdultId) }
        adults[i].neighbour = neighbour
        return adults[i]
    }
    func recordScore(olderAdultId: String, score: Int) async throws {
        guard let i = adults.firstIndex(where: { $0.id == olderAdultId }) else { throw DomainError.notFound(olderAdultId) }
        adults[i].latestScore = score
    }
    func patients() async throws -> [Patient] { patientList }
    func create(olderAdultId: String, subjectName: String, recipient: AlertRecipient, score: Int?,
                raisedAt: Date, acknowledgeBy: Date) async throws -> Alert {
        let a = Alert(id: "a\(alerts.count + 1)", olderAdultId: olderAdultId, subjectName: subjectName,
                      recipient: recipient, score: score, raisedAt: raisedAt, acknowledgeBy: acknowledgeBy)
        alerts[a.id] = a
        return a
    }
    func alert(id: String) async throws -> Alert {
        guard let a = alerts[id] else { throw DomainError.notFound(id) }
        return a
    }
    func updateStatus(alertId: String, status: AlertStatus) async throws -> Alert {
        var a = try await alert(id: alertId)
        a.status = status
        alerts[alertId] = a
        return a
    }
    func events(olderAdultId: String) async throws -> [TimelineEvent] { events.filter { $0.olderAdultId == olderAdultId } }
    func append(olderAdultId: String, kind: TimelineEventKind, date: Date) async throws -> TimelineEvent {
        let e = TimelineEvent(id: "e\(events.count)", olderAdultId: olderAdultId, date: date, kind: kind)
        events.append(e)
        return e
    }
    func kinds() -> [TimelineEventKind] { events.map(\.kind) }
}
