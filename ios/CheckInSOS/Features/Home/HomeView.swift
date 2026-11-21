import Domain
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
                    RoleCard(title: option.title, description: option.description,
                             systemImage: option.role.systemImage, tint: Theme.color(for: option.role)) {
                        viewModel.select(option.role)
                    }
                    .accessibilityIdentifier(option.id)
                }
            }
        } actions: {
            EmptyView()
        }
        .navigationTitle("Home")
    }
}

private extension Role {
    var systemImage: String {
        switch self {
        case .olderAdult: "person.fill.checkmark"
        case .family: "person.2.fill"
        case .hospital: "cross.case.fill"
        case .neighbour: "house.fill"
        }
    }
}
