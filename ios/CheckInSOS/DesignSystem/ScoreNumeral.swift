import SwiftUI

/// Big score rendered like the prototype: "76/" over "100" ("—" when there is no score yet).
struct ScoreNumeral: View {
    @Environment(\.roleTheme) private var theme
    let score: Int?
    @ScaledMetric(relativeTo: .largeTitle) private var size: CGFloat = 72

    var body: some View {
        VStack(spacing: 0) {
            Text(score.map { "\($0)/" } ?? "—")
                .font(.system(size: size, weight: .bold, design: .rounded))
            if score != nil {
                Text("100").font(.system(size: size / 2, weight: .semibold, design: .rounded))
            }
        }
        .foregroundStyle(theme.primary)
        .frame(maxWidth: .infinity)
        .accessibilityElement(children: .ignore)
        .accessibilityLabel(Self.accessibilityLabel(for: score))
    }

    static func accessibilityLabel(for score: Int?) -> String {
        score.map { "Score \($0) out of 100" } ?? "No score yet"
    }
}
