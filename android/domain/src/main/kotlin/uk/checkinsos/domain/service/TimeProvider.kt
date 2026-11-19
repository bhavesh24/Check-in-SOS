package uk.checkinsos.domain.service

import java.time.Instant

/** Injectable clock so countdowns and timestamps are deterministic in tests. */
fun interface TimeProvider {
    fun now(): Instant
}

object SystemTimeProvider : TimeProvider {
    override fun now(): Instant = Instant.now()
}

class FixedTimeProvider(private val instant: Instant) : TimeProvider {
    override fun now(): Instant = instant
}
