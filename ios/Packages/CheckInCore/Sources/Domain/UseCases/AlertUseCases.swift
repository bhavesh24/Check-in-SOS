public struct GetNeighbours: Sendable {
    let olderAdults: any OlderAdultRepository
    public init(olderAdults: any OlderAdultRepository) { self.olderAdults = olderAdults }
    public func callAsFunction() async throws -> [OlderAdult] { try await olderAdults.neighbours() }
}

public struct GetPatients: Sendable {
    let patients: any PatientRepository
    public init(patients: any PatientRepository) { self.patients = patients }
    public func callAsFunction() async throws -> [Patient] { try await patients.patients() }
}

/// Demo stand-in for a push notification: raises an alert about the lowest-scoring person in the
/// recipient's list (people without a score are only picked if nobody has one).
public struct SimulateAlert: Sendable {
    let olderAdults: any OlderAdultRepository
    let patients: any PatientRepository
    let alerts: any AlertRepository
    let timeline: any TimelineRepository
    let time: any TimeProvider

    public init(olderAdults: any OlderAdultRepository, patients: any PatientRepository, alerts: any AlertRepository,
                timeline: any TimelineRepository, time: any TimeProvider) {
        self.olderAdults = olderAdults
        self.patients = patients
        self.alerts = alerts
        self.timeline = timeline
        self.time = time
    }

    public func callAsFunction(recipient: AlertRecipient) async throws -> Alert {
        let candidates: [(olderAdultId: String, name: String, score: Int?)] = switch recipient {
        case .neighbour: try await olderAdults.neighbours().map { ($0.id, $0.name, $0.latestScore) }
        case .hospital: try await patients.patients().map { ($0.olderAdultId, $0.name, $0.latestScore) }
        }
        let scored = candidates.filter { $0.score != nil }
        guard let subject = scored.min(by: { $0.score! < $1.score! }) ?? candidates.first else {
            throw DomainError.noAlertSubject
        }
        let now = time.now()
        let timeout = EscalationPolicy.acknowledgementTimeout(forScore: subject.score)
            ?? EscalationPolicy.missedCheckInTimeout
        let alert = try await alerts.create(
            olderAdultId: subject.olderAdultId, subjectName: subject.name, recipient: recipient,
            score: subject.score, raisedAt: now, acknowledgeBy: now.addingTimeInterval(timeout)
        )
        try await timeline.append(olderAdultId: subject.olderAdultId, kind: .notificationSent(level: recipient.level), date: now)
        return alert
    }
}

/// Shared by every alert-status use case: change status, then log a timeline event.
struct AlertTransition: Sendable {
    let alerts: any AlertRepository
    let timeline: any TimelineRepository
    let time: any TimeProvider

    func apply(alertId: String, status: AlertStatus, event: (Alert) -> TimelineEventKind) async throws -> Alert {
        let updated = try await alerts.updateStatus(alertId: alertId, status: status)
        try await timeline.append(olderAdultId: updated.olderAdultId, kind: event(updated), date: time.now())
        return updated
    }
}

public struct AcknowledgeAlert: Sendable {
    let transition: AlertTransition
    public init(alerts: any AlertRepository, timeline: any TimelineRepository, time: any TimeProvider) {
        transition = AlertTransition(alerts: alerts, timeline: timeline, time: time)
    }
    public func callAsFunction(alertId: String) async throws -> Alert {
        try await transition.apply(alertId: alertId, status: .acknowledged) { .acknowledged(level: $0.recipient.level) }
    }
}

public struct MarkArrived: Sendable {
    let transition: AlertTransition
    public init(alerts: any AlertRepository, timeline: any TimelineRepository, time: any TimeProvider) {
        transition = AlertTransition(alerts: alerts, timeline: timeline, time: time)
    }
    public func callAsFunction(alertId: String) async throws -> Alert {
        try await transition.apply(alertId: alertId, status: .arrived) { _ in .neighbourReached }
    }
}

/// Neighbour escalates to the next level (emergency services).
public struct EscalateAlert: Sendable {
    let transition: AlertTransition
    public init(alerts: any AlertRepository, timeline: any TimelineRepository, time: any TimeProvider) {
        transition = AlertTransition(alerts: alerts, timeline: timeline, time: time)
    }
    public func callAsFunction(alertId: String) async throws -> Alert {
        try await transition.apply(alertId: alertId, status: .escalated) { .escalated(level: $0.recipient.level + 1) }
    }
}

/// Records a neighbour/hospital answer; terminal outcomes resolve the alert.
public struct RecordCareOutcome: Sendable {
    let alerts: any AlertRepository
    let transition: AlertTransition
    public init(alerts: any AlertRepository, timeline: any TimelineRepository, time: any TimeProvider) {
        self.alerts = alerts
        transition = AlertTransition(alerts: alerts, timeline: timeline, time: time)
    }
    public func callAsFunction(alertId: String, outcome: CareOutcome) async throws -> Alert {
        let current = try await alerts.alert(id: alertId)
        let status = outcome.resolvesAlert ? .resolved : current.status
        return try await transition.apply(alertId: alertId, status: status) { _ in .careOutcome(outcome) }
    }
}
