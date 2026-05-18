package com.olivinestudio614.hartmannav.hartman

import com.olivinestudio614.hartmannav.hartman.HartmanEvent.Turn.Distance
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HartmanEventMapperTest {

    @Test
    fun `left turn at 500ft threshold returns Left FEET_500`() {
        val event = HartmanEventMapper.mapTurnEvent(
            maneuverType = "turn",
            maneuverModifier = "left",
            distanceRemainingMeters = 140f,
            announced = emptySet()
        )
        assertEquals(HartmanEvent.Turn.Left(Distance.FEET_500), event)
    }

    @Test
    fun `left turn already announced at 500ft returns null`() {
        val event = HartmanEventMapper.mapTurnEvent(
            maneuverType = "turn",
            maneuverModifier = "left",
            distanceRemainingMeters = 140f,
            announced = setOf(Distance.FEET_500)
        )
        assertNull(event)
    }

    @Test
    fun `left turn at 200ft threshold returns Left FEET_200`() {
        val event = HartmanEventMapper.mapTurnEvent(
            maneuverType = "turn",
            maneuverModifier = "left",
            distanceRemainingMeters = 55f,
            announced = setOf(Distance.FEET_500)
        )
        assertEquals(HartmanEvent.Turn.Left(Distance.FEET_200), event)
    }

    @Test
    fun `left turn at now threshold returns Left NOW`() {
        val event = HartmanEventMapper.mapTurnEvent(
            maneuverType = "turn",
            maneuverModifier = "left",
            distanceRemainingMeters = 10f,
            announced = setOf(Distance.FEET_500, Distance.FEET_200)
        )
        assertEquals(HartmanEvent.Turn.Left(Distance.NOW), event)
    }

    @Test
    fun `right turn modifier returns Right event`() {
        val event = HartmanEventMapper.mapTurnEvent(
            maneuverType = "turn",
            maneuverModifier = "right",
            distanceRemainingMeters = 140f,
            announced = emptySet()
        )
        assertEquals(HartmanEvent.Turn.Right(Distance.FEET_500), event)
    }

    @Test
    fun `slight left modifier returns SlightLeft event`() {
        val event = HartmanEventMapper.mapTurnEvent(
            maneuverType = "turn",
            maneuverModifier = "slight left",
            distanceRemainingMeters = 140f,
            announced = emptySet()
        )
        assertEquals(HartmanEvent.Turn.SlightLeft(Distance.FEET_500), event)
    }

    @Test
    fun `straight modifier returns null`() {
        val event = HartmanEventMapper.mapTurnEvent(
            maneuverType = "continue",
            maneuverModifier = "straight",
            distanceRemainingMeters = 140f,
            announced = emptySet()
        )
        assertNull(event)
    }

    @Test
    fun `distance above 500ft threshold returns null`() {
        val event = HartmanEventMapper.mapTurnEvent(
            maneuverType = "turn",
            maneuverModifier = "left",
            distanceRemainingMeters = 300f,
            announced = emptySet()
        )
        assertNull(event)
    }
}
