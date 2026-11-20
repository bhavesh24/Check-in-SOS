import Foundation
import Testing
@testable import Data
import Domain

@Suite struct MockDataTests {
    let source = MockDataSource()
    var adults: OlderAdultRepositoryImpl { OlderAdultRepositoryImpl(source: source) }
    var timeline: TimelineRepositoryImpl { TimelineRepositoryImpl(source: source) }
    var alerts: AlertRepositoryImpl { AlertRepositoryImpl(source: source) }

    @Test func seedMatchesSpec() async throws {
        let lovedOnes = try await adults.lovedOnes()
        #expect(lovedOnes.map(\.name) == ["Margaret Wilson", "Arthur Patel", "Edith Clarke"])
        #expect(lovedOnes.map(\.latestScore) == [76, 38, nil])
        #expect(lovedOnes[0].neighbour.displayName == "Alex Brown")
        #expect(try await adults.neighbours().map(\.id) == ["oa-1", "oa-2", "oa-3"])
        let patients = try await PatientRepositoryImpl(source: source).patients()
        #expect(patients.map(\.id) == ["p-1", "p-2", "p-3"])
        #expect(patients[0].name == "Arthur Patel" && patients[0].latestScore == 38)
        #expect(MockFixtures.currentOlderAdultId == "oa-1")
        #expect(ISO8601DateFormatter().string(from: MockFixtures.fixedNow) == "2026-01-15T09:00:00Z")
    }

    @Test func seededTimelineMirrorsPrototype() async throws {
        let events = try await timeline.events(olderAdultId: "oa-2")
        #expect(events.map(\.kind.title) == [
            "Score 38", "Notification sent to 1st level", "1st level acknowledged", "Neighbour reached",
            "Escalation to 2nd level", "2nd level acknowledged", "In care in hospital", "Recovered",
        ])
        #expect(events.first?.date == MockFixtures.fixedNow.addingTimeInterval(-3 * 3600))
        #expect(try await timeline.events(olderAdultId: "oa-3").isEmpty)
        let added = try await timeline.append(olderAdultId: "oa-3", kind: .missedCheckIn, date: MockFixtures.fixedNow)
        #expect(added.id == "event-10")
        #expect(try await timeline.events(olderAdultId: "oa-3") == [added])
    }

    @Test func updatesScoreAndNeighbour() async throws {
        try await adults.recordScore(olderAdultId: "oa-3", score: 55)
        let updated = try await adults.updateNeighbour(olderAdultId: "oa-3", neighbour: Neighbour(
            firstName: "Sam", lastName: "Lee", email: "sam.lee@example.com", mobile: "07700900999"))
        #expect(updated.latestScore == 55)
        #expect(try await adults.olderAdult(id: "oa-3").neighbour.displayName == "Sam Lee")
        #expect(try await PatientRepositoryImpl(source: source).patients()[1].latestScore == 55)
    }

    @Test func alertLifecycle() async throws {
        let now = MockFixtures.fixedNow
        let created = try await alerts.create(olderAdultId: "oa-2", subjectName: "Arthur Patel", recipient: .neighbour,
                                              score: 38, raisedAt: now, acknowledgeBy: now.addingTimeInterval(1200))
        #expect(created.id == "alert-1" && created.status == .pending)
        #expect(try await alerts.updateStatus(alertId: created.id, status: .acknowledged).status == .acknowledged)
        #expect(try await alerts.alert(id: created.id).status == .acknowledged)
    }

    @Test func missingIdsThrowNotFound() async {
        await #expect(throws: DomainError.notFound("x")) { try await adults.olderAdult(id: "x") }
        await #expect(throws: DomainError.notFound("x")) { try await adults.recordScore(olderAdultId: "x", score: 1) }
        await #expect(throws: DomainError.notFound("x")) { try await alerts.alert(id: "x") }
        await #expect(throws: DomainError.notFound("x")) { try await alerts.updateStatus(alertId: "x", status: .resolved) }
    }

    @Test func endToEndWithRealRepositories() async throws {
        let time = FixedTimeProvider(MockFixtures.fixedNow)
        let submit = SubmitAssessment(olderAdults: adults, alerts: alerts, timeline: timeline, time: time)
        let result = try await submit(olderAdultId: MockFixtures.currentOlderAdultId,
                                      assessment: Assessment(physical: .ill, mood: .anxious))
        #expect(result.neighbourNotified)
        #expect(try await timeline.events(olderAdultId: "oa-1").map(\.kind).suffix(2)
                == [.score(38), .notificationSent(level: 1)])
    }
}
