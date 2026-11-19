package uk.checkinsos.domain.usecase

import uk.checkinsos.domain.model.AlertRecipient
import uk.checkinsos.domain.model.Assessment
import uk.checkinsos.domain.model.AssessmentResult
import uk.checkinsos.domain.model.TimelineEventKind
import uk.checkinsos.domain.repository.AlertRepository
import uk.checkinsos.domain.repository.OlderAdultRepository
import uk.checkinsos.domain.repository.TimelineRepository
import uk.checkinsos.domain.service.EscalationPolicy
import uk.checkinsos.domain.service.ScoreCalculator
import uk.checkinsos.domain.service.TimeProvider

/** Scores a check-in, stores it, and notifies the 1st-level neighbour when the score is below 70. */
class SubmitAssessment(
    private val olderAdults: OlderAdultRepository,
    private val alerts: AlertRepository,
    private val timeline: TimelineRepository,
    private val time: TimeProvider,
) {
    suspend operator fun invoke(olderAdultId: String, assessment: Assessment): AssessmentResult {
        val score = ScoreCalculator.score(assessment)
        val now = time.now()
        olderAdults.recordScore(olderAdultId, score)
        timeline.append(olderAdultId, TimelineEventKind.Score(score), now)

        val timeout = EscalationPolicy.acknowledgementTimeout(score)
        if (!EscalationPolicy.shouldAlert(score) || timeout == null) {
            return AssessmentResult(score, neighbourNotified = false)
        }
        val adult = olderAdults.olderAdult(olderAdultId)
        alerts.create(olderAdultId, adult.name, AlertRecipient.NEIGHBOUR, score, now, now.plus(timeout))
        timeline.append(olderAdultId, TimelineEventKind.NotificationSent(AlertRecipient.NEIGHBOUR.level), now)
        return AssessmentResult(score, neighbourNotified = true)
    }
}
