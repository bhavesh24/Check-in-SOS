import SwiftUI

/// A single-choice answer tile (Fine / Good / …): an icon above the label. Selected tiles are filled and
/// expose the "Selected" trait.
struct ChoiceTile: View {
    @Environment(\.roleTheme) private var theme
    let title: String
    let systemImage: String
    let isSelected: Bool
    let action: () -> Void

    @ScaledMetric(relativeTo: .title) private var iconSize: CGFloat = 32

    var body: some View {
        Button(action: action) {
            VStack(spacing: 8) {
                Image(systemName: systemImage)
                    .font(.system(size: iconSize))
                    .frame(height: iconSize * 1.2)
                    .accessibilityHidden(true)
                Text(title)
                    .font(.title3.weight(.semibold))
                    .multilineTextAlignment(.center)
                    .fixedSize(horizontal: false, vertical: true)
            }
            .padding(.horizontal, 12)
            .padding(.vertical, Theme.spacing)
            .frame(maxWidth: .infinity, minHeight: 112, maxHeight: .infinity)
            .foregroundStyle(isSelected ? theme.onPrimary : theme.primary)
            .background(isSelected ? theme.primary : theme.surfaceTint,
                        in: RoundedRectangle(cornerRadius: Theme.cornerRadius))
            .overlay(RoundedRectangle(cornerRadius: Theme.cornerRadius).stroke(theme.primary, lineWidth: 2))
            .overlay(alignment: .topTrailing) {
                if isSelected {
                    Image(systemName: "checkmark.circle.fill")
                        .font(.title3)
                        .padding(8)
                        .accessibilityHidden(true)
                        .transition(.scale.combined(with: .opacity))
                }
            }
            .contentShape(RoundedRectangle(cornerRadius: Theme.cornerRadius))
        }
        .buttonStyle(.plain)
        .animation(.snappy, value: isSelected)
        .sensoryFeedback(.selection, trigger: isSelected) { _, selected in selected }
        .accessibilityAddTraits(isSelected ? .isSelected : [])
    }
}

/// Lays answer tiles out two to a row with equal heights, or one per row at accessibility text sizes.
struct ChoiceGrid<Item: Hashable, Tile: View>: View {
    let items: [Item]
    @ViewBuilder var tile: (Item) -> Tile
    @Environment(\.dynamicTypeSize) private var dynamicTypeSize

    var body: some View {
        let columns = dynamicTypeSize.isAccessibilitySize ? 1 : 2
        let rows = stride(from: 0, to: items.count, by: columns).map { Array(items[$0..<min($0 + columns, items.count)]) }
        Grid(horizontalSpacing: 12, verticalSpacing: 12) {
            ForEach(rows, id: \.self) { row in
                GridRow {
                    ForEach(row, id: \.self) { item in
                        tile(item)
                    }
                }
            }
        }
    }
}
