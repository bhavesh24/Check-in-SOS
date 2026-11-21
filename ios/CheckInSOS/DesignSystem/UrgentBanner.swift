import SwiftUI

/// Full-width red banner that marks a help request as urgent.
struct UrgentBanner: View {
    let text: String

    var body: some View {
        Label(text, systemImage: "exclamationmark.triangle.fill")
            .font(.headline)
            .foregroundStyle(Theme.onRole)
            .fixedSize(horizontal: false, vertical: true)
            .padding(Theme.spacing)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(Theme.statusCritical, in: RoundedRectangle(cornerRadius: Theme.cornerRadius))
            .accessibilityAddTraits(.isHeader)
    }
}
