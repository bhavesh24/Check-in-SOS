import SwiftUI

/// A circle with someone's initials ("MW"). Decorative: the row it sits in already reads the name.
struct InitialsAvatar: View {
    let name: String
    @ScaledMetric(relativeTo: .headline) private var size: CGFloat = 44

    var body: some View {
        Text(DisplayText.initials(name))
            .font(.headline)
            .foregroundStyle(Theme.onPrimary)
            .frame(width: size, height: size)
            .background(Theme.primary, in: Circle())
            .accessibilityHidden(true)
    }
}
