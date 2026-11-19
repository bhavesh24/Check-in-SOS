public struct GetLovedOnes: Sendable {
    let olderAdults: any OlderAdultRepository
    public init(olderAdults: any OlderAdultRepository) { self.olderAdults = olderAdults }
    public func callAsFunction() async throws -> [OlderAdult] { try await olderAdults.lovedOnes() }
}

/// Current status = latest score + 1st-level neighbour.
public struct GetLovedOneStatus: Sendable {
    let olderAdults: any OlderAdultRepository
    public init(olderAdults: any OlderAdultRepository) { self.olderAdults = olderAdults }
    public func callAsFunction(olderAdultId: String) async throws -> OlderAdult {
        try await olderAdults.olderAdult(id: olderAdultId)
    }
}

/// Validates then saves the 1st-level neighbour. Throws `DomainError.invalidNeighbour`.
public struct UpdateNeighbour: Sendable {
    let olderAdults: any OlderAdultRepository
    public init(olderAdults: any OlderAdultRepository) { self.olderAdults = olderAdults }
    public func callAsFunction(olderAdultId: String, neighbour: Neighbour) async throws -> OlderAdult {
        let valid: Neighbour
        do { valid = try NeighbourValidator.validate(neighbour) } catch { throw DomainError.invalidNeighbour(error) }
        return try await olderAdults.updateNeighbour(olderAdultId: olderAdultId, neighbour: valid)
    }
}

/// Timeline events, oldest first.
public struct GetTimeline: Sendable {
    let timeline: any TimelineRepository
    public init(timeline: any TimelineRepository) { self.timeline = timeline }
    public func callAsFunction(olderAdultId: String) async throws -> [TimelineEvent] {
        try await timeline.events(olderAdultId: olderAdultId).sorted { $0.date < $1.date }
    }
}
