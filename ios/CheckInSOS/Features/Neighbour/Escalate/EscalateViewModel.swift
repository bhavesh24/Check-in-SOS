import Domain
import Observation

/// "Do you want to escalate?" — step 3. "Yes" escalates, then offers 999/111, which never dial in this build.
@MainActor @Observable
final class EscalateViewModel {
    var isEmergencySheetPresented = false
    private(set) var isBusy = false
    private(set) var errorMessage: String?

    let alert: AlertSummary
    let step = 3
    let totalSteps = FlowSteps.neighbour
    let heading = "Do you want to escalate?"
    let emergencyNumbers = ["999", "111"]

    private let recordOutcome: RecordCareOutcome
    private let escalateAlert: EscalateAlert
    private let onAllOk: () -> Void
    private let onCalled: (AlertSummary, String) -> Void

    init(alert: AlertSummary, recordOutcome: RecordCareOutcome, escalateAlert: EscalateAlert,
         onAllOk: @escaping () -> Void, onCalled: @escaping (AlertSummary, String) -> Void) {
        self.alert = alert
        self.recordOutcome = recordOutcome
        self.escalateAlert = escalateAlert
        self.onAllOk = onAllOk
        self.onCalled = onCalled
    }

    func allOk() async {
        await perform {
            _ = try await recordOutcome(alertId: alert.id, outcome: .allOk)
            onAllOk()
        }
    }

    func alertServices() async {
        await perform {
            _ = try await escalateAlert(alertId: alert.id)
            isEmergencySheetPresented = true
        }
    }

    /// Demo only: never dials. The Alive screen shows "Demo: would call <number>".
    func call(_ number: String) {
        isEmergencySheetPresented = false
        onCalled(alert, number)
    }

    func cancel() { isEmergencySheetPresented = false }

    private func perform(_ work: () async throws -> Void) async {
        guard !isBusy else { return }
        isBusy = true
        defer { isBusy = false }
        do {
            try await work()
            errorMessage = nil
        } catch {
            errorMessage = DisplayText.genericError
        }
    }
}
