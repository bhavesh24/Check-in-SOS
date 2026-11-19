import SwiftUI

struct HomeView: View {
    @State var viewModel: HomeViewModel

    var body: some View {
        FlowScreen {
            Text("Are you")
                .font(.title.weight(.bold))
                .foregroundStyle(Theme.text)
                .accessibilityAddTraits(.isHeader)
            VStack(spacing: 12) {
                ForEach(viewModel.options) { option in
                    Button(option.title) { viewModel.select(option.role) }
                        .buttonStyle(.primary)
                        .accessibilityIdentifier(option.id)
                }
            }
        } actions: {
            EmptyView()
        }
        .navigationTitle("Home")
    }
}
