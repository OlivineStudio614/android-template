package com.olivinestudio614.hartmannav.hartman

sealed class HartmanEvent {
    data object TripStart : HartmanEvent()

    sealed class Turn : HartmanEvent() {
        enum class Distance { FEET_500, FEET_200, NOW }
        data class Left(val distance: Distance) : Turn()
        data class Right(val distance: Distance) : Turn()
        data class SlightLeft(val distance: Distance) : Turn()
        data class SlightRight(val distance: Distance) : Turn()
    }

    data object Continue : HartmanEvent()
    data class Recalculating(val count: Int) : HartmanEvent()
    data object SpeedWarning : HartmanEvent()
    data object IdleTaunt : HartmanEvent()
    data object Arrival : HartmanEvent()
}
