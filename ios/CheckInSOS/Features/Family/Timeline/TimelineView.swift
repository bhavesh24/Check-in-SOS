import SwiftUI

struct TimelineView: View {
    @State var viewModel: TimelineViewModel

    var body: some View {
        FlowScreen(errorMessage: viewModel.errorMessage) {
            if viewModel.isEmpty {
                MessageText(viewModel.emptyText, testID: "timeline.empty")
            }
            VStack(alignment: .leading, spacing: 0) {
                ForEach(viewModel.rows) { row in
                    TimelineRow(title: row.title, time: row.time, kind: row.kind,
                                isFirst: row.index == 0, isLast: row.index == viewModel.rows.count - 1)
                        .accessibilityIdentifier(row.testID)
                }
            }
        } actions: {
            EmptyView()
        }
        .navigationTitle("Timeline")
        .withBackButton()
        .task { await viewModel.load() }
    }
}
