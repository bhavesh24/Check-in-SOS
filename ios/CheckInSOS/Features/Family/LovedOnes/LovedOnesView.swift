import SwiftUI

struct LovedOnesView: View {
    @State var viewModel: LovedOnesViewModel

    var body: some View {
        FlowScreen(errorMessage: viewModel.errorMessage) {
            GroupedList(items: viewModel.lovedOnes) { adult in
                Button { viewModel.select(adult) } label: {
                    PersonRow(name: adult.name, subtitle: viewModel.subtitle(for: adult), score: adult.latestScore,
                              showsChevron: true)
                }
                .buttonStyle(.plain)
                .accessibilityIdentifier("lovedOnes.row.\(adult.id)")
            }
        } actions: {
            EmptyView()
        }
        .navigationTitle("Your loved ones")
        .withBackButton()
        .task { await viewModel.load() }
    }
}
