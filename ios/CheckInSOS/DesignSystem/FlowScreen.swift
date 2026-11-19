import SwiftUI

/// Standard screen layout: optional step progress and scrolling content, with the actions pinned at the bottom.
struct FlowScreen<Content: View, Actions: View>: View {
    var step: Int?
    var totalSteps: Int?
    var errorMessage: String?
    @ViewBuilder var content: Content
    @ViewBuilder var actions: Actions

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 24) {
                if let step, let totalSteps {
                    StepProgress(step: step, total: totalSteps)
                }
                content
                if let errorMessage {
                    ErrorBanner(message: errorMessage)
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(Theme.spacing)
        }
        .safeAreaInset(edge: .bottom) {
            if Actions.self != EmptyView.self {
                VStack(spacing: 12) { actions }
                    .padding(Theme.spacing)
                    .background(Theme.background)
                    .accessibilityElement(children: .contain)
                    .accessibilityIdentifier("actionBar")
            }
        }
        .background(Theme.background)
    }
}

/// Large body copy used for the help-flow messages and questions.
struct MessageText: View {
    let text: String
    let testID: String?

    init(_ text: String, testID: String? = nil) {
        self.text = text
        self.testID = testID
    }

    var body: some View {
        Text(text)
            .font(.title2.weight(.semibold))
            .foregroundStyle(Theme.text)
            .fixedSize(horizontal: false, vertical: true)
            .accessibilityIdentifier(testID ?? "")
    }
}
