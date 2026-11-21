import Domain
import SwiftUI

/// One timeline event: an icon badge in the event's colour, the copy and the HH:mm time. A line joins the
/// badges of neighbouring rows. At accessibility text sizes the time moves under the copy.
struct TimelineRow: View {
    let title: String
    let time: String
    let kind: TimelineEventKind
    var isFirst = false
    var isLast = false
    @Environment(\.dynamicTypeSize) private var dynamicTypeSize
    @ScaledMetric(relativeTo: .body) private var badgeSize: CGFloat = 36

    /// Grows a little with Dynamic Type, but is capped so the copy keeps the room at the largest sizes.
    private var badge: CGFloat { min(badgeSize, 44) }

    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: kind.systemImage)
                .font(.subheadline.weight(.semibold))
                .dynamicTypeSize(...DynamicTypeSize.xxLarge)
                .foregroundStyle(Theme.onRole)
                .frame(width: badge, height: badge)
                .background(Theme.color(for: kind.severity), in: Circle())
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
        .padding(.vertical, 10)
        .frame(minHeight: Theme.minTouchTarget)
        .background(alignment: .leading) {
            VStack(spacing: 0) {
                Rectangle().fill(isFirst ? .clear : Theme.cardBorder)
                Rectangle().fill(isLast ? .clear : Theme.cardBorder)
            }
            .frame(width: 2)
            .padding(.leading, badge / 2 - 1)
            .accessibilityHidden(true)
        }
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

private extension TimelineEventKind {
    var systemImage: String {
        switch self {
        case .score: "heart.text.square.fill"
        case .missedCheckIn: "clock.badge.exclamationmark"
        case .notificationSent: "bell.fill"
        case .acknowledged: "hand.raised.fill"
        case .neighbourReached: "figure.walk"
        case .escalated: "exclamationmark.triangle.fill"
        case .careOutcome(let outcome):
            switch outcome {
            case .inCare: "cross.case.fill"
            case .recovering: "arrow.up.heart.fill"
            case .allOk, .recovered, .alive: "checkmark.seal.fill"
            case .badCondition: "exclamationmark.triangle.fill"
            case .deceased: "heart.slash.fill"
            }
        }
    }
}
