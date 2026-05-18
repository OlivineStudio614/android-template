package com.olivinestudio614.drillnav.sergeant

import com.olivinestudio614.drillnav.sergeant.SergeantEvent.Turn.Distance

object SergeantEventMapper {

    private const val METERS_500FT = 152f
    private const val METERS_200FT = 61f
    private const val METERS_NOW = 15f

    fun mapTurnEvent(
        maneuverType: String?,
        maneuverModifier: String?,
        distanceRemainingMeters: Float,
        announced: Set<Distance>
    ): SergeantEvent.Turn? {
        val direction = resolveDirection(maneuverModifier) ?: return null

        val threshold = when {
            distanceRemainingMeters <= METERS_NOW && Distance.NOW !in announced -> Distance.NOW
            distanceRemainingMeters <= METERS_200FT && Distance.FEET_200 !in announced -> Distance.FEET_200
            distanceRemainingMeters <= METERS_500FT && Distance.FEET_500 !in announced -> Distance.FEET_500
            else -> return null
        }

        return when (direction) {
            Direction.LEFT -> SergeantEvent.Turn.Left(threshold)
            Direction.RIGHT -> SergeantEvent.Turn.Right(threshold)
            Direction.SLIGHT_LEFT -> SergeantEvent.Turn.SlightLeft(threshold)
            Direction.SLIGHT_RIGHT -> SergeantEvent.Turn.SlightRight(threshold)
        }
    }

    private fun resolveDirection(modifier: String?): Direction? = when (modifier) {
        "left", "sharp left" -> Direction.LEFT
        "right", "sharp right" -> Direction.RIGHT
        "slight left" -> Direction.SLIGHT_LEFT
        "slight right" -> Direction.SLIGHT_RIGHT
        else -> null
    }

    private enum class Direction { LEFT, RIGHT, SLIGHT_LEFT, SLIGHT_RIGHT }
}
