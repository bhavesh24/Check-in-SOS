import SwiftUI

/// "Step N of M" with a segmented bar (test ID `stepProgress`).
struct StepProgress: View {
    let step: Int
    let total: Int

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(Self.text(step: step, total: total))
                .font(.subheadline.weight(.semibold))
                .foregroundStyle(Theme.secondaryText)
            HStack(spacing: 6) {
                ForEach(1...max(total, 1), id: \.self) { index in
                    Capsule()
                        .fill(index <= step ? Theme.primary : Theme.disabledBackground)
                        .frame(height: 6)
                }
            }
        }
        .accessibilityElement(children: .ignore)
        .accessibilityLabel(Self.text(step: step, total: total))
        .accessibilityIdentifier("stepProgress")
    }

    static func text(step: Int, total: Int) -> String { "Step \(step) of \(total)" }
}
