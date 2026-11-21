import Domain
import SwiftUI

// SwiftUI previews live here so they stay out of coverage (`/Previews/` is excluded by the script).
#Preview("Home") {
    RootView(container: AppContainer.make(arguments: [AppContainer.uiTestingArgument]))
}

#Preview("Design system") {
    ScrollView {
        VStack(spacing: 16) {
            StepProgress(step: 2, total: 4)
            ScoreNumeral(score: 76)
            ScoreRing(score: 76, band: .good)
            RoleCard(title: "Neighbour", description: "Help someone living nearby", systemImage: "house.fill",
                     tint: Theme.color(for: .neighbour)) {}
            ChoiceGrid(items: ["Good", "Ill"]) { title in
                ChoiceTile(title: title, systemImage: "star.fill", isSelected: title == "Good") {}
            }
            PersonRow(name: "Margaret Wilson", subtitle: "Score 76", showsChevron: true)
            TimelineRow(title: "Score 38", time: "06:00", severity: .critical)
            Button("Primary") {}.buttonStyle(.primary)
            Button("Secondary") {}.buttonStyle(.secondary)
        }
        .padding()
    }
}
