import SwiftUI

/// A compact score badge: the number on a tint of its score band colour, or "No score".
/// Reads as "Score 76" / "No score yet".
struct ScorePill: View {
    let score: Int?

    var body: some View {
        HStack(spacing: 6) {
            Circle()
                .fill(score.map { Theme.color(for: ScoreBand(score: $0)) } ?? Theme.secondaryText)
                .frame(width: 8, height: 8)
            Text(score.map(String.init) ?? "No score")
                .font(.subheadline.weight(.semibold).monospacedDigit())
                .foregroundStyle(Theme.text)
        }
        .padding(.horizontal, 10)
        .padding(.vertical, 4)
        .background((score.map { Theme.color(for: ScoreBand(score: $0)) } ?? Theme.secondaryText).opacity(0.14),
                    in: Capsule())
        .accessibilityElement(children: .ignore)
        .accessibilityLabel(DisplayText.score(score))
    }
}
