import SwiftUI

/// Rows on one rounded panel with dividers between them, like an iOS inset grouped list. Keeps the
/// screen's pinned action bar, which a `List` would not.
struct GroupedList<Item: Identifiable, Row: View>: View {
    @Environment(\.roleTheme) private var theme
    let items: [Item]
    @ViewBuilder var row: (Item) -> Row

    var body: some View {
        VStack(spacing: 0) {
            ForEach(items) { item in
                row(item)
                if item.id != items.last?.id {
                    Divider().padding(.leading, 72)
                }
            }
        }
        .background(theme.surfaceTint, in: RoundedRectangle(cornerRadius: Theme.cornerRadius + 4))
        .clipShape(RoundedRectangle(cornerRadius: Theme.cornerRadius + 4))
    }
}
