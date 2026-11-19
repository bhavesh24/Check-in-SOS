import Domain

/// The hospital's question steps after the alert (table in docs/screens.md).
enum CareStep: String, CaseIterable, Hashable, Sendable {
    case inCare, recovering, recovered, badCondition, patientAlive

    struct Answer: Hashable, Sendable {
        let title: String
        let testID: String
        /// Outcome to record, or nil to record nothing.
        let outcome: CareOutcome?
        /// Next step, or nil to return to the Patients list.
        let next: CareStep?
    }

    var heading: String {
        switch self {
        case .inCare: "Have you taken them in your care?"
        case .recovering: "Are they recovering?"
        case .recovered: "Have they recovered?"
        case .badCondition: "Are they in bad condition?"
        case .patientAlive: "Is your patient alive?"
        }
    }

    /// Position in the 5-step hospital flow (the alert is step 1).
    var stepNumber: Int {
        switch self {
        case .inCare: 2
        case .recovering: 3
        case .recovered, .badCondition: 4
        case .patientAlive: 5
        }
    }

    var answers: [Answer] {
        switch self {
        case .inCare:
            [answer("Yes", .inCare, next: .recovering)]
        case .recovering:
            [answer("Yes", .recovering, next: .recovered), answer("No", nil, next: .badCondition)]
        case .recovered:
            [answer("Yes", .recovered, next: nil)]
        case .badCondition:
            [answer("Yes", .badCondition, next: .patientAlive), answer("No", nil, next: nil)]
        case .patientAlive:
            [answer("No", .deceased, next: nil), answer("Yes", .alive, next: nil)]
        }
    }

    private func answer(_ title: String, _ outcome: CareOutcome?, next: CareStep?) -> Answer {
        Answer(title: title, testID: "\(rawValue).\(title.lowercased())", outcome: outcome, next: next)
    }
}
