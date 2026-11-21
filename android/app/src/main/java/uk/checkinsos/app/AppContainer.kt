package uk.checkinsos.app

import uk.checkinsos.app.feature.family.editneighbour.EditNeighbourViewModel
import uk.checkinsos.app.feature.family.lovedones.LovedOnesViewModel
import uk.checkinsos.app.feature.family.status.StatusViewModel
import uk.checkinsos.app.feature.family.timeline.TimelineViewModel
import uk.checkinsos.app.feature.home.HomeViewModel
import uk.checkinsos.app.feature.hospital.carestep.CareStep
import uk.checkinsos.app.feature.hospital.carestep.CareStepViewModel
import uk.checkinsos.app.feature.hospital.patients.PatientsViewModel
import uk.checkinsos.app.feature.neighbour.alive.AliveViewModel
import uk.checkinsos.app.feature.neighbour.escalate.EscalateViewModel
import uk.checkinsos.app.feature.neighbour.neighbours.NeighboursViewModel
import uk.checkinsos.app.feature.neighbour.reached.ReachedViewModel
import uk.checkinsos.app.feature.olderadult.assessment.AssessmentViewModel
import uk.checkinsos.app.feature.olderadult.nocheckin.NoCheckInViewModel
import uk.checkinsos.app.feature.olderadult.score.ScoreViewModel
import uk.checkinsos.app.feature.shared.AlertSummary
import uk.checkinsos.app.feature.shared.helpalert.HelpAlertViewModel
import uk.checkinsos.data.AlertRepositoryImpl
import uk.checkinsos.data.MockDataSource
import uk.checkinsos.data.MockFixtures
import uk.checkinsos.data.OlderAdultRepositoryImpl
import uk.checkinsos.data.PatientRepositoryImpl
import uk.checkinsos.data.TimelineRepositoryImpl
import uk.checkinsos.domain.service.FixedTimeProvider
import uk.checkinsos.domain.service.SystemTimeProvider
import uk.checkinsos.domain.service.TimeProvider
import uk.checkinsos.domain.usecase.AcknowledgeAlert
import uk.checkinsos.domain.usecase.EscalateAlert
import uk.checkinsos.domain.usecase.GetLovedOneStatus
import uk.checkinsos.domain.usecase.GetLovedOnes
import uk.checkinsos.domain.usecase.GetNeighbours
import uk.checkinsos.domain.usecase.GetPatients
import uk.checkinsos.domain.usecase.GetScoreHistory
import uk.checkinsos.domain.usecase.GetTimeline
import uk.checkinsos.domain.usecase.MarkArrived
import uk.checkinsos.domain.usecase.RecordCareOutcome
import uk.checkinsos.domain.usecase.SimulateAlert
import uk.checkinsos.domain.usecase.SubmitAssessment
import uk.checkinsos.domain.usecase.UpdateNeighbour
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

/**
 * Composition root (excluded from coverage): the only presentation file that touches the Data layer.
 * Wires repositories → use cases → ViewModels by constructor injection.
 */
class AppContainer(
    source: MockDataSource,
    private val time: TimeProvider,
    private val zone: ZoneId,
    private val currentOlderAdultId: String = MockFixtures.CURRENT_OLDER_ADULT_ID,
) {
    private val olderAdults = OlderAdultRepositoryImpl(source)
    private val patients = PatientRepositoryImpl(source)
    private val alerts = AlertRepositoryImpl(source)
    private val timeline = TimelineRepositoryImpl(source)

    private val recordCareOutcome get() = RecordCareOutcome(alerts, timeline, time)
    private val getLovedOneStatus get() = GetLovedOneStatus(olderAdults)
    private val simulateAlert get() = SimulateAlert(olderAdults, patients, alerts, timeline, time)

    fun makeHome() = HomeViewModel()
    fun makeAssessment() = AssessmentViewModel(SubmitAssessment(olderAdults, alerts, timeline, time), currentOlderAdultId)
    fun makeScore(score: Int, neighbourNotified: Boolean) = ScoreViewModel(score, neighbourNotified)
    fun makeNoCheckIn() = NoCheckInViewModel()

    fun makeLovedOnes() = LovedOnesViewModel(GetLovedOnes(olderAdults), time, zone)
    fun makeStatus(olderAdultId: String) =
        StatusViewModel(olderAdultId, getLovedOneStatus, GetScoreHistory(olderAdults), time, zone)
    fun makeEditNeighbour(olderAdultId: String) =
        EditNeighbourViewModel(olderAdultId, getLovedOneStatus, UpdateNeighbour(olderAdults))
    fun makeTimeline(olderAdultId: String) = TimelineViewModel(olderAdultId, GetTimeline(timeline), zone)

    fun makeNeighbours() = NeighboursViewModel(GetNeighbours(olderAdults), simulateAlert)
    fun makePatients() = PatientsViewModel(GetPatients(patients), simulateAlert)
    fun makeHelpAlert(alert: AlertSummary) = HelpAlertViewModel(alert, AcknowledgeAlert(alerts, timeline, time), time)
    fun makeReached(alert: AlertSummary) = ReachedViewModel(alert, MarkArrived(alerts, timeline, time))
    fun makeEscalate(alert: AlertSummary) =
        EscalateViewModel(alert, recordCareOutcome, EscalateAlert(alerts, timeline, time))
    fun makeAlive(alert: AlertSummary, calledNumber: String?) = AliveViewModel(alert, calledNumber, recordCareOutcome)
    fun makeCareStep(step: CareStep, alert: AlertSummary) = CareStepViewModel(step, alert, recordCareOutcome)

    companion object {
        /** One store for the app process, so data survives configuration changes. */
        val live: AppContainer by lazy {
            AppContainer(MockDataSource(Instant.now()), SystemTimeProvider, ZoneId.systemDefault())
        }

        /** UI tests: fresh mock data, the fixed clock and UTC times, so every run is deterministic. */
        fun forTesting(): AppContainer = AppContainer(
            MockDataSource(MockFixtures.FIXED_NOW), FixedTimeProvider(MockFixtures.FIXED_NOW), ZoneOffset.UTC,
        )
    }
}
