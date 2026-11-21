import SwiftUI

/// A full-width Home card: an icon badge in the role's colour, the title and description, and a chevron.
struct RoleCard: View {
    let title: String
    let description: String
    let systemImage: String
    let tint: Color
    let action: () -> Void

    @ScaledMetric(relativeTo: .title2) private var badgeSize: CGFloat = 52

    var body: some View {
        Button(action: action) {
            HStack(spacing: Theme.spacing) {
                Image(systemName: systemImage)
                    .font(.title2.weight(.semibold))
                    .foregroundStyle(Theme.onRole)
                    .frame(width: badgeSize, height: badgeSize)
                    .background(tint, in: Circle())
                VStack(alignment: .leading, spacing: 4) {
                    Text(title)
                        .font(.title3.weight(.semibold))
                        .foregroundStyle(Theme.text)
                    Text(description)
                        .font(.subheadline)
                        .foregroundStyle(Theme.secondaryText)
                }
                .multilineTextAlignment(.leading)
                .fixedSize(horizontal: false, vertical: true)
                .frame(maxWidth: .infinity, alignment: .leading)
                Image(systemName: "chevron.forward")
                    .font(.body.weight(.semibold))
                    .foregroundStyle(Theme.secondaryText)
            }
            .padding(Theme.spacing)
            .frame(maxWidth: .infinity, minHeight: 80)
            .background(Theme.background, in: RoundedRectangle(cornerRadius: Theme.cornerRadius + 4))
            .overlay(RoundedRectangle(cornerRadius: Theme.cornerRadius + 4).stroke(Theme.cardBorder, lineWidth: 1))
            .contentShape(RoundedRectangle(cornerRadius: Theme.cornerRadius + 4))
        }
        .buttonStyle(PressableCardStyle())
        .accessibilityElement(children: .ignore)
        .accessibilityLabel(title)
        .accessibilityHint(description)
        .accessibilityAddTraits(.isButton)
    }
}

/// Gently shrinks a card while it's pressed.
private struct PressableCardStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .scaleEffect(configuration.isPressed ? 0.97 : 1)
            .animation(.snappy, value: configuration.isPressed)
    }
}
