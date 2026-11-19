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
                    TimelineRow(title: row.title, time: row.time, severity: row.severity)
                        .accessibilityIdentifier(row.testID)
                    if row.index < viewModel.rows.count - 1 {
                        Rectangle()
                            .fill(Theme.disabledBackground)
                            .frame(width: 2, height: 16)
                            .padding(.leading, 10)
                            .accessibilityHidden(true)
                    }
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
