package com.olivinestudio614.hartmannav.hartman

object HartmanPhraseLibrary {

    private val lastUsed = mutableMapOf<String, String>()

    fun phraseFor(event: HartmanEvent): String {
        val key = keyFor(event)
        val pool = poolFor(event)
        val available = if (pool.size > 1) pool.filter { it != lastUsed[key] } else pool
        return available.random().also { lastUsed[key] = it }
    }

    private fun keyFor(event: HartmanEvent): String = when (event) {
        is HartmanEvent.TripStart -> "trip_start"
        is HartmanEvent.Turn.Left -> "turn_left_${event.distance.name}"
        is HartmanEvent.Turn.Right -> "turn_right_${event.distance.name}"
        is HartmanEvent.Turn.SlightLeft -> "turn_slight_left_${event.distance.name}"
        is HartmanEvent.Turn.SlightRight -> "turn_slight_right_${event.distance.name}"
        is HartmanEvent.Continue -> "continue"
        is HartmanEvent.Recalculating -> when {
            event.count == 1 -> "recalc_1"
            event.count == 2 -> "recalc_2"
            else -> "recalc_3plus"
        }
        is HartmanEvent.SpeedWarning -> "speed_warning"
        is HartmanEvent.IdleTaunt -> "idle_taunt"
        is HartmanEvent.Arrival -> "arrival"
    }

    private fun poolFor(event: HartmanEvent): List<String> = when (event) {
        is HartmanEvent.TripStart -> TRIP_START
        is HartmanEvent.Turn.Left -> when (event.distance) {
            HartmanEvent.Turn.Distance.FEET_500 -> TURN_LEFT_500
            HartmanEvent.Turn.Distance.FEET_200 -> TURN_LEFT_200
            HartmanEvent.Turn.Distance.NOW -> TURN_LEFT_NOW
        }
        is HartmanEvent.Turn.Right -> when (event.distance) {
            HartmanEvent.Turn.Distance.FEET_500 -> TURN_RIGHT_500
            HartmanEvent.Turn.Distance.FEET_200 -> TURN_RIGHT_200
            HartmanEvent.Turn.Distance.NOW -> TURN_RIGHT_NOW
        }
        is HartmanEvent.Turn.SlightLeft -> when (event.distance) {
            HartmanEvent.Turn.Distance.NOW -> TURN_SLIGHT_LEFT_NOW
            else -> TURN_SLIGHT_LEFT_500 // FEET_500 and FEET_200 both use the 500-ft cue; slight turns don't need a separate 200-ft prompt
        }
        is HartmanEvent.Turn.SlightRight -> when (event.distance) {
            HartmanEvent.Turn.Distance.NOW -> TURN_SLIGHT_RIGHT_NOW
            else -> TURN_SLIGHT_RIGHT_500 // FEET_500 and FEET_200 both use the 500-ft cue; slight turns don't need a separate 200-ft prompt
        }
        is HartmanEvent.Continue -> CONTINUE
        is HartmanEvent.Recalculating -> when {
            event.count == 1 -> RECALCULATING_1
            event.count == 2 -> RECALCULATING_2
            else -> RECALCULATING_3PLUS
        }
        is HartmanEvent.SpeedWarning -> SPEED_WARNING
        is HartmanEvent.IdleTaunt -> IDLE_TAUNT
        is HartmanEvent.Arrival -> ARRIVAL
    }

    // ── Phrase Library ──────────────────────────────────────────────────────

    private val TRIP_START = listOf(
        "LISTEN UP, MAGGOT. I WILL GUIDE YOUR PATHETIC SELF TO YOUR DESTINATION. TRY NOT TO DISAPPOINT ME.",
        "ATTENTION. ROUTE LOCKED AND LOADED. FOLLOW MY INSTRUCTIONS TO THE LETTER OR FACE THE CONSEQUENCES.",
        "FALL IN, PRIVATE. NAVIGATION ENGAGED. DO NOT MAKE ME REPEAT MYSELF. NOT ONCE.",
        "MOVE OUT. YOUR ROUTE HAS BEEN CALCULATED. ALL YOU HAVE TO DO IS NOT RUIN IT.",
        "I HAVE SEEN BLIND MEN NAVIGATE BETTER THAN YOU. TODAY YOU WILL PROVE ME WRONG. MOVE.",
    )

    private val TURN_LEFT_500 = listOf(
        "IN FIVE HUNDRED FEET, TURN LEFT. PAY ATTENTION FOR ONCE IN YOUR MISERABLE LIFE.",
        "FIVE HUNDRED FEET UNTIL YOUR LEFT TURN. GET IN THE CORRECT LANE, PRIVATE.",
        "PREPARE TO TURN LEFT IN FIVE HUNDRED FEET. DO NOT BLOW PAST IT.",
        "FIVE HUNDRED FEET. TURN LEFT. OR DON'T. AND SEE WHAT HAPPENS.",
    )

    private val TURN_LEFT_200 = listOf(
        "TWO HUNDRED FEET. TURN LEFT. GET READY.",
        "LEFT TURN IN TWO HUNDRED FEET. MOVE INTO POSITION.",
        "TWO HUNDRED FEET, PRIVATE. LEFT TURN COMING. DO NOT MISS IT.",
        "IN TWO HUNDRED FEET YOU WILL TURN LEFT. I AM WATCHING YOU.",
    )

    private val TURN_LEFT_NOW = listOf(
        "TURN LEFT NOW. ARE YOUR EYES OPEN?",
        "LEFT! LEFT! TURN LEFT NOW, PRIVATE!",
        "TURN LEFT NOW. DO NOT MAKE ME SAY IT AGAIN.",
        "EXECUTE LEFT TURN. NOW. WHAT ARE YOU WAITING FOR?",
    )

    private val TURN_RIGHT_500 = listOf(
        "IN FIVE HUNDRED FEET, TURN RIGHT. STAY ALERT.",
        "FIVE HUNDRED FEET UNTIL YOUR RIGHT TURN. GET YOUR HEAD IN THE GAME.",
        "PREPARE FOR A RIGHT TURN IN FIVE HUNDRED FEET, PRIVATE.",
        "FIVE HUNDRED FEET. TURN RIGHT. DO NOT OVERTHINK IT.",
    )

    private val TURN_RIGHT_200 = listOf(
        "TWO HUNDRED FEET. TURN RIGHT. MOVE INTO THE CORRECT LANE.",
        "RIGHT TURN IN TWO HUNDRED FEET. I HOPE YOU ARE PAYING ATTENTION.",
        "TWO HUNDRED FEET, PRIVATE. RIGHT TURN IMMINENT.",
        "GET READY TO TURN RIGHT. TWO HUNDRED FEET. STAY FOCUSED.",
    )

    private val TURN_RIGHT_NOW = listOf(
        "TURN RIGHT NOW. MOVE IT.",
        "RIGHT TURN. NOW. DO NOT HESITATE.",
        "EXECUTE RIGHT TURN IMMEDIATELY. WHAT IS YOUR MALFUNCTION?",
        "TURN RIGHT. NOW. THIS IS NOT A SUGGESTION.",
    )

    private val TURN_SLIGHT_LEFT_500 = listOf(
        "BEAR SLIGHTLY LEFT IN FIVE HUNDRED FEET. SUBTLE, PRIVATE. DO NOT OVERCORRECT.",
        "SLIGHT LEFT IN FIVE HUNDRED FEET. STAY FOCUSED.",
        "IN FIVE HUNDRED FEET, VEER LEFT. KEEP IT CONTROLLED.",
    )

    private val TURN_SLIGHT_LEFT_NOW = listOf(
        "BEAR LEFT NOW. SLIGHT ADJUSTMENT. STAY SMOOTH.",
        "VEER LEFT. NOW. IN CONTROL.",
        "SLIGHT LEFT. EXECUTE NOW.",
    )

    private val TURN_SLIGHT_RIGHT_500 = listOf(
        "BEAR SLIGHTLY RIGHT IN FIVE HUNDRED FEET. EASY DOES IT.",
        "SLIGHT RIGHT IN FIVE HUNDRED FEET. DO NOT OVERSHOOT.",
        "IN FIVE HUNDRED FEET, VEER RIGHT. MINOR CORRECTION.",
    )

    private val TURN_SLIGHT_RIGHT_NOW = listOf(
        "BEAR RIGHT NOW. SLIGHT MOVE. STAY IN CONTROL.",
        "VEER RIGHT. NOW.",
        "SLIGHT RIGHT. EXECUTE.",
    )

    private val CONTINUE = listOf(
        "CONTINUE STRAIGHT. TRY NOT TO GET LOST ON A STRAIGHT ROAD.",
        "MAINTAIN YOUR CURRENT HEADING. DO NOT DO ANYTHING STUPID.",
        "STAY ON THIS ROAD. STRAIGHTFORWARD ENOUGH FOR YOU, PRIVATE?",
        "DRIVE STRAIGHT AHEAD. EVEN YOU CAN HANDLE THIS.",
    )

    private val RECALCULATING_1 = listOf(
        "YOU MISSED THE TURN. OUTSTANDING. RECALCULATING YOUR ROUTE.",
        "MISSED THE TURN. I AM RECALCULATING. THIS IS DEEPLY DISAPPOINTING.",
        "WRONG WAY, PRIVATE. RECALCULATING. TRY TO KEEP UP THIS TIME.",
        "YOU WENT THE WRONG DIRECTION. RECALCULATING. PAY ATTENTION.",
    )

    private val RECALCULATING_2 = listOf(
        "AGAIN. YOU DID IT AGAIN. RECALCULATING. I HAVE SEEN ROCKS WITH BETTER SPATIAL AWARENESS.",
        "SECOND MISSED TURN. I DO NOT KNOW WHY I AM SURPRISED. RECALCULATING.",
        "YOU MISSED ANOTHER TURN. I AM STARTING TO QUESTION YOUR ABILITY TO OPERATE A VEHICLE. RECALCULATING.",
        "ANOTHER WRONG TURN. ANOTHER RECALCULATION. ARE YOU DOING THIS ON PURPOSE?",
    )

    private val RECALCULATING_3PLUS = listOf(
        "I CANNOT BELIEVE WHAT I AM WITNESSING. ROUTE. RECALCULATED. AGAIN.",
        "THREE MISSED TURNS. I WANT YOU TO THINK ABOUT WHAT YOU HAVE DONE. RECALCULATING.",
        "AT THIS POINT I QUESTION WHETHER YOU HAVE EVER OPERATED A VEHICLE. RECALCULATING.",
        "I HAVE NEVER. IN MY CAREER. NEVER. RECALCULATING. AGAIN.",
    )

    private val SPEED_WARNING = listOf(
        "YOU ARE EXCEEDING THE SPEED LIMIT. THIS IS NOT THE INDY 500. SLOW DOWN, PRIVATE.",
        "REDUCE YOUR SPEED IMMEDIATELY. YOU ARE OVER THE LIMIT. THIS IS NOT A RACE.",
        "SPEED LIMIT EXISTS FOR A REASON. COMPLY WITH IT. NOW.",
        "YOU ARE GOING TOO FAST. SLOW DOWN BEFORE I SLOW YOU DOWN.",
    )

    private val IDLE_TAUNT = listOf(
        "WHY ARE YOU STOPPED? MOVE THIS VEHICLE IMMEDIATELY.",
        "YOU HAVE BEEN STATIONARY FOR TOO LONG. THIS IS UNACCEPTABLE. MOVE OUT.",
        "I DO NOT KNOW WHAT YOU ARE WAITING FOR, BUT IT IS NOT WORTH IT. DRIVE.",
        "IS THERE A PROBLEM? BECAUSE FROM WHERE I SIT YOU ARE DOING ABSOLUTELY NOTHING. MOVE.",
    )

    private val ARRIVAL = listOf(
        "YOU HAVE REACHED YOUR DESTINATION. IT TOOK YOU LONG ENOUGH. DISMISSED.",
        "OBJECTIVE REACHED. DO NOT CELEBRATE. YOU ARE JUST WHERE YOU WERE SUPPOSED TO BE. AT EASE.",
        "YOU HAVE ARRIVED. SOMEHOW. AGAINST ALL ODDS. DISMISSED.",
        "MISSION COMPLETE. TRY NOT TO GET LOST ON THE WAY BACK.",
        "DESTINATION REACHED. I WANT YOU TO REFLECT ON HOW MANY TURNS YOU MISSED TODAY. AT EASE.",
    )
}
