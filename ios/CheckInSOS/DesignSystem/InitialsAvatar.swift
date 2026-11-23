import SwiftUI

/// A circle with someone's initials ("MW"). Decorative: the row it sits in already reads the name.
struct InitialsAvatar: View {
    @Environment(\.roleTheme) private var theme
    let name: String
    @ScaledMetric(relativeTo: .headline) private var size: CGFloat = 44

    var body: some View {
        Text(DisplayText.initials(name))
            .font(.headline)
            .foregroundStyle(theme.onPrimary)
            .frame(width: size, height: size)
            .background(theme.primary, in: Circle())
            .accessibilityHidden(true)
    }
}
