import Domain
import Observation

@MainActor @Observable
final class LovedOnesViewModel {
    private(set) var lovedOnes: [OlderAdult] = []
    private(set) var errorMessage: String?

    private let getLovedOnes: GetLovedOnes
    private let onSelect: (String) -> Void

    init(getLovedOnes: GetLovedOnes, onSelect: @escaping (String) -> Void) {
        self.getLovedOnes = getLovedOnes
        self.onSelect = onSelect
    }

    func load() async {
        do {
            lovedOnes = try await getLovedOnes()
            errorMessage = nil
        } catch {
            errorMessage = DisplayText.genericError
        }
    }

    func subtitle(for adult: OlderAdult) -> String { DisplayText.score(adult.latestScore) }

    func select(_ adult: OlderAdult) { onSelect(adult.id) }
}
