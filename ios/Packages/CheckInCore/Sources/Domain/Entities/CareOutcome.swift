/// Answers recorded by neighbours and hospitals after an alert.
public enum CareOutcome: String, CaseIterable, Sendable {
    case allOk, inCare, recovering, recovered, badCondition, alive, deceased

    /// Terminal outcomes close the alert.
    public var resolvesAlert: Bool {
        switch self {
        case .allOk, .recovered, .alive, .deceased: true
        case .inCare, .recovering, .badCondition: false
        }
    }
}
