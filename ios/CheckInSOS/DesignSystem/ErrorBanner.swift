import SwiftUI

/// Inline error message (test ID `error.message` unless a screen gives its own).
struct ErrorBanner: View {
    let message: String
    var testID = "error.message"

    var body: some View {
        Label(message, systemImage: "exclamationmark.triangle.fill")
            .font(.body.weight(.semibold))
            .foregroundStyle(Theme.statusCritical)
            .fixedSize(horizontal: false, vertical: true)
            .accessibilityIdentifier(testID)
    }
}
