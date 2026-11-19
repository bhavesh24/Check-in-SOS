import SwiftUI

struct PatientsView: View {
    @State var viewModel: PatientsViewModel

    var body: some View {
        FlowScreen(errorMessage: viewModel.errorMessage) {
            VStack(spacing: 12) {
                ForEach(viewModel.patients) { patient in
                    PersonRow(name: patient.name, subtitle: viewModel.subtitle(for: patient))
                        .accessibilityIdentifier("patients.row.\(patient.id)")
                }
            }
        } actions: {
            Button("Simulate alert") { Task { await viewModel.simulate() } }
                .buttonStyle(.primary)
                .disabled(viewModel.isBusy)
                .accessibilityIdentifier("patients.simulateAlert")
        }
        .navigationTitle("Patients")
        .withBackButton()
        .task { await viewModel.load() }
    }
}
