package com.olivinestudio614.drillnav.sergeant

sealed class SergeantEvent {
    data object TripStart : SergeantEvent()

    sealed class Turn : SergeantEvent() {
        enum class Distance { FEET_500, FEET_200, NOW }
        abstract val distance: Distance
        data class Left(override val distance: Distance) : Turn()
        data class Right(override val distance: Distance) : Turn()
        data class SlightLeft(override val distance: Distance) : Turn()
        data class SlightRight(override val distance: Distance) : Turn()
    }

    data object Continue : SergeantEvent()
    data class Recalculating(val count: Int) : SergeantEvent()
    data object SpeedWarning : SergeantEvent()
    data object IdleTaunt : SergeantEvent()
    data object Arrival : SergeantEvent()
}
