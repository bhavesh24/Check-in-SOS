import SwiftUI
import UIKit

/// App entry point (excluded from coverage). `-ui-testing` → fresh mock data + fixed clock, no animations.
@main
struct CheckInSOSApp: App {
    private let container: AppContainer

    init() {
        let arguments = ProcessInfo.processInfo.arguments
        container = AppContainer.make(arguments: arguments)
        if arguments.contains(AppContainer.uiTestingArgument) {
            UIView.setAnimationsEnabled(false)
        }
    }

    var body: some Scene {
        WindowGroup {
            RootView(container: container)
        }
    }
}
