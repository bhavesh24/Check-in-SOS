import Domain
import SwiftUI
import UIKit

/// Design tokens from docs/screens.md. The teal is darkened so white text passes WCAG AA (5.5:1).
enum Theme {
    static let background = Color(light: 0xFFFFFF, dark: 0x0B1214)
    static let text = Color(light: 0x101828, dark: 0xF2F4F7)
    static let secondaryText = Color(light: 0x475467, dark: 0xD0D5DD)
    static let disabledBackground = Color(light: 0xE4E7EC, dark: 0x344054)
    static let disabledText = Color(light: 0x475467, dark: 0xD0D5DD)
    static let statusCritical = Color(light: 0xB42318, dark: 0xF97066)
    static let statusWarning = Color(light: 0xB54708, dark: 0xFDB022)
    static let statusOk = Color(light: 0x067647, dark: 0x47CD89)
    static let onRole = Color(light: 0xFFFFFF, dark: 0x0B1214)
    static let cardBorder = Color(light: 0xE4E7EC, dark: 0x344054)

    /// Minimum touch target (pt).
    static let minTouchTarget: CGFloat = 48
    /// Primary buttons are full width and at least this tall (pt).
    static let primaryButtonHeight: CGFloat = 56
    static let cornerRadius: CGFloat = 14
    static let spacing: CGFloat = 16

    /// Icon badge colour for each role on Home. `onRole` passes 4.5:1 on all of them.
    static func color(for role: Role) -> Color { RoleTheme.forRole(role).primary }

    /// Score ring colour: green when good, then reds that deepen as the score drops. All ≥3:1 on `background`.
    static func color(for band: ScoreBand) -> Color {
        switch band {
        case .good: statusOk
        case .mild: Color(light: 0xF04438, dark: 0xFDA29B)
        case .moderate: Color(light: 0xD92D20, dark: 0xF97066)
        case .severe: Color(light: 0x912018, dark: 0xF04438)
        }
    }

    static func color(for severity: TimelineSeverity) -> Color {
        switch severity {
        case .critical: statusCritical
        case .warning: statusWarning
        case .ok: statusOk
        }
    }
}

/// A light/dark hex pair, kept raw so tests can check contrast.
struct HexPair: Equatable, Sendable {
    let light: UInt32
    let dark: UInt32

    var color: Color { Color(light: light, dark: dark) }
}

/// The accent colours of one flow: every screen after a Home card is themed in that card's role colour
/// (docs/screens.md, "Flow themes"). Components read it with `@Environment(\.roleTheme)`.
struct RoleTheme: Equatable, Sendable {
    let primaryHex: HexPair
    let onPrimaryHex: HexPair
    let surfaceTintHex: HexPair

    var primary: Color { primaryHex.color }
    var onPrimary: Color { onPrimaryHex.color }
    var surfaceTint: Color { surfaceTintHex.color }

    static func forRole(_ role: Role) -> RoleTheme {
        switch role {
        case .olderAdult:
            RoleTheme(primaryHex: HexPair(light: 0x0F766E, dark: 0x2DD4BF),
                      onPrimaryHex: HexPair(light: 0xFFFFFF, dark: 0x042F2E),
                      surfaceTintHex: HexPair(light: 0xF0FDFA, dark: 0x132326))
        case .family:
            RoleTheme(primaryHex: HexPair(light: 0x6941C6, dark: 0xB692F6),
                      onPrimaryHex: onRole, surfaceTintHex: HexPair(light: 0xF4F3FF, dark: 0x1C1830))
        case .hospital:
            RoleTheme(primaryHex: HexPair(light: 0x175CD3, dark: 0x84ADFF),
                      onPrimaryHex: onRole, surfaceTintHex: HexPair(light: 0xEFF8FF, dark: 0x111C2E))
        case .neighbour:
            RoleTheme(primaryHex: HexPair(light: 0xB93815, dark: 0xF38744),
                      onPrimaryHex: onRole, surfaceTintHex: HexPair(light: 0xFEF6EE, dark: 0x2A1A12))
        }
    }

    private static let onRole = HexPair(light: 0xFFFFFF, dark: 0x0B1214)
}

extension EnvironmentValues {
    /// Home and the older-adult flow use the teal default.
    @Entry var roleTheme: RoleTheme = .forRole(.olderAdult)
}

extension View {
    /// Themes this screen in `role`'s Home card colour (buttons, tiles, avatars, back button tint…).
    func roleTheme(_ role: Role) -> some View {
        let theme = RoleTheme.forRole(role)
        return environment(\.roleTheme, theme).tint(theme.primary)
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
