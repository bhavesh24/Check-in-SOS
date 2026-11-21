import Domain
import Foundation
import Testing
@testable import CheckInSOS

@MainActor @Suite struct NeighboursAndPatientsViewModelTests {
    let store = FakeStore()
    var simulate: SimulateAlert {
        SimulateAlert(olderAdults: store, patients: store, alerts: store, timeline: store, time: FixedTimeProvider(t0))
    }

    @Test func neighboursLoadAndSimulate() async {
        let alerts = Recorder<AlertSummary>()
        let vm = NeighboursViewModel(getNeighbours: GetNeighbours(olderAdults: store), simulateAlert: simulate,
                                     onAlert: alerts.record)
        await vm.load()
        #expect(vm.neighbours.map(\.latestScore) == [76, 38])
        await vm.simulate()
        #expect(alerts.values.map(\.subjectName) == ["Arthur Patel"])
        #expect(alerts.values.first?.recipient == .neighbour)
        #expect(alerts.values.first?.acknowledgeBy == t0.addingTimeInterval(20 * 60))
        #expect(!vm.isBusy)
    }

    @Test func patientsLoadAndSimulate() async {
        let alerts = Recorder<AlertSummary>()
        let vm = PatientsViewModel(getPatients: GetPatients(patients: store), simulateAlert: simulate,
                                   onAlert: alerts.record)
        await vm.load()
        #expect(vm.patients.map(\.latestScore) == [38])
        await vm.simulate()
        #expect(alerts.values.first?.recipient == .hospital)
    }

    @Test func failuresShowError() async {
        await store.setFailing(true)
        let neighbours = NeighboursViewModel(getNeighbours: GetNeighbours(olderAdults: store), simulateAlert: simulate,
                                             onAlert: { _ in })
        await neighbours.load()
        #expect(neighbours.errorMessage == DisplayText.genericError)
        let patients = PatientsViewModel(getPatients: GetPatients(patients: store), simulateAlert: simulate,
                                         onAlert: { _ in })
        await patients.simulate()
        await patients.load()
        #expect(patients.errorMessage == DisplayText.genericError)
        await neighbours.simulate()
        #expect(neighbours.errorMessage == DisplayText.genericError)
    }
}

@MainActor @Suite struct HelpAlertViewModelTests {
    let store = FakeStore()

    @Test func neighbourAlertCountsDownAndAccepts() async throws {
        let summary = try await store.seedAlert()
        let clock = MutableTimeProvider(t0)
        let accepted = Recorder<AlertSummary>()
        let vm = HelpAlertViewModel(alert: summary,
                                    acknowledgeAlert: AcknowledgeAlert(alerts: store, timeline: store, time: clock),
                                    time: clock, onAccepted: accepted.record)
        #expect(vm.message == "Your neighbour Arthur Patel needs your help, please acknowledge the notification")
        #expect(vm.idPrefix == "alert" && vm.step == 1 && vm.totalSteps == 4)
        #expect(vm.countdownText == "20:00" && vm.countdownProgress == 1)
        #expect(vm.title == "Urgent alert" && vm.bannerText == "Urgent: someone needs help now")
        #expect(vm.countdownCaption == "left to respond")
        clock.date = t0.addingTimeInterval(600)
        vm.tick()
        #expect(vm.countdownText == "10:00" && vm.countdownProgress == 0.5)
        clock.date = t0.addingTimeInterval(61)
        vm.tick()
        #expect(vm.countdownText == "18:59")
        vm.tick()
        #expect(vm.countdownText == "18:59")
        clock.date = t0.addingTimeInterval(3600)
        vm.tick()
        #expect(vm.countdownText == "Time expired" && vm.countdownProgress == 0)

        await vm.accept()
        #expect(accepted.values == [summary])
        #expect(try await store.alert(id: summary.id).status == .acknowledged)
        #expect(await store.kinds() == [.acknowledged(level: 1)])
    }

    @Test func hospitalCopyAndFailure() async throws {
        let summary = try await store.seedAlert(recipient: .hospital)
        await store.setFailing(true)
        let vm = HelpAlertViewModel(alert: summary,
                                    acknowledgeAlert: AcknowledgeAlert(alerts: store, timeline: store,
                                                                       time: FixedTimeProvider(t0)),
                                    time: FixedTimeProvider(t0), onAccepted: { _ in Issue.record("unexpected") })
        #expect(vm.message == "The patient Arthur Patel needs your help, please acknowledge the notification")
        #expect(vm.idPrefix == "hospitalAlert" && vm.totalSteps == 5)
        await vm.accept()
        #expect(vm.errorMessage == DisplayText.genericError)
    }
}

@MainActor @Suite struct NeighbourStepViewModelTests {
    let store = FakeStore()
    var record: RecordCareOutcome { RecordCareOutcome(alerts: store, timeline: store, time: FixedTimeProvider(t0)) }

    @Test func reachedMarksArrived() async throws {
        let summary = try await store.seedAlert()
        let reached = Recorder<AlertSummary>()
        let vm = ReachedViewModel(alert: summary,
                                  markArrived: MarkArrived(alerts: store, timeline: store, time: FixedTimeProvider(t0)),
                                  onReached: reached.record)
        #expect(vm.message ==
                "You have acknowledged to help your neighbour Arthur Patel, please let us know have you reached")
        #expect(vm.step == 2 && vm.totalSteps == 4)
        await vm.confirm()
        #expect(reached.values == [summary])
        #expect(await store.kinds() == [.neighbourReached])

        await store.setFailing(true)
        await vm.confirm()
        #expect(vm.errorMessage == DisplayText.genericError)
    }

    func makeEscalate(_ summary: AlertSummary, allOk: Recorder<Void>, called: Recorder<String>) -> EscalateViewModel {
        EscalateViewModel(alert: summary, recordOutcome: record,
                          escalateAlert: EscalateAlert(alerts: store, timeline: store, time: FixedTimeProvider(t0)),
                          onAllOk: { allOk.record(()) }, onCalled: { called.record($1) })
    }

    @Test func escalateAllOkResolves() async throws {
        let summary = try await store.seedAlert()
        let allOk = Recorder<Void>(), called = Recorder<String>()
        let vm = makeEscalate(summary, allOk: allOk, called: called)
        #expect(vm.heading == "Do you want to escalate?" && vm.step == 3)
        await vm.allOk()
        #expect(allOk.values.count == 1)
        #expect(try await store.alert(id: summary.id).status == .resolved)
        #expect(await store.kinds() == [.careOutcome(.allOk)])
    }

    @Test func escalateOpensSheetThenDemoCallsOrCancels() async throws {
        let summary = try await store.seedAlert()
        let allOk = Recorder<Void>(), called = Recorder<String>()
        let vm = makeEscalate(summary, allOk: allOk, called: called)
        await vm.alertServices()
        #expect(vm.isEmergencySheetPresented)
        #expect(await store.kinds() == [.escalated(level: 2)])
        vm.cancel()
        #expect(!vm.isEmergencySheetPresented)
        await vm.alertServices()
        #expect(vm.emergencyNumbers == ["999", "111"])
        vm.call("111")
        #expect(!vm.isEmergencySheetPresented)
        #expect(called.values == ["111"])

        await store.setFailing(true)
        await vm.allOk()
        #expect(vm.errorMessage == DisplayText.genericError)
        #expect(allOk.values.isEmpty)
    }

    @Test(arguments: [(true, CareOutcome.alive), (false, CareOutcome.deceased)])
    func aliveRecordsOutcome(alive: Bool, outcome: CareOutcome) async throws {
        let summary = try await store.seedAlert()
        let done = Recorder<Void>()
        let vm = AliveViewModel(alert: summary, calledNumber: "999", recordOutcome: record, onDone: { done.record(()) })
        #expect(vm.demoMessage == "Demo: would call 999")
        #expect(vm.heading == "Emergency services alerted, Is your neighbour alive?")
        #expect(vm.step == 4 && vm.totalSteps == 4)
        await vm.answer(alive: alive)
        #expect(done.values.count == 1)
        #expect(await store.kinds() == [.careOutcome(outcome)])
    }

    @Test func aliveWithoutCallAndFailure() async throws {
        let summary = try await store.seedAlert()
        let vm = AliveViewModel(alert: summary, calledNumber: nil, recordOutcome: record, onDone: {})
        #expect(vm.demoMessage == nil)
        await store.setFailing(true)
        await vm.answer(alive: true)
        #expect(vm.errorMessage == DisplayText.genericError)
    }
}

@MainActor @Suite struct CareStepViewModelTests {
    let store = FakeStore()

    @Test func stepTableMatchesSpec() {
        #expect(CareStep.allCases.map(\.stepNumber) == [2, 3, 4, 4, 5])
        #expect(CareStep.allCases.map(\.title) == ["In care", "Recovery", "Recovery", "Condition", "Patient status"])
        #expect(CareStep.inCare.heading == "Have you taken them in your care?")
        #expect(CareStep.recovering.heading == "Are they recovering?")
        #expect(CareStep.recovered.heading == "Have they recovered?")
        #expect(CareStep.badCondition.heading == "Are they in bad condition?")
        #expect(CareStep.patientAlive.heading == "Is your patient alive?")
        #expect(CareStep.allCases.flatMap { $0.answers.map(\.testID) } == [
            "inCare.yes", "recovering.yes", "recovering.no", "recovered.yes",
            "badCondition.yes", "badCondition.no", "patientAlive.no", "patientAlive.yes",
        ])
        #expect(CareStep.recovering.answers.map(\.next) == [.recovered, .badCondition])
        #expect(CareStep.patientAlive.answers.map(\.outcome) == [.deceased, .alive])
    }

    @Test func answerRecordsOutcomeAndAdvances() async throws {
        let summary = try await store.seedAlert(recipient: .hospital)
        let next = Recorder<CareStep?>()
        let vm = CareStepViewModel(step: .recovering, alert: summary,
                                   recordOutcome: RecordCareOutcome(alerts: store, timeline: store,
                                                                    time: FixedTimeProvider(t0)),
                                   onNext: next.record)
        #expect(vm.totalSteps == 5)
        await vm.choose(CareStep.recovering.answers[0])
        await vm.choose(CareStep.recovering.answers[1])
        #expect(next.values == [.recovered, .badCondition])
        #expect(await store.kinds() == [.careOutcome(.recovering)])

        await store.setFailing(true)
        await vm.choose(CareStep.recovering.answers[0])
        #expect(vm.errorMessage == DisplayText.genericError)
        #expect(next.values.count == 2)
    }
}
