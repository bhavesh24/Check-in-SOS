import Domain
import Observation

/// The hospital's patient list. "Simulate alert" stands in for a push notification.
@MainActor @Observable
final class PatientsViewModel {
    private(set) var patients: [Patient] = []
    private(set) var isBusy = false
    private(set) var errorMessage: String?

    private let getPatients: GetPatients
    private let simulateAlert: SimulateAlert
    private let onAlert: (AlertSummary) -> Void

    init(getPatients: GetPatients, simulateAlert: SimulateAlert, onAlert: @escaping (AlertSummary) -> Void) {
        self.getPatients = getPatients
        self.simulateAlert = simulateAlert
        self.onAlert = onAlert
    }

    func load() async {
        do {
            patients = try await getPatients()
            errorMessage = nil
        } catch {
            errorMessage = DisplayText.genericError
        }
    }

    func subtitle(for patient: Patient) -> String { DisplayText.score(patient.latestScore) }

    func simulate() async {
        guard !isBusy else { return }
        isBusy = true
        defer { isBusy = false }
        do {
            onAlert(AlertSummary(try await simulateAlert(recipient: .hospital)))
        } catch {
            errorMessage = DisplayText.genericError
        }
    }
}
