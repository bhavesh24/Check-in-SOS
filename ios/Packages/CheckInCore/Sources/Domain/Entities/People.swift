/// A 1st-level notified person (the neighbour contact a family member manages).
public struct Neighbour: Equatable, Sendable {
    public var firstName: String
    public var middleName: String
    public var lastName: String
    public var email: String
    public var mobile: String

    public init(firstName: String, middleName: String = "", lastName: String, email: String, mobile: String) {
        self.firstName = firstName
        self.middleName = middleName
        self.lastName = lastName
        self.email = email
        self.mobile = mobile
    }

    /// "First Last" — the middle name is omitted for display.
    public var displayName: String { "\(firstName) \(lastName)" }
}

/// The person who checks in daily.
public struct OlderAdult: Equatable, Identifiable, Sendable {
    public let id: String
    public var name: String
    /// Latest wellbeing score (0–100), or nil if they have never checked in.
    public var latestScore: Int?
    public var neighbour: Neighbour

    public init(id: String, name: String, latestScore: Int?, neighbour: Neighbour) {
        self.id = id
        self.name = name
        self.latestScore = latestScore
        self.neighbour = neighbour
    }
}

/// An older adult as known to a hospital.
public struct Patient: Equatable, Identifiable, Sendable {
    public let id: String
    public var name: String
    public let olderAdultId: String
    public var latestScore: Int?

    public init(id: String, name: String, olderAdultId: String, latestScore: Int?) {
        self.id = id
        self.name = name
        self.olderAdultId = olderAdultId
        self.latestScore = latestScore
    }
}
