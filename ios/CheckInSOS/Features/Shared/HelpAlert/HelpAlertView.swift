import SwiftUI

/// Neighbour "Help" alert and Hospital alert (IDs prefixed `alert.` / `hospitalAlert.`).
struct HelpAlertView: View {
    @State var viewModel: HelpAlertViewModel

    var body: some View {
        FlowScreen(step: viewModel.step, totalSteps: viewModel.totalSteps, errorMessage: viewModel.errorMessage) {
            MessageText(viewModel.message, testID: "\(viewModel.idPrefix).message")
            Text(viewModel.countdownText)
                .font(.system(.largeTitle, design: .rounded).weight(.bold).monospacedDigit())
                .foregroundStyle(Theme.statusCritical)
                .frame(maxWidth: .infinity)
                .accessibilityIdentifier("\(viewModel.idPrefix).countdown")
        } actions: {
            Button("Accept") { Task { await viewModel.accept() } }
                .buttonStyle(.primary)
                .disabled(viewModel.isBusy)
                .accessibilityIdentifier("\(viewModel.idPrefix).accept")
        }
        .navigationTitle("Help")
        .withBackButton()
        .task {
            while !Task.isCancelled {
                viewModel.tick()
                try? await Task.sleep(for: .seconds(1))
            }
        }
    }
}
