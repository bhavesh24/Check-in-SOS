import Domain
import SwiftUI

struct AssessmentView: View {
    @State var viewModel: AssessmentViewModel

    var body: some View {
        FlowScreen(step: viewModel.step, totalSteps: viewModel.totalSteps, errorMessage: viewModel.errorMessage) {
            section("How do you feel physically?") {
                ChoiceGrid(items: PhysicalWellbeing.allCases) { option in
                    ChoiceTile(title: option.title, systemImage: option.systemImage,
                               isSelected: viewModel.physical == option) {
                        viewModel.select(option)
                    }
                    .accessibilityIdentifier(option.testID)
                }
            }
            section("How is your mood?") {
                ChoiceGrid(items: Mood.allCases) { option in
                    ChoiceTile(title: option.title, systemImage: option.systemImage,
                               isSelected: viewModel.mood == option) {
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

    private func section(_ heading: String, @ViewBuilder _ options: () -> some View) -> some View {
        VStack(alignment: .leading, spacing: 12) {
            Text(heading)
                .font(.title2.weight(.bold))
                .foregroundStyle(Theme.text)
                .fixedSize(horizontal: false, vertical: true)
                .accessibilityAddTraits(.isHeader)
            options()
        }
    }
}

private extension PhysicalWellbeing {
    var systemImage: String {
        switch self {
        case .fine: "hand.thumbsup.fill"
        case .good: "star.fill"
        case .ill: "thermometer.medium"
        case .verySick: "bed.double.fill"
        }
    }
}

private extension Mood {
    var systemImage: String {
        switch self {
        case .happy: "sun.max.fill"
        case .pleasant: "cloud.sun.fill"
        case .anxious: "cloud.bolt.rain.fill"
        case .depressed: "cloud.rain.fill"
        }
    }
}
