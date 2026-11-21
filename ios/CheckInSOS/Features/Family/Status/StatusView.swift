import SwiftUI

struct StatusView: View {
    @State var viewModel: StatusViewModel

    var body: some View {
        FlowScreen(errorMessage: viewModel.errorMessage) {
            if let adult = viewModel.adult {
                VStack(spacing: 8) {
                    Text(viewModel.scoreCaption)
                        .font(.headline)
                        .foregroundStyle(Theme.secondaryText)
                    if let score = adult.latestScore, let band = viewModel.band {
                        ScoreRing(score: score, band: band, size: 160)
                            .accessibilityIdentifier("status.score")
                    } else {
                        ScoreNumeral(score: nil)
                            .accessibilityIdentifier("status.score")
                    }
                    Label(viewModel.lastCheckInText, systemImage: "clock")
                        .font(.subheadline)
                        .foregroundStyle(Theme.secondaryText)
                        .fixedSize(horizontal: false, vertical: true)
                        .accessibilityElement(children: .combine)
                        .accessibilityIdentifier("status.lastCheckIn")
                }
                .frame(maxWidth: .infinity)

                section(viewModel.neighbourCaption) {
                    HStack(spacing: 12) {
                        HStack(spacing: 12) {
                            InitialsAvatar(name: viewModel.neighbourName)
                            Text(viewModel.neighbourText)
                                .font(.headline)
                                .foregroundStyle(Theme.text)
                                .fixedSize(horizontal: false, vertical: true)
                                .frame(maxWidth: .infinity, alignment: .leading)
                        }
                        .accessibilityElement(children: .combine)
                        .accessibilityIdentifier("status.neighbour")
                        Button(action: viewModel.editNeighbour) {
                            Label("Edit neighbour", systemImage: "pencil")
                                .labelStyle(.iconOnly)
                                .font(.title3)
                                .frame(width: Theme.minTouchTarget, height: Theme.minTouchTarget)
                                .contentShape(Rectangle())
                        }
                        .foregroundStyle(Theme.primary)
                        .accessibilityIdentifier("status.editNeighbour")
                    }
                    .padding(.leading, Theme.spacing)
                    .padding(.trailing, 4)
                    .padding(.vertical, 8)
                    .background(Theme.surfaceTint, in: RoundedRectangle(cornerRadius: Theme.cornerRadius + 4))
                }

                if viewModel.showsTrend {
                    section(viewModel.trendTitle) {
                        ScoreTrendChart(records: viewModel.history, accessibilityText: viewModel.trendAccessibilityText)
                            .padding(Theme.spacing)
                            .background(Theme.surfaceTint, in: RoundedRectangle(cornerRadius: Theme.cornerRadius + 4))
                            .accessibilityIdentifier("status.trend")
                    }
                }
            }
        } actions: {
            Button("Timeline", action: viewModel.openTimeline)
                .buttonStyle(.secondary)
                .accessibilityIdentifier("status.timeline")
        }
        .navigationTitle(viewModel.title)
        .withBackButton()
        .task { await viewModel.load() }
    }

    private func section(_ title: String, @ViewBuilder content: () -> some View) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(title)
                .font(.headline)
                .foregroundStyle(Theme.text)
                .accessibilityAddTraits(.isHeader)
            content()
        }
    }
}
