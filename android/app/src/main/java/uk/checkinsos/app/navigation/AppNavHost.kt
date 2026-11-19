package uk.checkinsos.app.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import uk.checkinsos.app.AppContainer
import uk.checkinsos.app.designsystem.CheckInTheme
import uk.checkinsos.app.feature.family.editneighbour.EditNeighbourScreen
import uk.checkinsos.app.feature.family.lovedones.LovedOnesScreen
import uk.checkinsos.app.feature.family.status.StatusScreen
import uk.checkinsos.app.feature.family.timeline.TimelineScreen
import uk.checkinsos.app.feature.home.HomeScreen
import uk.checkinsos.app.feature.hospital.carestep.CareStep
import uk.checkinsos.app.feature.hospital.carestep.CareStepScreen
import uk.checkinsos.app.feature.hospital.patients.PatientsScreen
import uk.checkinsos.app.feature.neighbour.alive.AliveScreen
import uk.checkinsos.app.feature.neighbour.escalate.EscalateScreen
import uk.checkinsos.app.feature.neighbour.neighbours.NeighboursScreen
import uk.checkinsos.app.feature.neighbour.reached.ReachedScreen
import uk.checkinsos.app.feature.olderadult.assessment.AssessmentScreen
import uk.checkinsos.app.feature.olderadult.nocheckin.NoCheckInScreen
import uk.checkinsos.app.feature.olderadult.score.ScoreScreen
import uk.checkinsos.app.feature.shared.helpalert.HelpAlertScreen
import uk.checkinsos.domain.model.AlertRecipient

/** The whole app: theme + navigation. Used by MainActivity and by the UI tests (with a test container). */
@Composable
fun CheckInSosRoot(container: AppContainer) {
    CheckInTheme { AppNavHost(container) }
}

/**
 * Maps ViewModel events onto the back stack. ViewModels are scoped to their back-stack entry and
 * built by [AppContainer] factories.
 */
@Composable
fun AppNavHost(container: AppContainer, navController: NavHostController = rememberNavController()) {
    val back: () -> Unit = { navController.navigateUp() }
    NavHost(navController, startDestination = Routes.Home) {
        composable<Routes.Home> {
            HomeScreen(viewModel { container.makeHome() }) { navController.navigate(Routes.forRole(it)) }
        }

        // Older adult
        composable<Routes.Assessment> {
            AssessmentScreen(
                viewModel { container.makeAssessment() },
                onBack = back,
                onSubmitted = { navController.navigate(Routes.Score(it.score, it.neighbourNotified)) },
                onMissedCheckIn = { navController.navigate(Routes.NoCheckIn) },
            )
        }
        composable<Routes.Score> { entry ->
            val route = entry.toRoute<Routes.Score>()
            ScoreScreen(viewModel { container.makeScore(route.score, route.neighbourNotified) }, onBack = back) {
                navController.popBackStack<Routes.Home>(inclusive = false)
            }
        }
        composable<Routes.NoCheckIn> {
            NoCheckInScreen(viewModel { container.makeNoCheckIn() }, onBack = back)
        }

        // Family
        composable<Routes.LovedOnes> {
            LovedOnesScreen(viewModel { container.makeLovedOnes() }, onBack = back) {
                navController.navigate(Routes.Status(it))
            }
        }
        composable<Routes.Status> { entry ->
            val id = entry.toRoute<Routes.Status>().olderAdultId
            StatusScreen(
                viewModel { container.makeStatus(id) },
                onBack = back,
                onEditNeighbour = { navController.navigate(Routes.EditNeighbour(it)) },
                onTimeline = { navController.navigate(Routes.Timeline(it)) },
            )
        }
        composable<Routes.EditNeighbour> { entry ->
            val id = entry.toRoute<Routes.EditNeighbour>().olderAdultId
            EditNeighbourScreen(viewModel { container.makeEditNeighbour(id) }, onBack = back, onSaved = back)
        }
        composable<Routes.Timeline> { entry ->
            val id = entry.toRoute<Routes.Timeline>().olderAdultId
            TimelineScreen(viewModel { container.makeTimeline(id) }, onBack = back)
        }

        // Neighbour
        composable<Routes.Neighbours> {
            NeighboursScreen(viewModel { container.makeNeighbours() }, onBack = back) {
                navController.navigate(Routes.helpAlert(it))
            }
        }
        composable<Routes.HelpAlert> { entry ->
            val alert = entry.toRoute<Routes.HelpAlert>().alert
            HelpAlertScreen(viewModel { container.makeHelpAlert(alert) }, onBack = back) { accepted ->
                navController.navigate(
                    if (accepted.recipient == AlertRecipient.NEIGHBOUR) Routes.reached(accepted)
                    else Routes.careStep(CareStep.IN_CARE, accepted),
                )
            }
        }
        composable<Routes.Reached> { entry ->
            val alert = entry.toRoute<Routes.Reached>().alert
            ReachedScreen(viewModel { container.makeReached(alert) }, onBack = back) {
                navController.navigate(Routes.escalate(it))
            }
        }
        composable<Routes.Escalate> { entry ->
            val alert = entry.toRoute<Routes.Escalate>().alert
            EscalateScreen(
                viewModel { container.makeEscalate(alert) },
                onBack = back,
                onAllOk = { navController.popBackStack<Routes.Neighbours>(inclusive = false) },
                onCalled = { a, number -> navController.navigate(Routes.alive(a, number)) },
            )
        }
        composable<Routes.Alive> { entry ->
            val route = entry.toRoute<Routes.Alive>()
            AliveScreen(viewModel { container.makeAlive(route.alert, route.calledNumber) }, onBack = back) {
                navController.popBackStack<Routes.Neighbours>(inclusive = false)
            }
        }

        // Hospital
        composable<Routes.Patients> {
            PatientsScreen(viewModel { container.makePatients() }, onBack = back) {
                navController.navigate(Routes.helpAlert(it))
            }
        }
        composable<Routes.CareStepRoute> { entry ->
            val route = entry.toRoute<Routes.CareStepRoute>()
            CareStepScreen(viewModel { container.makeCareStep(route.careStep, route.alert) }, onBack = back) { next, alert ->
                if (next != null) {
                    navController.navigate(Routes.careStep(next, alert))
                } else {
                    navController.popBackStack<Routes.Patients>(inclusive = false)
                }
            }
        }
    }
}
