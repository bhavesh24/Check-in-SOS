import Domain
import Foundation
import Testing
@testable import CheckInSOS

/// Flow themes (docs/screens.md, "Flow themes"): each screen takes its Home card's role colour.
@Suite struct RoleThemeTests {
    private let alert = AlertSummary(id: "a-1", subjectName: "Arthur Patel", recipient: .neighbour,
                                     acknowledgeBy: Date(timeIntervalSince1970: 0))

    @Test func routesMapToTheirFlowRole() {
        #expect([Route.assessment, .score(score: 76, neighbourNotified: false), .noCheckIn].map(\.role)
            == Array(repeating: .olderAdult, count: 3))
        #expect([Route.lovedOnes, .status(olderAdultId: "oa-1"), .editNeighbour(olderAdultId: "oa-1"),
                  .timeline(olderAdultId: "oa-1")].map(\.role) == Array(repeating: .family, count: 4))
        #expect([Route.neighbours, .neighbourAlert(alert), .reached(alert), .escalate(alert),
                  .alive(alert, calledNumber: nil)].map(\.role) == Array(repeating: .neighbour, count: 5))
        #expect([Route.patients, .hospitalAlert(alert), .careStep(.inCare, alert)].map(\.role)
            == Array(repeating: .hospital, count: 3))
    }

    @Test func homeBadgeUsesTheFlowPrimary() {
        #expect(RoleTheme.forRole(.family).primaryHex == HexPair(light: 0x6941C6, dark: 0xB692F6))
        #expect(RoleTheme.forRole(.olderAdult).primaryHex == HexPair(light: 0x0F766E, dark: 0x2DD4BF))
    }

    @Test(arguments: [Role.olderAdult, .family, .hospital, .neighbour])
    func everyThemePassesAAContrast(role: Role) {
        let theme = RoleTheme.forRole(role)
        let background = HexPair(light: 0xFFFFFF, dark: 0x0B1214)
        for dark in [false, true] {
            func hex(_ pair: HexPair) -> UInt32 { dark ? pair.dark : pair.light }
            #expect(Self.contrast(hex(theme.primaryHex), hex(background)) >= 4.5)
            #expect(Self.contrast(hex(theme.primaryHex), hex(theme.surfaceTintHex)) >= 4.5)
            #expect(Self.contrast(hex(theme.onPrimaryHex), hex(theme.primaryHex)) >= 4.5)
        }
    }

    /// WCAG 2.x contrast ratio.
    static func contrast(_ a: UInt32, _ b: UInt32) -> Double {
        func linear(_ channel: UInt32) -> Double {
            let value = Double(channel & 0xFF) / 255
            return value <= 0.03928 ? value / 12.92 : pow((value + 0.055) / 1.055, 2.4)
        }
        func luminance(_ rgb: UInt32) -> Double {
            let red: Double = 0.2126 * linear(rgb >> 16)
            let green: Double = 0.7152 * linear(rgb >> 8)
            let blue: Double = 0.0722 * linear(rgb)
            return red + green + blue
        }
        let (l1, l2) = (luminance(a), luminance(b))
        return (max(l1, l2) + 0.05) / (min(l1, l2) + 0.05)
    }
}
