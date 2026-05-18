package com.olivinestudio614.hartmannav.hartman

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Test

class HartmanPhraseLibraryTest {

    @Test
    fun `all event types return a non-blank phrase`() {
        val events = listOf(
            HartmanEvent.TripStart,
            HartmanEvent.Turn.Left(HartmanEvent.Turn.Distance.FEET_500),
            HartmanEvent.Turn.Left(HartmanEvent.Turn.Distance.FEET_200),
            HartmanEvent.Turn.Left(HartmanEvent.Turn.Distance.NOW),
            HartmanEvent.Turn.Right(HartmanEvent.Turn.Distance.FEET_500),
            HartmanEvent.Turn.Right(HartmanEvent.Turn.Distance.FEET_200),
            HartmanEvent.Turn.Right(HartmanEvent.Turn.Distance.NOW),
            HartmanEvent.Turn.SlightLeft(HartmanEvent.Turn.Distance.FEET_500),
            HartmanEvent.Turn.SlightLeft(HartmanEvent.Turn.Distance.NOW),
            HartmanEvent.Turn.SlightRight(HartmanEvent.Turn.Distance.FEET_500),
            HartmanEvent.Turn.SlightRight(HartmanEvent.Turn.Distance.NOW),
            HartmanEvent.Continue,
            HartmanEvent.Recalculating(1),
            HartmanEvent.Recalculating(2),
            HartmanEvent.Recalculating(3),
            HartmanEvent.Recalculating(5),
            HartmanEvent.SpeedWarning,
            HartmanEvent.IdleTaunt,
            HartmanEvent.Arrival,
        )
        events.forEach { event ->
            val phrase = HartmanPhraseLibrary.phraseFor(event)
            assertFalse("Phrase for $event was blank", phrase.isBlank())
        }
    }

    @Test
    fun `same event called many times returns multiple different phrases`() {
        val seen = mutableSetOf<String>()
        repeat(20) {
            seen += HartmanPhraseLibrary.phraseFor(HartmanEvent.TripStart)
        }
        assert(seen.size > 1) { "Expected multiple different phrases, only got: $seen" }
    }

    @Test
    fun `consecutive calls for same event do not return the same phrase`() {
        val first = HartmanPhraseLibrary.phraseFor(HartmanEvent.Arrival)
        val second = HartmanPhraseLibrary.phraseFor(HartmanEvent.Arrival)
        assertNotEquals("Two consecutive calls returned the same phrase", first, second)
    }
}
