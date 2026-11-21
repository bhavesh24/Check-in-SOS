import SwiftUI

/// A ring that empties as the acknowledgement deadline approaches, with the countdown text in the middle.
/// The countdown text keeps its own accessibility identifier so tests can read it.
struct CountdownRing: View {
    let text: String
    /// 1 when the screen opened, 0 at the deadline.
    let progress: Double
    let caption: String
    let testID: String

    @ScaledMetric(relativeTo: .largeTitle) private var diameter: CGFloat = 200

    var body: some View {
        let lineWidth = diameter * 0.07
        ZStack {
            Circle().stroke(Theme.statusCritical.opacity(0.16), lineWidth: lineWidth)
            Circle()
                .trim(from: 0, to: progress)
                .stroke(Theme.statusCritical, style: StrokeStyle(lineWidth: lineWidth, lineCap: .round))
                .rotationEffect(.degrees(-90))
                .animation(.smooth, value: progress)
            VStack(spacing: 2) {
                Text(text)
                    .font(.system(.largeTitle, design: .rounded).weight(.bold).monospacedDigit())
                    .foregroundStyle(Theme.statusCritical)
                    .multilineTextAlignment(.center)
                    .accessibilityIdentifier(testID)
                Text(caption)
                    .font(.subheadline)
                    .foregroundStyle(Theme.secondaryText)
                    .accessibilityHidden(true)
            }
            .padding(lineWidth * 1.5)
        }
        .frame(width: diameter, height: diameter)
        .frame(maxWidth: .infinity)
    }
}
