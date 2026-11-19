import SwiftUI

/// List row: name + subtitle on a tinted card. Shows a chevron when it navigates somewhere.
struct PersonRow: View {
    let name: String
    let subtitle: String
    var showsChevron = false

    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: "person.crop.circle.fill")
                .font(.title)
                .foregroundStyle(Theme.primary)
                .accessibilityHidden(true)
            VStack(alignment: .leading, spacing: 2) {
                Text(name).font(.headline).foregroundStyle(Theme.text)
                    .fixedSize(horizontal: false, vertical: true)
                Text(subtitle).font(.subheadline).foregroundStyle(Theme.secondaryText)
                    .fixedSize(horizontal: false, vertical: true)
            }
            Spacer(minLength: 8)
            if showsChevron {
                Image(systemName: "chevron.forward")
                    .foregroundStyle(Theme.secondaryText)
                    .accessibilityHidden(true)
            }
        }
        .padding(Theme.spacing)
        .frame(maxWidth: .infinity, minHeight: Theme.primaryButtonHeight, alignment: .leading)
        .background(Theme.surfaceTint, in: RoundedRectangle(cornerRadius: Theme.cornerRadius))
        .contentShape(RoundedRectangle(cornerRadius: Theme.cornerRadius))
        .accessibilityElement(children: .combine)
    }
}
