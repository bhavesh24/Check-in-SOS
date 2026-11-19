import Domain
import SwiftUI

/// One timeline event: coloured ring, copy and HH:mm time. At accessibility text sizes the time moves
/// under the copy so nothing is truncated.
struct TimelineRow: View {
    let title: String
    let time: String
    let severity: TimelineSeverity
    @Environment(\.dynamicTypeSize) private var dynamicTypeSize

    var body: some View {
        HStack(alignment: .firstTextBaseline, spacing: 12) {
            Circle()
                .stroke(Theme.color(for: severity), lineWidth: 4)
                .frame(width: 22, height: 22)
                .alignmentGuide(.firstTextBaseline) { $0[VerticalAlignment.center] + 6 }
                .accessibilityHidden(true)
            AdaptiveStack(vertical: dynamicTypeSize.isAccessibilitySize) {
                Text(title)
                    .font(.body.weight(.semibold))
                    .foregroundStyle(Theme.text)
                    .fixedSize(horizontal: false, vertical: true)
                    .frame(maxWidth: .infinity, alignment: .leading)
                Text(time)
                    .font(.subheadline.monospacedDigit())
                    .foregroundStyle(Theme.secondaryText)
                    .fixedSize()
            }
        }
        .padding(.vertical, 12)
        .frame(minHeight: Theme.minTouchTarget)
        .accessibilityElement(children: .combine)
    }
}

/// HStack normally; VStack (leading) when `vertical` — used where big Dynamic Type would squeeze a row.
struct AdaptiveStack<Content: View>: View {
    let vertical: Bool
    @ViewBuilder var content: Content

    var body: some View {
        let layout = vertical ? AnyLayout(VStackLayout(alignment: .leading, spacing: 4))
                              : AnyLayout(HStackLayout(spacing: 8))
        layout { content }
    }
}
