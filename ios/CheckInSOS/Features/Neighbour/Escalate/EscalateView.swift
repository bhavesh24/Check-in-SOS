import SwiftUI

struct EscalateView: View {
    @State var viewModel: EscalateViewModel

    var body: some View {
        FlowScreen(step: viewModel.step, totalSteps: viewModel.totalSteps, errorMessage: viewModel.errorMessage) {
            MessageText(viewModel.heading)
                .accessibilityAddTraits(.isHeader)
        } actions: {
            Button("No, All OK!") { Task { await viewModel.allOk() } }
                .buttonStyle(.secondary)
                .disabled(viewModel.isBusy)
                .accessibilityIdentifier("escalate.allOk")
            Button("Yes, Alert emergency services") { Task { await viewModel.alertServices() } }
                .buttonStyle(.primary)
                .disabled(viewModel.isBusy)
                .accessibilityIdentifier("escalate.alertServices")
        }
        .navigationTitle("Help")
        .withBackButton()
        .confirmationDialog("Alert emergency services", isPresented: $viewModel.isEmergencySheetPresented,
                            titleVisibility: .visible) {
            ForEach(viewModel.emergencyNumbers, id: \.self) { number in
                Button(number) { viewModel.call(number) }
                    .accessibilityIdentifier("emergency.call\(number)")
            }
            Button("Cancel", role: .cancel) { viewModel.cancel() }
                .accessibilityIdentifier("emergency.cancel")
        } message: {
            Text("Do you want to call")
        }
    }
}
