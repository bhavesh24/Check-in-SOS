public enum DomainError: Error, Equatable, Sendable {
    case notFound(String)
    case noAlertSubject
    case invalidNeighbour(NeighbourValidationError)
}

public enum NeighbourValidationError: Error, Equatable, Sendable {
    case firstNameRequired, lastNameRequired, invalidEmail, invalidMobile

    /// Copy shown in `editNeighbour.error`.
    public var message: String {
        switch self {
        case .firstNameRequired: "First name is required"
        case .lastNameRequired: "Last name is required"
        case .invalidEmail: "Enter a valid email"
        case .invalidMobile: "Enter a valid UK mobile number"
        }
    }
}
