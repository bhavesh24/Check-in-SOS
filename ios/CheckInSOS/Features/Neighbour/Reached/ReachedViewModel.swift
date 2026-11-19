import Domain
import Observation

/// "Have you reached?" — step 2 of the neighbour flow.
@MainActor @Observable
final class ReachedViewModel {
    private(set) var isBusy = false
    private(set) var errorMessage: String?

    let alert: AlertSummary
    let step = 2
    let totalSteps = FlowSteps.neighbour
    var message: String {
        "You have acknowledged to help your neighbour \(alert.subjectName), please let us know have you reached"
    }

    private let markArrived: MarkArrived
    private let onReached: (AlertSummary) -> Void

    init(alert: AlertSummary, markArrived: MarkArrived, onReached: @escaping (AlertSummary) -> Void) {
        self.alert = alert
        self.markArrived = markArrived
        self.onReached = onReached
    }

    func confirm() async {
        guard !isBusy else { return }
        isBusy = true
        defer { isBusy = false }
        do {
            _ = try await markArrived(alertId: alert.id)
            onReached(alert)
        } catch {
            errorMessage = DisplayText.genericError
        }
    }
}
