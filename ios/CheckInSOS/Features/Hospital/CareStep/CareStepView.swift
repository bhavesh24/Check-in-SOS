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
                    .buttonStyle(AnswerButtonStyle(isPrimary: answer.title == "Yes"))
                    .disabled(viewModel.isBusy)
                    .accessibilityIdentifier(answer.testID)
            }
        }
        .navigationTitle("Help")
        .withBackButton()
    }
}

/// "Yes" is the filled primary button, as on Escalate and Alive; other answers are outlined.
private struct AnswerButtonStyle: ButtonStyle {
    let isPrimary: Bool

    @ViewBuilder
    func makeBody(configuration: Configuration) -> some View {
        if isPrimary {
            PrimaryButtonStyle().makeBody(configuration: configuration)
        } else {
            SecondaryButtonStyle().makeBody(configuration: configuration)
        }
    }
}
