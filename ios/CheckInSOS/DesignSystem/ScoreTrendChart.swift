import Charts
import Domain
import SwiftUI

/// The last few check-in scores as a line on a 0–100 axis, with a dashed line at the good threshold (70).
/// Each point takes its score band colour.
struct ScoreTrendChart: View {
    @Environment(\.roleTheme) private var theme
    let records: [ScoreRecord]
    let accessibilityText: String
    @ScaledMetric(relativeTo: .body) private var height: CGFloat = 160

    var body: some View {
        Chart {
            RuleMark(y: .value("Good", ScoreCalculator.goodThreshold))
                .foregroundStyle(Theme.secondaryText.opacity(0.6))
                .lineStyle(StrokeStyle(lineWidth: 1, dash: [4, 4]))
            ForEach(Array(records.enumerated()), id: \.offset) { index, record in
                LineMark(x: .value("Check-in", index), y: .value("Score", record.score))
                    .foregroundStyle(theme.primary)
                    .interpolationMethod(.monotone)
                PointMark(x: .value("Check-in", index), y: .value("Score", record.score))
                    .foregroundStyle(Theme.color(for: ScoreBand(score: record.score)))
                    .symbolSize(60)
            }
        }
        .chartYScale(domain: 0...100)
        .chartYAxis {
            AxisMarks(position: .leading, values: [0, 50, 100])
        }
        .chartXAxis(.hidden)
        .frame(height: height)
        .accessibilityElement(children: .ignore)
        .accessibilityLabel(accessibilityText)
    }
}
