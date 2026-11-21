import SwiftUI

/// Full-width filled teal button, at least 56 pt high.
struct PrimaryButtonStyle: ButtonStyle {
    @Environment(\.isEnabled) private var isEnabled

    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .font(.headline)
            .multilineTextAlignment(.center)
            .padding(.horizontal, Theme.spacing)
            .padding(.vertical, 12)
            .frame(maxWidth: .infinity, minHeight: Theme.primaryButtonHeight)
            .foregroundStyle(isEnabled ? Theme.onPrimary : Theme.disabledText)
            .background(isEnabled ? Theme.primary : Theme.disabledBackground,
                        in: RoundedRectangle(cornerRadius: Theme.cornerRadius))
            .opacity(configuration.isPressed ? 0.85 : 1)
            .contentShape(RoundedRectangle(cornerRadius: Theme.cornerRadius))
            .sensoryFeedback(.impact(weight: .light), trigger: configuration.isPressed) { _, pressed in pressed }
    }
}

/// Full-width outlined button for the secondary choice.
struct SecondaryButtonStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .font(.headline)
            .multilineTextAlignment(.center)
            .padding(.horizontal, Theme.spacing)
            .padding(.vertical, 12)
            .frame(maxWidth: .infinity, minHeight: Theme.primaryButtonHeight)
            .foregroundStyle(Theme.primary)
            .background(Theme.surfaceTint, in: RoundedRectangle(cornerRadius: Theme.cornerRadius))
            .overlay(RoundedRectangle(cornerRadius: Theme.cornerRadius).stroke(Theme.primary, lineWidth: 2))
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
