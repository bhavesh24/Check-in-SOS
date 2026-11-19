import SwiftUI

struct NoCheckInView: View {
    @State var viewModel: NoCheckInViewModel

    var body: some View {
        FlowScreen {
            Image(systemName: "bell.badge.fill")
                .font(.system(size: 56))
                .foregroundStyle(Theme.statusWarning)
                .frame(maxWidth: .infinity)
                .accessibilityHidden(true)
            MessageText(viewModel.message, testID: "noCheckIn.message")
        } actions: {
            EmptyView()
        }
        .navigationTitle("No Check-in")
        .withBackButton()
    }
}
