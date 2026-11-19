package uk.checkinsos.domain.usecase

import uk.checkinsos.domain.model.DomainException
import uk.checkinsos.domain.model.Neighbour
import uk.checkinsos.domain.model.OlderAdult
import uk.checkinsos.domain.model.TimelineEvent
import uk.checkinsos.domain.repository.OlderAdultRepository
import uk.checkinsos.domain.repository.TimelineRepository
import uk.checkinsos.domain.service.NeighbourValidator

class GetLovedOnes(private val olderAdults: OlderAdultRepository) {
    suspend operator fun invoke(): List<OlderAdult> = olderAdults.lovedOnes()
}

/** Current status = latest score + 1st-level neighbour. */
class GetLovedOneStatus(private val olderAdults: OlderAdultRepository) {
    suspend operator fun invoke(olderAdultId: String): OlderAdult = olderAdults.olderAdult(olderAdultId)
}

/** Validates then saves the 1st-level neighbour. Throws [DomainException.InvalidNeighbour]. */
class UpdateNeighbour(private val olderAdults: OlderAdultRepository) {
    suspend operator fun invoke(olderAdultId: String, neighbour: Neighbour): OlderAdult =
        when (val result = NeighbourValidator.validate(neighbour)) {
            is NeighbourValidator.Result.Invalid -> throw DomainException.InvalidNeighbour(result.error)
            is NeighbourValidator.Result.Valid -> olderAdults.updateNeighbour(olderAdultId, result.neighbour)
        }
}

/** Timeline events, oldest first. */
class GetTimeline(private val timeline: TimelineRepository) {
    suspend operator fun invoke(olderAdultId: String): List<TimelineEvent> =
        timeline.events(olderAdultId).sortedBy { it.date }
}
