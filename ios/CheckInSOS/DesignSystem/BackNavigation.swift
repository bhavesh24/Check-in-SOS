import SwiftUI
import UIKit

/// Replaces the system back button with one carrying the shared test ID `nav.back`.
struct BackButtonModifier: ViewModifier {
    @Environment(\.dismiss) private var dismiss

    func body(content: Content) -> some View {
        content
            .navigationBarBackButtonHidden(true)
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    Button(action: { dismiss() }) {
                        Label("Back", systemImage: "chevron.backward")
                            .labelStyle(.titleAndIcon)
                    }
                    .accessibilityIdentifier("nav.back")
                }
            }
    }
}

extension View {
    /// Back control on every screen except Home (docs/screens.md, global rules).
    func withBackButton() -> some View { modifier(BackButtonModifier()) }
}

/// Keeps the edge-swipe back gesture working although the system back button is hidden.
extension UINavigationController: @retroactive UIGestureRecognizerDelegate {
    override open func viewDidLoad() {
        super.viewDidLoad()
        interactivePopGestureRecognizer?.delegate = self
    }

    public func gestureRecognizerShouldBegin(_ gestureRecognizer: UIGestureRecognizer) -> Bool {
        viewControllers.count > 1
    }
}
