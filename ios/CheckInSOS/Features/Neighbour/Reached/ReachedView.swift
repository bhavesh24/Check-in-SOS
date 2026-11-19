import SwiftUI

struct ReachedView: View {
    @State var viewModel: ReachedViewModel

    var body: some View {
        FlowScreen(step: viewModel.step, totalSteps: viewModel.totalSteps, errorMessage: viewModel.errorMessage) {
            MessageText(viewModel.message, testID: "reached.message")
        } actions: {
            Button("I have reached") { Task { await viewModel.confirm() } }
                .buttonStyle(.primary)
                .disabled(viewModel.isBusy)
                .accessibilityIdentifier("reached.confirm")
        }
        .navigationTitle("Help")
        .withBackButton()
    }
}
