import Domain
import Observation

/// Role picker — the demo entry point.
@MainActor @Observable
final class HomeViewModel {
    struct Option: Identifiable, Equatable {
        let role: Role
        let title: String
        var id: String { "home.role.\(role.rawValue)" }
    }

    /// In the order shown on screen.
    let options: [Option] = Role.allCases.map { Option(role: $0, title: HomeViewModel.title(for: $0)) }

    private let onSelect: (Role) -> Void

    init(onSelect: @escaping (Role) -> Void) {
        self.onSelect = onSelect
    }

    func select(_ role: Role) { onSelect(role) }

    static func title(for role: Role) -> String {
        switch role {
        case .olderAdult: "Old age adult"
        case .family: "Concerned family"
        case .hospital: "Hospital"
        case .neighbour: "Neighbour"
        }
    }
}
