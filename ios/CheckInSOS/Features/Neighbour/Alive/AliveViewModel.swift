import Domain
import Observation

/// "Is your neighbour alive?" — step 4. Both answers are recorded and return to the Neighbours list.
@MainActor @Observable
final class AliveViewModel {
    private(set) var isBusy = false
    private(set) var errorMessage: String?

    let alert: AlertSummary
    let step = 4
    let totalSteps = FlowSteps.neighbour
    let heading = "Emergency services alerted, Is your neighbour alive?"
    /// "Demo: would call 999" — 999/111 never dial in this build.
    let demoMessage: String?

    private let recordOutcome: RecordCareOutcome
    private let onDone: () -> Void

    init(alert: AlertSummary, calledNumber: String?, recordOutcome: RecordCareOutcome, onDone: @escaping () -> Void) {
        self.alert = alert
        self.demoMessage = calledNumber.map { "Demo: would call \($0)" }
        self.recordOutcome = recordOutcome
        self.onDone = onDone
    }

    func answer(alive: Bool) async {
        guard !isBusy else { return }
        isBusy = true
        defer { isBusy = false }
        do {
            _ = try await recordOutcome(alertId: alert.id, outcome: alive ? .alive : .deceased)
            onDone()
        } catch {
            errorMessage = DisplayText.genericError
        }
    }
}
