import Domain
import SwiftUI
import UIKit

/// Design tokens from docs/screens.md. The teal is darkened so white text passes WCAG AA (5.5:1).
enum Theme {
    static let primary = Color(light: 0x0F766E, dark: 0x2DD4BF)
    static let onPrimary = Color(light: 0xFFFFFF, dark: 0x042F2E)
    static let background = Color(light: 0xFFFFFF, dark: 0x0B1214)
    static let surfaceTint = Color(light: 0xF0FDFA, dark: 0x132326)
    static let text = Color(light: 0x101828, dark: 0xF2F4F7)
    static let secondaryText = Color(light: 0x475467, dark: 0xD0D5DD)
    static let disabledBackground = Color(light: 0xE4E7EC, dark: 0x344054)
    static let disabledText = Color(light: 0x475467, dark: 0xD0D5DD)
    static let statusCritical = Color(light: 0xB42318, dark: 0xF97066)
    static let statusWarning = Color(light: 0xB54708, dark: 0xFDB022)
    static let statusOk = Color(light: 0x067647, dark: 0x47CD89)

    /// Minimum touch target (pt).
    static let minTouchTarget: CGFloat = 48
    /// Primary buttons are full width and at least this tall (pt).
    static let primaryButtonHeight: CGFloat = 56
    static let cornerRadius: CGFloat = 14
    static let spacing: CGFloat = 16

    static func color(for severity: TimelineSeverity) -> Color {
        switch severity {
        case .critical: statusCritical
        case .warning: statusWarning
        case .ok: statusOk
        }
    }
}

extension Color {
    /// A colour that follows the system light/dark appearance.
    init(light: UInt32, dark: UInt32) {
        self.init(uiColor: UIColor { traits in
            UIColor(rgb: traits.userInterfaceStyle == .dark ? dark : light)
        })
    }
}

extension UIColor {
    convenience init(rgb: UInt32) {
        self.init(red: CGFloat((rgb >> 16) & 0xFF) / 255, green: CGFloat((rgb >> 8) & 0xFF) / 255,
                  blue: CGFloat(rgb & 0xFF) / 255, alpha: 1)
    }
}
