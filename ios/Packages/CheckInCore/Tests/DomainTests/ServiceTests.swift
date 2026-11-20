import Foundation
import Testing
@testable import Domain

@Suite struct ScoreCalculatorTests {
    @Test func reproducesPrototypeExamples() {
        #expect(ScoreCalculator.score(Assessment(physical: .good, mood: .pleasant)) == 76)
        #expect(ScoreCalculator.score(Assessment(physical: .ill, mood: .anxious)) == 38)
    }

    @Test func scoresStayWithinZeroToHundred() {
        for p in PhysicalWellbeing.allCases {
            for m in Mood.allCases {
                #expect((0...100).contains(ScoreCalculator.score(Assessment(physical: p, mood: m))))
            }
        }
        #expect(ScoreCalculator.score(Assessment(physical: .fine, mood: .happy)) == 100)
        #expect(ScoreCalculator.score(Assessment(physical: .verySick, mood: .depressed)) == 10)
    }

    @Test(arguments: [(69, false), (70, true), (100, true), (0, false)])
    func goodThreshold(score: Int, good: Bool) {
        #expect(ScoreCalculator.isGood(score) == good)
        #expect(EscalationPolicy.shouldAlert(score: score) == !good)
    }
}

@Suite struct EscalationPolicyTests {
    @Test(arguments: [
        (100, nil), (70, nil), (69, 30.0), (50, 30.0), (49, 20.0), (30, 20.0), (29, 15.0), (0, 15.0),
    ] as [(Int, Double?)])
    func timeoutByScore(score: Int, minutes: Double?) {
        #expect(EscalationPolicy.acknowledgementTimeout(forScore: score) == minutes.map { $0 * 60 })
    }

    @Test func missedCheckInUsesPlaceholderTimeout() {
        let timeout = EscalationPolicy.acknowledgementTimeout(forScore: nil)
        #expect(timeout == 900)
    }
}

@Suite struct NeighbourValidatorTests {
    let valid = Neighbour(firstName: " Alex ", lastName: "Brown", email: "alex@example.com", mobile: "07700 900123")

    @Test func trimsAndAccepts() throws {
        let result = try NeighbourValidator.validate(valid)
        #expect(result.firstName == "Alex")
        #expect(result.displayName == "Alex Brown")
    }

    @Test(arguments: [
        (Neighbour(firstName: "  ", lastName: "B", email: "a@example.com", mobile: "07700900123"), NeighbourValidationError.firstNameRequired),
        (Neighbour(firstName: "A", lastName: "", email: "a@example.com", mobile: "07700900123"), .lastNameRequired),
        (Neighbour(firstName: "A", lastName: "B", email: "ABC@mail", mobile: "07700900123"), .invalidEmail),
        (Neighbour(firstName: "A", lastName: "B", email: "a@example.com", mobile: "0712"), .invalidMobile),
        (Neighbour(firstName: "A", lastName: "B", email: "a@example.com", mobile: "07700abc123"), .invalidMobile),
    ])
    func rejects(neighbour: Neighbour, error: NeighbourValidationError) {
        #expect(throws: error) { try NeighbourValidator.validate(neighbour) }
    }

    @Test(arguments: ["+44 7700 900123", "07700-900-123", "0770090012"])
    func acceptsUkMobileFormats(mobile: String) {
        #expect(NeighbourValidator.isValidMobile(mobile))
    }

    @Test func errorMessagesMatchSpec() {
        #expect(NeighbourValidationError.firstNameRequired.message == "First name is required")
        #expect(NeighbourValidationError.lastNameRequired.message == "Last name is required")
        #expect(NeighbourValidationError.invalidEmail.message == "Enter a valid email")
        #expect(NeighbourValidationError.invalidMobile.message == "Enter a valid UK mobile number")
    }
}

@Suite struct EntityTests {
    @Test(arguments: [
        (TimelineEventKind.score(38), "Score 38", TimelineSeverity.critical),
        (.score(76), "Score 76", .ok),
        (.missedCheckIn, "Missed check-in", .critical),
        (.notificationSent(level: 1), "Notification sent to 1st level", .critical),
        (.acknowledged(level: 2), "2nd level acknowledged", .warning),
        (.neighbourReached, "Neighbour reached", .ok),
        (.escalated(level: 3), "Escalation to 3rd level", .critical),
        (.escalated(level: 4), "Escalation to 4th level", .critical),
        (.careOutcome(.allOk), "Neighbour reports all OK", .ok),
        (.careOutcome(.inCare), "In care in hospital", .warning),
        (.careOutcome(.recovering), "Recovering", .ok),
        (.careOutcome(.recovered), "Recovered", .ok),
        (.careOutcome(.badCondition), "In bad condition", .critical),
        (.careOutcome(.alive), "Confirmed alive", .ok),
        (.careOutcome(.deceased), "Reported deceased", .critical),
    ])
    func timelineCopyAndSeverity(kind: TimelineEventKind, title: String, severity: TimelineSeverity) {
        #expect(kind.title == title)
        #expect(kind.severity == severity)
    }

    @Test func careOutcomeResolution() {
        let resolving = CareOutcome.allCases.filter(\.resolvesAlert)
        #expect(Set(resolving) == [.allOk, .recovered, .alive, .deceased])
    }

    @Test func alertCountdown() {
        let alert = Alert(id: "a", olderAdultId: "o", subjectName: "X", recipient: .neighbour, score: 38,
                          raisedAt: t0, acknowledgeBy: t0.addingTimeInterval(1200))
        #expect(alert.secondsRemaining(at: t0) == 1200)
        #expect(alert.secondsRemaining(at: t0.addingTimeInterval(1199.5)) == 1)
        #expect(alert.secondsRemaining(at: t0.addingTimeInterval(5000)) == 0)
        #expect(AlertRecipient.hospital.level == 2)
        #expect(Role.allCases.count == 4)
    }

    @Test func timeProviders() {
        #expect(FixedTimeProvider(t0).now() == t0)
        #expect(abs(SystemTimeProvider().now().timeIntervalSinceNow) < 5)
    }
}
