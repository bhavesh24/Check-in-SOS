import Foundation

/// Validation rules for the Edit neighbour form.
public enum NeighbourValidator {
    /// Returns a trimmed copy of `neighbour`, or throws the first rule it breaks.
    public static func validate(_ neighbour: Neighbour) throws(NeighbourValidationError) -> Neighbour {
        let trimmed = Neighbour(
            firstName: neighbour.firstName.trimmingCharacters(in: .whitespaces),
            middleName: neighbour.middleName.trimmingCharacters(in: .whitespaces),
            lastName: neighbour.lastName.trimmingCharacters(in: .whitespaces),
            email: neighbour.email.trimmingCharacters(in: .whitespaces),
            mobile: neighbour.mobile.trimmingCharacters(in: .whitespaces)
        )
        if trimmed.firstName.isEmpty { throw .firstNameRequired }
        if trimmed.lastName.isEmpty { throw .lastNameRequired }
        if !isValidEmail(trimmed.email) { throw .invalidEmail }
        if !isValidMobile(trimmed.mobile) { throw .invalidMobile }
        return trimmed
    }

    static func isValidEmail(_ email: String) -> Bool {
        email.wholeMatch(of: /[^@\s]+@[^@\s]+\.[^@\s]+/) != nil
    }

    /// Spaces/dashes removed, then a leading "+44" or "0" removed; 9–10 digits must remain.
    static func isValidMobile(_ mobile: String) -> Bool {
        var digits = mobile.filter { $0 != " " && $0 != "-" }
        if digits.hasPrefix("+44") { digits.removeFirst(3) } else if digits.hasPrefix("0") { digits.removeFirst() }
        return (9...10).contains(digits.count) && digits.allSatisfy(\.isASCIIDigit)
    }
}

private extension Character {
    var isASCIIDigit: Bool { isASCII && isNumber }
}
