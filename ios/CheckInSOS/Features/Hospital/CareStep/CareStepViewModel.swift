import Domain
import Observation

/// One hospital question step. Records the answer's outcome (if any), then moves on.
@MainActor @Observable
final class CareStepViewModel {
    private(set) var isBusy = false
    private(set) var errorMessage: String?

    let step: CareStep
    let alert: AlertSummary
    let totalSteps = FlowSteps.hospital

    private let recordOutcome: RecordCareOutcome
    private let onNext: (CareStep?) -> Void

    init(step: CareStep, alert: AlertSummary, recordOutcome: RecordCareOutcome, onNext: @escaping (CareStep?) -> Void) {
        self.step = step
        self.alert = alert
        self.recordOutcome = recordOutcome
        self.onNext = onNext
    }

    func choose(_ answer: CareStep.Answer) async {
        guard !isBusy else { return }
        isBusy = true
        defer { isBusy = false }
        do {
            if let outcome = answer.outcome {
                _ = try await recordOutcome(alertId: alert.id, outcome: outcome)
            }
            onNext(answer.next)
        } catch {
            errorMessage = DisplayText.genericError
        }
    }
}
