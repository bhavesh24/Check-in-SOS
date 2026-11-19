import SwiftUI

struct NeighboursView: View {
    @State var viewModel: NeighboursViewModel

    var body: some View {
        FlowScreen(errorMessage: viewModel.errorMessage) {
            VStack(spacing: 12) {
                ForEach(viewModel.neighbours) { adult in
                    PersonRow(name: adult.name, subtitle: viewModel.subtitle(for: adult))
                        .accessibilityIdentifier("neighbours.row.\(adult.id)")
                }
            }
        } actions: {
            Button("Simulate alert") { Task { await viewModel.simulate() } }
                .buttonStyle(.primary)
                .disabled(viewModel.isBusy)
                .accessibilityIdentifier("neighbours.simulateAlert")
        }
        .navigationTitle("Neighbours")
        .withBackButton()
        .task { await viewModel.load() }
    }
}
