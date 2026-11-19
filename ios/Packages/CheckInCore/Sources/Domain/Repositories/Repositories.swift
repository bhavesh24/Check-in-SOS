import Foundation

/// Older adults visible to the current user (family "loved ones", neighbour "neighbours").
public protocol OlderAdultRepository: Sendable {
    func lovedOnes() async throws -> [OlderAdult]
    func neighbours() async throws -> [OlderAdult]
    func olderAdult(id: String) async throws -> OlderAdult
    func updateNeighbour(olderAdultId: String, neighbour: Neighbour) async throws -> OlderAdult
    func recordScore(olderAdultId: String, score: Int) async throws
}

public protocol PatientRepository: Sendable {
    func patients() async throws -> [Patient]
}

public protocol AlertRepository: Sendable {
    func create(
        olderAdultId: String, subjectName: String, recipient: AlertRecipient,
        score: Int?, raisedAt: Date, acknowledgeBy: Date
    ) async throws -> Alert
    func alert(id: String) async throws -> Alert
    func updateStatus(alertId: String, status: AlertStatus) async throws -> Alert
}

public protocol TimelineRepository: Sendable {
    func events(olderAdultId: String) async throws -> [TimelineEvent]
    @discardableResult
    func append(olderAdultId: String, kind: TimelineEventKind, date: Date) async throws -> TimelineEvent
}
