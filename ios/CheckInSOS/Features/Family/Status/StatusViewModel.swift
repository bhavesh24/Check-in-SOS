import Domain
import Observation

/// Current status of one loved one: latest score + 1st-level neighbour. Reloads on every appearance so an
/// edited neighbour shows straight away.
@MainActor @Observable
final class StatusViewModel {
    private(set) var adult: OlderAdult?
    private(set) var errorMessage: String?

    let scoreCaption = "Current score"

    var title: String { adult?.name ?? "" }
    var neighbourText: String { adult.map { "1st level: \($0.neighbour.displayName)" } ?? "" }

    private let olderAdultId: String
    private let getStatus: GetLovedOneStatus
    private let onEditNeighbour: (String) -> Void
    private let onTimeline: (String) -> Void

    init(olderAdultId: String, getStatus: GetLovedOneStatus,
         onEditNeighbour: @escaping (String) -> Void, onTimeline: @escaping (String) -> Void) {
        self.olderAdultId = olderAdultId
        self.getStatus = getStatus
        self.onEditNeighbour = onEditNeighbour
        self.onTimeline = onTimeline
    }

    func load() async {
        do {
            adult = try await getStatus(olderAdultId: olderAdultId)
            errorMessage = nil
        } catch {
            errorMessage = DisplayText.genericError
        }
    }

    func editNeighbour() { onEditNeighbour(olderAdultId) }
    func openTimeline() { onTimeline(olderAdultId) }
}
