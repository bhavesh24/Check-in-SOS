import SwiftUI

struct StatusView: View {
    @State var viewModel: StatusViewModel

    var body: some View {
        FlowScreen(errorMessage: viewModel.errorMessage) {
            if let adult = viewModel.adult {
                VStack(spacing: 4) {
                    ScoreNumeral(score: adult.latestScore)
                        .accessibilityIdentifier("status.score")
                    Text(viewModel.scoreCaption)
                        .font(.headline)
                        .foregroundStyle(Theme.secondaryText)
                }
                .frame(maxWidth: .infinity)
                .padding(.vertical, 16)

                HStack(spacing: 8) {
                    Text(viewModel.neighbourText)
                        .font(.title3.weight(.semibold))
                        .foregroundStyle(Theme.text)
                        .fixedSize(horizontal: false, vertical: true)
                        .accessibilityIdentifier("status.neighbour")
                    Spacer(minLength: 8)
                    Button(action: viewModel.editNeighbour) {
                        Image(systemName: "pencil")
                            .font(.title2)
                            .frame(width: Theme.minTouchTarget, height: Theme.minTouchTarget)
                    }
                    .foregroundStyle(Theme.primary)
                    .accessibilityLabel("Edit neighbour")
                    .accessibilityIdentifier("status.editNeighbour")
                }
                .padding(.horizontal, Theme.spacing)
                .padding(.vertical, 4)
                .background(Theme.surfaceTint, in: RoundedRectangle(cornerRadius: Theme.cornerRadius))
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
}
