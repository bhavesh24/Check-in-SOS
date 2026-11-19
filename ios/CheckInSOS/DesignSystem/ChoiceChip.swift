import SwiftUI

/// A single-choice answer (Fine / Good / …). Selected chips are filled and expose the "Selected" trait.
struct ChoiceChip: View {
    let title: String
    let isSelected: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            HStack {
                Text(title).font(.title3.weight(.semibold))
                    .fixedSize(horizontal: false, vertical: true)
                Spacer(minLength: 8)
                Image(systemName: isSelected ? "checkmark.circle.fill" : "circle")
                    .accessibilityHidden(true)
            }
            .padding(.horizontal, Theme.spacing)
            .padding(.vertical, 12)
            .frame(maxWidth: .infinity, minHeight: Theme.primaryButtonHeight)
            .foregroundStyle(isSelected ? Theme.onPrimary : Theme.primary)
            .background(isSelected ? Theme.primary : Theme.surfaceTint,
                        in: RoundedRectangle(cornerRadius: Theme.cornerRadius))
            .overlay(RoundedRectangle(cornerRadius: Theme.cornerRadius).stroke(Theme.primary, lineWidth: 2))
            .contentShape(RoundedRectangle(cornerRadius: Theme.cornerRadius))
        }
        .buttonStyle(.plain)
        .accessibilityAddTraits(isSelected ? .isSelected : [])
    }
}
