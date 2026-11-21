import SwiftUI

/// The check-in result: a circular gauge filled to score/100 in the score band's colour, with the
/// number in the middle.
struct ScoreRing: View {
    let score: Int
    let band: ScoreBand

    @ScaledMetric private var diameter: CGFloat
    @ScaledMetric private var numberSize: CGFloat
    @Environment(\.accessibilityReduceMotion) private var reduceMotion
    @State private var progress: Double = 0

    /// - Parameter size: diameter at the default text size; it grows with Dynamic Type.
    init(score: Int, band: ScoreBand, size: CGFloat = 200) {
        self.score = score
        self.band = band
        _diameter = ScaledMetric(wrappedValue: size, relativeTo: .largeTitle)
        _numberSize = ScaledMetric(wrappedValue: size * 0.32, relativeTo: .largeTitle)
    }

    var body: some View {
        let lineWidth = diameter * 0.09
        ZStack {
            Circle()
                .stroke(Theme.color(for: band).opacity(0.18), lineWidth: lineWidth)
            Circle()
                .trim(from: 0, to: progress)
                .stroke(Theme.color(for: band), style: StrokeStyle(lineWidth: lineWidth, lineCap: .round))
                .rotationEffect(.degrees(-90))
            VStack(spacing: 0) {
                Text("\(score)")
                    .font(.system(size: numberSize, weight: .bold, design: .rounded))
                    .monospacedDigit()
                Text("out of 100")
                    .font(.subheadline.weight(.semibold))
                    .multilineTextAlignment(.center)
                    .fixedSize(horizontal: false, vertical: true)
                    .foregroundStyle(Theme.secondaryText)
            }
            .foregroundStyle(Theme.text)
            .padding(lineWidth * 1.5)
        }
        .frame(width: diameter, height: diameter)
        .frame(maxWidth: .infinity)
        .accessibilityElement(children: .ignore)
        .accessibilityLabel(ScoreNumeral.accessibilityLabel(for: score))
        .onAppear {
            let target = Double(min(max(score, 0), 100)) / 100
            if reduceMotion {
                progress = target
            } else {
                withAnimation(.smooth(duration: 1).delay(0.15)) { progress = target }
            }
        }
    }
}
