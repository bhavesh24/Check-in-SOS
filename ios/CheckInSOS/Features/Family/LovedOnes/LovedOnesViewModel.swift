import Domain
import Foundation
import Observation

@MainActor @Observable
final class LovedOnesViewModel {
    private(set) var lovedOnes: [OlderAdult] = []
    private(set) var errorMessage: String?

    private let getLovedOnes: GetLovedOnes
    private let time: any TimeProvider
    private let timeZone: TimeZone
    private let onSelect: (String) -> Void

    init(getLovedOnes: GetLovedOnes, time: any TimeProvider, timeZone: TimeZone, onSelect: @escaping (String) -> Void) {
        self.getLovedOnes = getLovedOnes
        self.time = time
        self.timeZone = timeZone
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

    /// "Last check-in Today, 08:00" / "No check-in yet".
    func subtitle(for adult: OlderAdult) -> String {
        DisplayText.lastCheckIn(adult.lastCheckIn, now: time.now(), timeZone: timeZone)
    }

    func select(_ adult: OlderAdult) { onSelect(adult.id) }
}
