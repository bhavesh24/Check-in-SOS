import SwiftUI

struct ScoreView: View {
    @State var viewModel: ScoreViewModel

    var body: some View {
        FlowScreen(step: viewModel.step, totalSteps: viewModel.totalSteps) {
            ScoreNumeral(score: viewModel.score)
                .accessibilityIdentifier("score.value")
                .padding(.vertical, 24)
            if viewModel.neighbourNotified {
                MessageText(viewModel.notificationText, testID: "score.neighbourNotified")
                    .multilineTextAlignment(.center)
                    .frame(maxWidth: .infinity)
            }
        } actions: {
            Button("Done") { viewModel.done() }
                .buttonStyle(.primary)
                .accessibilityIdentifier("score.done")
        }
        .navigationTitle("Score")
        .withBackButton()
    }
}
