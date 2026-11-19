import Domain
import Foundation
import Observation

/// "Help" alert for both the neighbour (`alert.*`) and the hospital (`hospitalAlert.*`): message, Accept and a
/// display-only countdown to `acknowledgeBy`.
@MainActor @Observable
final class HelpAlertViewModel {
    private(set) var countdownText: String
    private(set) var isBusy = false
    private(set) var errorMessage: String?

    let alert: AlertSummary
    let step = 1

    var totalSteps: Int { alert.recipient == .neighbour ? FlowSteps.neighbour : FlowSteps.hospital }
    var idPrefix: String { alert.recipient == .neighbour ? "alert" : "hospitalAlert" }
    var message: String {
        switch alert.recipient {
        case .neighbour:
            "Your neighbour \(alert.subjectName) needs your help, please acknowledge the notification"
        case .hospital:
            "The patient \(alert.subjectName) needs your help, please acknowledge the notification"
        }
    }

    private let acknowledgeAlert: AcknowledgeAlert
    private let time: any TimeProvider
    private let onAccepted: (AlertSummary) -> Void

    init(alert: AlertSummary, acknowledgeAlert: AcknowledgeAlert, time: any TimeProvider,
         onAccepted: @escaping (AlertSummary) -> Void) {
        self.alert = alert
        self.acknowledgeAlert = acknowledgeAlert
        self.time = time
        self.onAccepted = onAccepted
        countdownText = Self.countdown(alert: alert, now: time.now())
    }

    /// Called once a second by the view.
    func tick() {
        let text = Self.countdown(alert: alert, now: time.now())
        if text != countdownText { countdownText = text }
    }

    func accept() async {
        guard !isBusy else { return }
        isBusy = true
        defer { isBusy = false }
        do {
            _ = try await acknowledgeAlert(alertId: alert.id)
            onAccepted(alert)
        } catch {
            errorMessage = DisplayText.genericError
        }
    }

    static func countdown(alert: AlertSummary, now: Date) -> String {
        DisplayText.countdown(seconds: max(0, Int(alert.acknowledgeBy.timeIntervalSince(now).rounded(.up))))
    }
}
