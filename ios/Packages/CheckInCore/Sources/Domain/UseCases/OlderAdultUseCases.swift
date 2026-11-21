/// Scores a check-in, stores it, and notifies the 1st-level neighbour when the score is below 70.
public struct SubmitAssessment: Sendable {
    let olderAdults: any OlderAdultRepository
    let alerts: any AlertRepository
    let timeline: any TimelineRepository
    let time: any TimeProvider

    public init(olderAdults: any OlderAdultRepository, alerts: any AlertRepository,
                timeline: any TimelineRepository, time: any TimeProvider) {
        self.olderAdults = olderAdults
        self.alerts = alerts
        self.timeline = timeline
        self.time = time
    }

    public func callAsFunction(olderAdultId: String, assessment: Assessment) async throws -> AssessmentResult {
        let score = ScoreCalculator.score(assessment)
        let now = time.now()
        try await olderAdults.recordScore(olderAdultId: olderAdultId, score: score, at: now)
        try await timeline.append(olderAdultId: olderAdultId, kind: .score(score), date: now)

        guard EscalationPolicy.shouldAlert(score: score),
              let timeout = EscalationPolicy.acknowledgementTimeout(forScore: score) else {
            return AssessmentResult(score: score, neighbourNotified: false)
        }
        let adult = try await olderAdults.olderAdult(id: olderAdultId)
        _ = try await alerts.create(
            olderAdultId: olderAdultId, subjectName: adult.name, recipient: .neighbour,
            score: score, raisedAt: now, acknowledgeBy: now.addingTimeInterval(timeout)
        )
        try await timeline.append(olderAdultId: olderAdultId, kind: .notificationSent(level: AlertRecipient.neighbour.level), date: now)
        return AssessmentResult(score: score, neighbourNotified: true)
    }
}
