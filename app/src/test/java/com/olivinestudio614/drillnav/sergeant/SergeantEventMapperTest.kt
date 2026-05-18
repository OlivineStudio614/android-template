package com.olivinestudio614.drillnav.sergeant

import com.olivinestudio614.drillnav.sergeant.SergeantEvent.Turn.Distance
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SergeantEventMapperTest {

    @Test
    fun `left turn at 500ft threshold returns Left FEET_500`() {
        val event = SergeantEventMapper.mapTurnEvent(
            maneuverType = "turn",
            maneuverModifier = "left",
            distanceRemainingMeters = 140f,
            announced = emptySet()
        )
        assertEquals(SergeantEvent.Turn.Left(Distance.FEET_500), event)
    }

    @Test
    fun `left turn already announced at 500ft returns null`() {
        val event = SergeantEventMapper.mapTurnEvent(
            maneuverType = "turn",
            maneuverModifier = "left",
            distanceRemainingMeters = 140f,
            announced = setOf(Distance.FEET_500)
        )
        assertNull(event)
    }

    @Test
    fun `left turn at 200ft threshold returns Left FEET_200`() {
        val event = SergeantEventMapper.mapTurnEvent(
            maneuverType = "turn",
            maneuverModifier = "left",
            distanceRemainingMeters = 55f,
            announced = setOf(Distance.FEET_500)
        )
        assertEquals(SergeantEvent.Turn.Left(Distance.FEET_200), event)
    }

    @Test
    fun `left turn at now threshold returns Left NOW`() {
        val event = SergeantEventMapper.mapTurnEvent(
            maneuverType = "turn",
            maneuverModifier = "left",
            distanceRemainingMeters = 10f,
            announced = setOf(Distance.FEET_500, Distance.FEET_200)
        )
        assertEquals(SergeantEvent.Turn.Left(Distance.NOW), event)
    }

    @Test
    fun `right turn modifier returns Right event`() {
        val event = SergeantEventMapper.mapTurnEvent(
            maneuverType = "turn",
            maneuverModifier = "right",
            distanceRemainingMeters = 140f,
            announced = emptySet()
        )
        assertEquals(SergeantEvent.Turn.Right(Distance.FEET_500), event)
    }

    @Test
    fun `slight left modifier returns SlightLeft event`() {
        val event = SergeantEventMapper.mapTurnEvent(
            maneuverType = "turn",
            maneuverModifier = "slight left",
            distanceRemainingMeters = 140f,
            announced = emptySet()
        )
        assertEquals(SergeantEvent.Turn.SlightLeft(Distance.FEET_500), event)
    }

    @Test
    fun `straight modifier returns null`() {
        val event = SergeantEventMapper.mapTurnEvent(
            maneuverType = "continue",
            maneuverModifier = "straight",
            distanceRemainingMeters = 140f,
            announced = emptySet()
        )
        assertNull(event)
    }

    @Test
    fun `distance above 500ft threshold returns null`() {
        val event = SergeantEventMapper.mapTurnEvent(
            maneuverType = "turn",
            maneuverModifier = "left",
            distanceRemainingMeters = 300f,
            announced = emptySet()
        )
        assertNull(event)
    }
}
