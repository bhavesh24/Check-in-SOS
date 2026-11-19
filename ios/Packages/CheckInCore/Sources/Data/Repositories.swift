import Domain
import Foundation

public struct OlderAdultRepositoryImpl: OlderAdultRepository {
    let source: MockDataSource
    public init(source: MockDataSource) { self.source = source }

    public func lovedOnes() async throws -> [OlderAdult] { await source.allOlderAdults() }
    public func neighbours() async throws -> [OlderAdult] { await source.allOlderAdults() }
    public func olderAdult(id: String) async throws -> OlderAdult { try await source.olderAdult(id: id) }

    public func updateNeighbour(olderAdultId: String, neighbour: Neighbour) async throws -> OlderAdult {
        try await source.mutateOlderAdult(id: olderAdultId) { $0.neighbour = neighbour }
    }

    public func recordScore(olderAdultId: String, score: Int) async throws {
        _ = try await source.mutateOlderAdult(id: olderAdultId) { $0.latestScore = score }
    }
}

public struct PatientRepositoryImpl: PatientRepository {
    let source: MockDataSource
    public init(source: MockDataSource) { self.source = source }
    public func patients() async throws -> [Patient] { try await source.patients() }
}

public struct AlertRepositoryImpl: AlertRepository {
    let source: MockDataSource
    public init(source: MockDataSource) { self.source = source }

    public func create(olderAdultId: String, subjectName: String, recipient: AlertRecipient,
                       score: Int?, raisedAt: Date, acknowledgeBy: Date) async throws -> Alert {
        await source.insertAlert { id in
            Alert(id: id, olderAdultId: olderAdultId, subjectName: subjectName, recipient: recipient,
                  score: score, raisedAt: raisedAt, acknowledgeBy: acknowledgeBy)
        }
    }

    public func alert(id: String) async throws -> Alert { try await source.alert(id: id) }

    public func updateStatus(alertId: String, status: AlertStatus) async throws -> Alert {
        try await source.setAlertStatus(id: alertId, status: status)
    }
}

public struct TimelineRepositoryImpl: TimelineRepository {
    let source: MockDataSource
    public init(source: MockDataSource) { self.source = source }

    public func events(olderAdultId: String) async throws -> [TimelineEvent] {
        await source.events(olderAdultId: olderAdultId)
    }

    @discardableResult
    public func append(olderAdultId: String, kind: TimelineEventKind, date: Date) async throws -> TimelineEvent {
        await source.appendEvent(olderAdultId: olderAdultId, kind: kind, date: date)
    }
}
