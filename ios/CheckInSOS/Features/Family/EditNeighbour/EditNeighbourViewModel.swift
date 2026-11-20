import Domain
import Observation

/// Edit the 1st-level notified person. The mobile field sits after a fixed "+44" prefix, so it shows the
/// national number without its leading "0"; saving puts the "0" back (docs/screens.md).
@MainActor @Observable
final class EditNeighbourViewModel {
    var firstName = ""
    var middleName = ""
    var lastName = ""
    var email = ""
    var mobile = ""
    private(set) var errorMessage: String?
    private(set) var isSaving = false
    private(set) var isLoaded = false

    let heading = "Your 1st level notified person"
    let mobilePrefix = "+44"

    private let olderAdultId: String
    private let getStatus: GetLovedOneStatus
    private let updateNeighbour: UpdateNeighbour
    private let onSaved: () -> Void

    init(olderAdultId: String, getStatus: GetLovedOneStatus, updateNeighbour: UpdateNeighbour,
         onSaved: @escaping () -> Void) {
        self.olderAdultId = olderAdultId
        self.getStatus = getStatus
        self.updateNeighbour = updateNeighbour
        self.onSaved = onSaved
    }

    /// Pre-fills the form once; later appearances keep what the user typed.
    func load() async {
        guard !isLoaded else { return }
        do {
            let neighbour = try await getStatus(olderAdultId: olderAdultId).neighbour
            firstName = neighbour.firstName
            middleName = neighbour.middleName
            lastName = neighbour.lastName
            email = neighbour.email
            mobile = Self.nationalNumber(neighbour.mobile)
            isLoaded = true
        } catch {
            errorMessage = DisplayText.genericError
        }
    }

    func save() async {
        guard !isSaving else { return }
        isSaving = true
        defer { isSaving = false }
        let neighbour = Neighbour(firstName: firstName, middleName: middleName, lastName: lastName,
                                  email: email, mobile: Self.storedNumber(mobile))
        do {
            _ = try await updateNeighbour(olderAdultId: olderAdultId, neighbour: neighbour)
            errorMessage = nil
            onSaved()
        } catch DomainError.invalidNeighbour(let reason) {
            errorMessage = reason.message
        } catch {
            errorMessage = DisplayText.genericError
        }
    }

    /// "07700900123" → "7700900123" (shown after the "+44" prefix).
    static func nationalNumber(_ stored: String) -> String {
        stored.hasPrefix("0") ? String(stored.dropFirst()) : stored
    }

    /// "7700 900123" → "07700 900123". Numbers already starting "0" or "+" are kept as typed.
    static func storedNumber(_ field: String) -> String {
        let trimmed = field.trimmingCharacters(in: .whitespaces)
        if trimmed.isEmpty || trimmed.hasPrefix("0") || trimmed.hasPrefix("+") { return trimmed }
        return "0" + trimmed
    }
}
