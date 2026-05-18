package com.olivinestudio614.drillnav.sergeant

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Test

class SergeantPhraseLibraryTest {

    @Test
    fun `all event types return a non-blank phrase`() {
        val events = listOf(
            SergeantEvent.TripStart,
            SergeantEvent.Turn.Left(SergeantEvent.Turn.Distance.FEET_500),
            SergeantEvent.Turn.Left(SergeantEvent.Turn.Distance.FEET_200),
            SergeantEvent.Turn.Left(SergeantEvent.Turn.Distance.NOW),
            SergeantEvent.Turn.Right(SergeantEvent.Turn.Distance.FEET_500),
            SergeantEvent.Turn.Right(SergeantEvent.Turn.Distance.FEET_200),
            SergeantEvent.Turn.Right(SergeantEvent.Turn.Distance.NOW),
            SergeantEvent.Turn.SlightLeft(SergeantEvent.Turn.Distance.FEET_500),
            SergeantEvent.Turn.SlightLeft(SergeantEvent.Turn.Distance.NOW),
            SergeantEvent.Turn.SlightRight(SergeantEvent.Turn.Distance.FEET_500),
            SergeantEvent.Turn.SlightRight(SergeantEvent.Turn.Distance.NOW),
            SergeantEvent.Continue,
            SergeantEvent.Recalculating(1),
            SergeantEvent.Recalculating(2),
            SergeantEvent.Recalculating(3),
            SergeantEvent.Recalculating(5),
            SergeantEvent.SpeedWarning,
            SergeantEvent.IdleTaunt,
            SergeantEvent.Arrival,
        )
        events.forEach { event ->
            val phrase = SergeantPhraseLibrary.phraseFor(event)
            assertFalse("Phrase for $event was blank", phrase.isBlank())
        }
    }

    @Test
    fun `same event called many times returns multiple different phrases`() {
        val seen = mutableSetOf<String>()
        repeat(20) {
            seen += SergeantPhraseLibrary.phraseFor(SergeantEvent.TripStart)
        }
        assert(seen.size > 1) { "Expected multiple different phrases, only got: $seen" }
    }

    @Test
    fun `consecutive calls for same event do not return the same phrase`() {
        val first = SergeantPhraseLibrary.phraseFor(SergeantEvent.Arrival)
        val second = SergeantPhraseLibrary.phraseFor(SergeantEvent.Arrival)
        assertNotEquals("Two consecutive calls returned the same phrase", first, second)
    }
}
