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
            ForEach(Array(viewModel.step.answers.enumerated()), id: \.element) { index, answer in
                Button(answer.title) { Task { await viewModel.choose(answer) } }
                    .buttonStyle(AnswerButtonStyle(isLast: index == viewModel.step.answers.count - 1))
                    .disabled(viewModel.isBusy)
                    .accessibilityIdentifier(answer.testID)
            }
        }
        .navigationTitle("Help")
        .withBackButton()
    }
}

/// The last answer is the filled primary button; earlier ones are outlined (like No / Yes on the prototype).
private struct AnswerButtonStyle: ButtonStyle {
    let isLast: Bool

    @ViewBuilder
    func makeBody(configuration: Configuration) -> some View {
        if isLast {
            PrimaryButtonStyle().makeBody(configuration: configuration)
        } else {
            SecondaryButtonStyle().makeBody(configuration: configuration)
        }
    }
}
