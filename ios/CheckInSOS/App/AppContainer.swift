import Data
import Domain
import Foundation

/// Composition root (excluded from coverage): the only presentation file that imports Data.
/// Wires repositories → use cases → ViewModels by constructor injection.
@MainActor
final class AppContainer {
    static let uiTestingArgument = "-ui-testing"

    let time: any TimeProvider
    let timeZone: TimeZone
    let currentOlderAdultId: String

    private let olderAdults: any OlderAdultRepository
    private let patients: any PatientRepository
    private let alerts: any AlertRepository
    private let timeline: any TimelineRepository

    init(source: MockDataSource, time: any TimeProvider, timeZone: TimeZone,
         currentOlderAdultId: String = MockFixtures.currentOlderAdultId) {
        self.time = time
        self.timeZone = timeZone
        self.currentOlderAdultId = currentOlderAdultId
        olderAdults = OlderAdultRepositoryImpl(source: source)
        patients = PatientRepositoryImpl(source: source)
        alerts = AlertRepositoryImpl(source: source)
        timeline = TimelineRepositoryImpl(source: source)
    }

    /// `-ui-testing`: fresh mock data, the fixed clock and UTC times so tests are deterministic.
    static func make(arguments: [String]) -> AppContainer {
        if arguments.contains(uiTestingArgument) {
            let now = MockFixtures.fixedNow
            return AppContainer(source: MockDataSource(now: now), time: FixedTimeProvider(now),
                                timeZone: TimeZone(identifier: "UTC")!)
        }
        return AppContainer(source: MockDataSource(now: Date()), time: SystemTimeProvider(), timeZone: .current)
    }

    // MARK: Use cases

    private var recordCareOutcome: RecordCareOutcome {
        RecordCareOutcome(alerts: alerts, timeline: timeline, time: time)
    }

    private var getLovedOneStatus: GetLovedOneStatus { GetLovedOneStatus(olderAdults: olderAdults) }

    private var simulateAlert: SimulateAlert {
        SimulateAlert(olderAdults: olderAdults, patients: patients, alerts: alerts, timeline: timeline, time: time)
    }

    // MARK: ViewModels

    func makeHome(onSelect: @escaping (Role) -> Void) -> HomeViewModel {
        HomeViewModel(onSelect: onSelect)
    }

    func makeAssessment(onSubmitted: @escaping (AssessmentResult) -> Void,
                        onMissedCheckIn: @escaping () -> Void) -> AssessmentViewModel {
        AssessmentViewModel(
            submitAssessment: SubmitAssessment(olderAdults: olderAdults, alerts: alerts, timeline: timeline, time: time),
            olderAdultId: currentOlderAdultId, onSubmitted: onSubmitted, onMissedCheckIn: onMissedCheckIn)
    }

    func makeLovedOnes(onSelect: @escaping (String) -> Void) -> LovedOnesViewModel {
        LovedOnesViewModel(getLovedOnes: GetLovedOnes(olderAdults: olderAdults), onSelect: onSelect)
    }

    func makeStatus(olderAdultId: String, onEditNeighbour: @escaping (String) -> Void,
                    onTimeline: @escaping (String) -> Void) -> StatusViewModel {
        StatusViewModel(olderAdultId: olderAdultId, getStatus: getLovedOneStatus,
                        onEditNeighbour: onEditNeighbour, onTimeline: onTimeline)
    }

    func makeEditNeighbour(olderAdultId: String, onSaved: @escaping () -> Void) -> EditNeighbourViewModel {
        EditNeighbourViewModel(olderAdultId: olderAdultId, getStatus: getLovedOneStatus,
                               updateNeighbour: UpdateNeighbour(olderAdults: olderAdults), onSaved: onSaved)
    }

    func makeTimeline(olderAdultId: String) -> TimelineViewModel {
        TimelineViewModel(olderAdultId: olderAdultId, getTimeline: GetTimeline(timeline: timeline), timeZone: timeZone)
    }

    func makeNeighbours(onAlert: @escaping (AlertSummary) -> Void) -> NeighboursViewModel {
        NeighboursViewModel(getNeighbours: GetNeighbours(olderAdults: olderAdults), simulateAlert: simulateAlert,
                            onAlert: onAlert)
    }

    func makePatients(onAlert: @escaping (AlertSummary) -> Void) -> PatientsViewModel {
        PatientsViewModel(getPatients: GetPatients(patients: patients), simulateAlert: simulateAlert, onAlert: onAlert)
    }

    func makeHelpAlert(alert: AlertSummary, onAccepted: @escaping (AlertSummary) -> Void) -> HelpAlertViewModel {
        HelpAlertViewModel(alert: alert, acknowledgeAlert: AcknowledgeAlert(alerts: alerts, timeline: timeline, time: time),
                           time: time, onAccepted: onAccepted)
    }

    func makeReached(alert: AlertSummary, onReached: @escaping (AlertSummary) -> Void) -> ReachedViewModel {
        ReachedViewModel(alert: alert, markArrived: MarkArrived(alerts: alerts, timeline: timeline, time: time),
                         onReached: onReached)
    }

    func makeEscalate(alert: AlertSummary, onAllOk: @escaping () -> Void,
                      onCalled: @escaping (AlertSummary, String) -> Void) -> EscalateViewModel {
        EscalateViewModel(alert: alert, recordOutcome: recordCareOutcome,
                          escalateAlert: EscalateAlert(alerts: alerts, timeline: timeline, time: time),
                          onAllOk: onAllOk, onCalled: onCalled)
    }

    func makeAlive(alert: AlertSummary, calledNumber: String?, onDone: @escaping () -> Void) -> AliveViewModel {
        AliveViewModel(alert: alert, calledNumber: calledNumber, recordOutcome: recordCareOutcome, onDone: onDone)
    }

    func makeCareStep(step: CareStep, alert: AlertSummary, onNext: @escaping (CareStep?) -> Void) -> CareStepViewModel {
        CareStepViewModel(step: step, alert: alert, recordOutcome: recordCareOutcome, onNext: onNext)
    }
}
