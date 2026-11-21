import SwiftUI

/// The check-in result: a circular gauge filled to score/100 in the score band's colour, with the
/// number in the middle.
struct ScoreRing: View {
    let score: Int
    let band: ScoreBand

    @ScaledMetric(relativeTo: .largeTitle) private var diameter: CGFloat = 200
    @ScaledMetric(relativeTo: .largeTitle) private var numberSize: CGFloat = 64
    @Environment(\.accessibilityReduceMotion) private var reduceMotion
    @State private var progress: Double = 0

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
                    .font(.headline)
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
