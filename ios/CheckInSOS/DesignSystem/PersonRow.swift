import SwiftUI

/// A grouped-list row: initials avatar, name, optional subtitle, score pill and (when it navigates) a chevron.
struct PersonRow: View {
    let name: String
    var subtitle: String?
    let score: Int?
    var showsChevron = false

    var body: some View {
        HStack(spacing: 12) {
            InitialsAvatar(name: name)
            VStack(alignment: .leading, spacing: 2) {
                Text(name).font(.headline).foregroundStyle(Theme.text)
                    .fixedSize(horizontal: false, vertical: true)
                if let subtitle {
                    Text(subtitle).font(.subheadline).foregroundStyle(Theme.secondaryText)
                        .fixedSize(horizontal: false, vertical: true)
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            ScorePill(score: score)
            if showsChevron {
                Image(systemName: "chevron.forward")
                    .font(.subheadline.weight(.semibold))
                    .foregroundStyle(Theme.secondaryText)
                    .accessibilityHidden(true)
            }
        }
        .padding(.horizontal, Theme.spacing)
        .padding(.vertical, 12)
        .frame(maxWidth: .infinity, minHeight: Theme.primaryButtonHeight, alignment: .leading)
        .contentShape(Rectangle())
        .accessibilityElement(children: .combine)
    }
}
