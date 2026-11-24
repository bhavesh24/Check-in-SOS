import Domain
import Observation
import SwiftUI

/// Every pushed screen. Home is the NavigationStack root.
enum Route: Hashable {
    // Older adult
    case assessment
    case score(score: Int, neighbourNotified: Bool)
    case noCheckIn
    // Family
    case lovedOnes
    case status(olderAdultId: String)
    case editNeighbour(olderAdultId: String)
    case timeline(olderAdultId: String)
    // Neighbour
    case neighbours
    case neighbourAlert(AlertSummary)
    case reached(AlertSummary)
    case escalate(AlertSummary)
    case alive(AlertSummary, calledNumber: String?)
    // Hospital
    case patients
    case hospitalAlert(AlertSummary)
    case careStep(CareStep, AlertSummary)
}

extension Route {
    /// The Home card this screen's flow started from; the screen is themed in its colour.
    var role: Role {
        switch self {
        case .assessment, .score, .noCheckIn: .olderAdult
        case .lovedOnes, .status, .editNeighbour, .timeline: .family
        case .neighbours, .neighbourAlert, .reached, .escalate, .alive: .neighbour
        case .patients, .hospitalAlert, .careStep: .hospital
        }
    }
}

/// Owns the navigation path. ViewModels never see it: they report outcomes through closures that
/// `RootView` maps onto these methods.
@MainActor @Observable
final class AppRouter {
    var path: [Route] = []

    func push(_ route: Route) { path.append(route) }
    func pop() { _ = path.popLast() }
    func popToRoot() { path.removeAll() }

    /// Pops back to the last occurrence of `route` (e.g. the Neighbours list), or to Home if absent.
    func popTo(_ route: Route) {
        guard let index = path.lastIndex(of: route) else { return popToRoot() }
        path.removeSubrange(path.index(after: index)...)
    }

    static func route(for role: Role) -> Route {
        switch role {
        case .olderAdult: .assessment
        case .family: .lovedOnes
        case .hospital: .patients
        case .neighbour: .neighbours
        }
    }
}

struct RootView: View {
    let container: AppContainer
    @State private var router = AppRouter()

    var body: some View {
        NavigationStack(path: $router.path) {
            HomeView(viewModel: container.makeHome { [router] in router.push(AppRouter.route(for: $0)) })
                .navigationDestination(for: Route.self, destination: destination)
        }
        .tint(RoleTheme.forRole(.olderAdult).primary)
    }

    private func destination(_ route: Route) -> some View {
        screen(route).roleTheme(route.role)
    }

    @ViewBuilder
    private func screen(_ route: Route) -> some View {
        let router = router
        switch route {
        case .assessment:
            AssessmentView(viewModel: container.makeAssessment(
                onSubmitted: { router.push(.score(score: $0.score, neighbourNotified: $0.neighbourNotified)) },
                onMissedCheckIn: { router.push(.noCheckIn) }))
        case let .score(score, neighbourNotified):
            ScoreView(viewModel: ScoreViewModel(score: score, neighbourNotified: neighbourNotified,
                                                onDone: router.popToRoot))
        case .noCheckIn:
            NoCheckInView(viewModel: NoCheckInViewModel())

        case .lovedOnes:
            LovedOnesView(viewModel: container.makeLovedOnes { router.push(.status(olderAdultId: $0)) })
        case let .status(id):
            StatusView(viewModel: container.makeStatus(
                olderAdultId: id,
                onEditNeighbour: { router.push(.editNeighbour(olderAdultId: $0)) },
                onTimeline: { router.push(.timeline(olderAdultId: $0)) }))
        case let .editNeighbour(id):
            EditNeighbourView(viewModel: container.makeEditNeighbour(olderAdultId: id, onSaved: router.pop))
        case let .timeline(id):
            TimelineView(viewModel: container.makeTimeline(olderAdultId: id))

        case .neighbours:
            NeighboursView(viewModel: container.makeNeighbours { router.push(.neighbourAlert($0)) })
        case let .neighbourAlert(alert), let .hospitalAlert(alert):
            HelpAlertView(viewModel: container.makeHelpAlert(alert: alert) { accepted in
                router.push(accepted.recipient == .neighbour ? .reached(accepted) : .careStep(.inCare, accepted))
            })
        case let .reached(alert):
            ReachedView(viewModel: container.makeReached(alert: alert) { router.push(.escalate($0)) })
        case let .escalate(alert):
            EscalateView(viewModel: container.makeEscalate(
                alert: alert,
                onAllOk: { router.popTo(.neighbours) },
                onCalled: { router.push(.alive($0, calledNumber: $1)) }))
        case let .alive(alert, calledNumber):
            AliveView(viewModel: container.makeAlive(alert: alert, calledNumber: calledNumber) {
                router.popTo(.neighbours)
            })

        case .patients:
            PatientsView(viewModel: container.makePatients { router.push(.hospitalAlert($0)) })
        case let .careStep(step, alert):
            CareStepView(viewModel: container.makeCareStep(step: step, alert: alert) { next in
                if let next { router.push(.careStep(next, alert)) } else { router.popTo(.patients) }
            })
        }
    }
}
