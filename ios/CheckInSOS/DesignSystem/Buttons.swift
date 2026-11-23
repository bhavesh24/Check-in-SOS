import SwiftUI

/// Full-width filled button in the flow's role colour, at least 56 pt high.
struct PrimaryButtonStyle: ButtonStyle {
    @Environment(\.roleTheme) private var theme
    @Environment(\.isEnabled) private var isEnabled

    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .font(.headline)
            .multilineTextAlignment(.center)
            .padding(.horizontal, Theme.spacing)
            .padding(.vertical, 12)
            .frame(maxWidth: .infinity, minHeight: Theme.primaryButtonHeight)
            .foregroundStyle(isEnabled ? theme.onPrimary : Theme.disabledText)
            .background(isEnabled ? theme.primary : Theme.disabledBackground,
                        in: RoundedRectangle(cornerRadius: Theme.cornerRadius))
            .opacity(configuration.isPressed ? 0.85 : 1)
            .contentShape(RoundedRectangle(cornerRadius: Theme.cornerRadius))
            .sensoryFeedback(.impact(weight: .light), trigger: configuration.isPressed) { _, pressed in pressed }
    }
}

/// Full-width outlined button for the secondary choice.
struct SecondaryButtonStyle: ButtonStyle {
    @Environment(\.roleTheme) private var theme
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .font(.headline)
            .multilineTextAlignment(.center)
            .padding(.horizontal, Theme.spacing)
            .padding(.vertical, 12)
            .frame(maxWidth: .infinity, minHeight: Theme.primaryButtonHeight)
            .foregroundStyle(theme.primary)
            .background(theme.surfaceTint, in: RoundedRectangle(cornerRadius: Theme.cornerRadius))
            .overlay(RoundedRectangle(cornerRadius: Theme.cornerRadius).stroke(theme.primary, lineWidth: 2))
            .opacity(configuration.isPressed ? 0.85 : 1)
            .contentShape(RoundedRectangle(cornerRadius: Theme.cornerRadius))
            .sensoryFeedback(.impact(weight: .light), trigger: configuration.isPressed) { _, pressed in pressed }
    }
}

extension ButtonStyle where Self == PrimaryButtonStyle {
    static var primary: PrimaryButtonStyle { PrimaryButtonStyle() }
}

extension ButtonStyle where Self == SecondaryButtonStyle {
    static var secondary: SecondaryButtonStyle { SecondaryButtonStyle() }
}
