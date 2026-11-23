import SwiftUI

struct AliveView: View {
    @Environment(\.roleTheme) private var theme
    @State var viewModel: AliveViewModel

    var body: some View {
        FlowScreen(step: viewModel.step, totalSteps: viewModel.totalSteps, errorMessage: viewModel.errorMessage) {
            if let demo = viewModel.demoMessage {
                Label(demo, systemImage: "phone.down.fill")
                    .font(.headline)
                    .foregroundStyle(Theme.statusWarning)
                    .padding(Theme.spacing)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .background(theme.surfaceTint, in: RoundedRectangle(cornerRadius: Theme.cornerRadius))
                    .accessibilityElement(children: .combine)
                    .accessibilityIdentifier("emergency.demoMessage")
            }
            MessageText(viewModel.heading)
                .accessibilityAddTraits(.isHeader)
        } actions: {
            Button("No") { Task { await viewModel.answer(alive: false) } }
                .buttonStyle(.secondary)
                .disabled(viewModel.isBusy)
                .accessibilityIdentifier("alive.no")
            Button("Yes") { Task { await viewModel.answer(alive: true) } }
                .buttonStyle(.primary)
                .disabled(viewModel.isBusy)
                .accessibilityIdentifier("alive.yes")
        }
        .navigationTitle("Emergency")
        .withBackButton()
    }
}
