import SwiftUI

/// Neighbour "Help" alert and Hospital alert (IDs prefixed `alert.` / `hospitalAlert.`).
struct HelpAlertView: View {
    @State var viewModel: HelpAlertViewModel

    var body: some View {
        FlowScreen(step: viewModel.step, totalSteps: viewModel.totalSteps, errorMessage: viewModel.errorMessage) {
            UrgentBanner(text: viewModel.bannerText)
            MessageText(viewModel.message, testID: "\(viewModel.idPrefix).message")
            CountdownRing(text: viewModel.countdownText, progress: viewModel.countdownProgress,
                          caption: viewModel.countdownCaption, testID: "\(viewModel.idPrefix).countdown")
        } actions: {
            Button("Accept") { Task { await viewModel.accept() } }
                .buttonStyle(.primary)
                .disabled(viewModel.isBusy)
                .accessibilityIdentifier("\(viewModel.idPrefix).accept")
        }
        .navigationTitle(viewModel.title)
        .withBackButton()
        .task {
            while !Task.isCancelled {
                viewModel.tick()
                try? await Task.sleep(for: .seconds(1))
            }
        }
    }
}
