import Domain
import Observation

/// The neighbour's list of older adults. "Simulate alert" stands in for a push notification.
@MainActor @Observable
final class NeighboursViewModel {
    private(set) var neighbours: [OlderAdult] = []
    private(set) var isBusy = false
    private(set) var errorMessage: String?

    private let getNeighbours: GetNeighbours
    private let simulateAlert: SimulateAlert
    private let onAlert: (AlertSummary) -> Void

    init(getNeighbours: GetNeighbours, simulateAlert: SimulateAlert, onAlert: @escaping (AlertSummary) -> Void) {
        self.getNeighbours = getNeighbours
        self.simulateAlert = simulateAlert
        self.onAlert = onAlert
    }

    func load() async {
        do {
            neighbours = try await getNeighbours()
            errorMessage = nil
        } catch {
            errorMessage = DisplayText.genericError
        }
    }

    func subtitle(for adult: OlderAdult) -> String { DisplayText.score(adult.latestScore) }

    func simulate() async {
        guard !isBusy else { return }
        isBusy = true
        defer { isBusy = false }
        do {
            onAlert(AlertSummary(try await simulateAlert(recipient: .neighbour)))
        } catch {
            errorMessage = DisplayText.genericError
        }
    }
}
