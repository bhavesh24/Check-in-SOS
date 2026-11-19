import Domain
import SwiftUI

struct AssessmentView: View {
    @State var viewModel: AssessmentViewModel

    var body: some View {
        FlowScreen(step: viewModel.step, totalSteps: viewModel.totalSteps, errorMessage: viewModel.errorMessage) {
            section {
                ForEach(PhysicalWellbeing.allCases, id: \.self) { option in
                    ChoiceChip(title: option.title, isSelected: viewModel.physical == option) {
                        viewModel.select(option)
                    }
                    .accessibilityIdentifier(option.testID)
                }
            }
            section {
                ForEach(Mood.allCases, id: \.self) { option in
                    ChoiceChip(title: option.title, isSelected: viewModel.mood == option) {
                        viewModel.select(option)
                    }
                    .accessibilityIdentifier(option.testID)
                }
            }
            Button("Demo: missed check-in") { viewModel.showMissedCheckIn() }
                .font(.body.weight(.semibold))
                .foregroundStyle(Theme.primary)
                .frame(minHeight: Theme.minTouchTarget)
                .accessibilityIdentifier("assessment.demoNoCheckIn")
        } actions: {
            Button("Submit") { Task { await viewModel.submit() } }
                .buttonStyle(.primary)
                .disabled(!viewModel.canSubmit)
                .accessibilityIdentifier("assessment.submit")
        }
        .navigationTitle("Assessment")
        .withBackButton()
    }

    private func section(@ViewBuilder _ options: () -> some View) -> some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Are you")
                .font(.title2.weight(.bold))
                .foregroundStyle(Theme.text)
                .accessibilityAddTraits(.isHeader)
            options()
        }
    }
}
