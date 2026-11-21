import Foundation
import Testing
@testable import Domain

@Suite struct SubmitAssessmentTests {
    let store = FakeStore(adults: [adult("oa-1", "Margaret", score: nil)])
    var submit: SubmitAssessment {
        SubmitAssessment(olderAdults: store, alerts: store, timeline: store, time: FixedTimeProvider(t0))
    }

    @Test func goodScoreDoesNotNotify() async throws {
        let result = try await submit(olderAdultId: "oa-1", assessment: Assessment(physical: .good, mood: .pleasant))
        #expect(result == AssessmentResult(score: 76, neighbourNotified: false))
        #expect(try await store.olderAdult(id: "oa-1").latestScore == 76)
        #expect(try await store.olderAdult(id: "oa-1").lastCheckIn == t0)
        #expect(await store.history == [ScoreRecord(score: 76, date: t0)])
        #expect(await store.kinds() == [.score(76)])
        #expect(await store.alerts.isEmpty)
    }

    @Test func lowScoreNotifiesNeighbourWithPolicyTimeout() async throws {
        let result = try await submit(olderAdultId: "oa-1", assessment: Assessment(physical: .ill, mood: .anxious))
        #expect(result == AssessmentResult(score: 38, neighbourNotified: true))
        #expect(await store.kinds() == [.score(38), .notificationSent(level: 1)])
        let alert = try #require(await store.alerts.values.first)
        #expect(alert.recipient == .neighbour)
        #expect(alert.subjectName == "Margaret")
        #expect(alert.acknowledgeBy == t0.addingTimeInterval(20 * 60))
    }

    @Test func unknownOlderAdultThrows() async {
        await #expect(throws: DomainError.notFound("nope")) {
            try await submit(olderAdultId: "nope", assessment: Assessment(physical: .fine, mood: .happy))
        }
    }
}

@Suite struct FamilyUseCaseTests {
    let store = FakeStore(adults: [adult("oa-1", "Margaret", score: 76), adult("oa-2", "Arthur", score: 38)])

    @Test func lovedOnesAndStatus() async throws {
        #expect(try await GetLovedOnes(olderAdults: store)().map(\.id) == ["oa-1", "oa-2"])
        #expect(try await GetLovedOneStatus(olderAdults: store)(olderAdultId: "oa-2").latestScore == 38)
    }

    @Test func updateNeighbourValidatesAndSaves() async throws {
        let update = UpdateNeighbour(olderAdults: store)
        let saved = try await update(olderAdultId: "oa-1", neighbour: Neighbour(
            firstName: " Sam ", lastName: "Lee", email: "sam.lee@example.com", mobile: "07700900999"))
        #expect(saved.neighbour.displayName == "Sam Lee")
        await #expect(throws: DomainError.invalidNeighbour(.invalidEmail)) {
            try await update(olderAdultId: "oa-1", neighbour: Neighbour(
                firstName: "Sam", lastName: "Lee", email: "nope", mobile: "07700900999"))
        }
    }

    @Test func scoreHistoryIsTheLatestSevenOldestFirst() async throws {
        for day in (0..<9).reversed() {
            try await store.recordScore(olderAdultId: "oa-1", score: 60 + day, at: t0.addingTimeInterval(-Double(day) * 86_400))
        }
        let history = try await GetScoreHistory(olderAdults: store)(olderAdultId: "oa-1")
        #expect(history.map(\.score) == [66, 65, 64, 63, 62, 61, 60])
        #expect(history.last?.date == t0)
    }

    @Test func timelineIsSortedOldestFirst() async throws {
        _ = try await store.append(olderAdultId: "oa-1", kind: .neighbourReached, date: t0)
        _ = try await store.append(olderAdultId: "oa-1", kind: .score(30), date: t0.addingTimeInterval(-60))
        _ = try await store.append(olderAdultId: "oa-2", kind: .missedCheckIn, date: t0)
        let events = try await GetTimeline(timeline: store)(olderAdultId: "oa-1")
        #expect(events.map(\.kind) == [.score(30), .neighbourReached])
    }
}

@Suite struct AlertUseCaseTests {
    let store = FakeStore(
        adults: [adult("oa-1", "Margaret", score: 76), adult("oa-2", "Arthur", score: 38), adult("oa-3", "Edith", score: nil)],
        patients: [Patient(id: "p-1", name: "Edith", olderAdultId: "oa-3", latestScore: nil),
                   Patient(id: "p-2", name: "Arthur", olderAdultId: "oa-2", latestScore: 38)]
    )
    let time = FixedTimeProvider(t0)
    var simulate: SimulateAlert {
        SimulateAlert(olderAdults: store, patients: store, alerts: store, timeline: store, time: time)
    }

    @Test func listsNeighboursAndPatients() async throws {
        #expect(try await GetNeighbours(olderAdults: store)().count == 3)
        #expect(try await GetPatients(patients: store)().map(\.id) == ["p-1", "p-2"])
    }

    @Test func simulatePicksLowestScore() async throws {
        let neighbourAlert = try await simulate(recipient: .neighbour)
        #expect(neighbourAlert.subjectName == "Arthur")
        #expect(neighbourAlert.acknowledgeBy == t0.addingTimeInterval(20 * 60))
        let hospitalAlert = try await simulate(recipient: .hospital)
        #expect(hospitalAlert.olderAdultId == "oa-2")
        #expect(hospitalAlert.recipient == .hospital)
        #expect(await store.kinds() == [.notificationSent(level: 1), .notificationSent(level: 2)])
    }

    @Test func simulateFallsBackToUnscoredThenGoodScoreTimeouts() async throws {
        let unscored = FakeStore(adults: [adult("oa-3", "Edith", score: nil)])
        let alert = try await SimulateAlert(olderAdults: unscored, patients: unscored, alerts: unscored,
                                            timeline: unscored, time: time)(recipient: .neighbour)
        #expect(alert.score == nil)
        #expect(alert.acknowledgeBy == t0.addingTimeInterval(EscalationPolicy.missedCheckInTimeout))

        let healthy = FakeStore(adults: [adult("oa-1", "Margaret", score: 90)])
        let good = try await SimulateAlert(olderAdults: healthy, patients: healthy, alerts: healthy,
                                           timeline: healthy, time: time)(recipient: .neighbour)
        #expect(good.acknowledgeBy == t0.addingTimeInterval(EscalationPolicy.missedCheckInTimeout))
    }

    @Test func simulateWithNobodyThrows() async {
        let empty = FakeStore()
        await #expect(throws: DomainError.noAlertSubject) {
            try await SimulateAlert(olderAdults: empty, patients: empty, alerts: empty, timeline: empty,
                                    time: time)(recipient: .hospital)
        }
    }

    @Test func neighbourFlowEscalates() async throws {
        let alert = try await simulate(recipient: .neighbour)
        #expect(try await AcknowledgeAlert(alerts: store, timeline: store, time: time)(alertId: alert.id).status == .acknowledged)
        #expect(try await MarkArrived(alerts: store, timeline: store, time: time)(alertId: alert.id).status == .arrived)
        #expect(try await EscalateAlert(alerts: store, timeline: store, time: time)(alertId: alert.id).status == .escalated)
        let done = try await RecordCareOutcome(alerts: store, timeline: store, time: time)(alertId: alert.id, outcome: .alive)
        #expect(done.status == .resolved)
        #expect(await store.kinds() == [
            .notificationSent(level: 1), .acknowledged(level: 1), .neighbourReached,
            .escalated(level: 2), .careOutcome(.alive),
        ])
    }

    @Test func nonTerminalOutcomeKeepsStatus() async throws {
        let alert = try await simulate(recipient: .hospital)
        let record = RecordCareOutcome(alerts: store, timeline: store, time: time)
        _ = try await AcknowledgeAlert(alerts: store, timeline: store, time: time)(alertId: alert.id)
        #expect(try await record(alertId: alert.id, outcome: .inCare).status == .acknowledged)
        #expect(try await record(alertId: alert.id, outcome: .recovered).status == .resolved)
    }

    @Test func unknownAlertThrows() async {
        await #expect(throws: DomainError.notFound("x")) {
            try await AcknowledgeAlert(alerts: store, timeline: store, time: time)(alertId: "x")
        }
    }
}
