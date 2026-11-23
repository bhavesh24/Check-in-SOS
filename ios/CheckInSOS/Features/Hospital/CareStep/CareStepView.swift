import SwiftUI

/// In care / Recovering / Recovered / Bad condition / Patient alive.
struct CareStepView: View {
    @State var viewModel: CareStepViewModel

    var body: some View {
        FlowScreen(step: viewModel.step.stepNumber, totalSteps: viewModel.totalSteps,
                   errorMessage: viewModel.errorMessage) {
            MessageText(viewModel.step.heading)
                .accessibilityAddTraits(.isHeader)
        } actions: {
            ForEach(viewModel.step.answers, id: \.self) { answer in
                Button(answer.title) { Task { await viewModel.choose(answer) } }
                    .answerStyle(isPrimary: answer.title == "Yes")
                    .disabled(viewModel.isBusy)
                    .accessibilityIdentifier(answer.testID)
            }
        }
        .navigationTitle(viewModel.step.title)
        .withBackButton()
    }
}

/// "Yes" is the filled primary button, as on Escalate and Alive; other answers are outlined.
/// The styles are applied with `.buttonStyle` (not by calling `makeBody` directly) so they get the
/// environment: the flow's `roleTheme` and `isEnabled`.
private extension View {
    @ViewBuilder
    func answerStyle(isPrimary: Bool) -> some View {
        if isPrimary {
            buttonStyle(.primary)
        } else {
            buttonStyle(.secondary)
        }
    }
}
