package com.olivinestudio614.hartmannav.hartman

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class IdleTauntController(
    private val scope: CoroutineScope,
    private val idleThresholdMs: Long = 60_000L,
    private val onIdle: () -> Unit
) {
    private var job: Job? = null

    fun start() {
        scheduleIdle()
    }

    fun stop() {
        job?.cancel()
        job = null
    }

    fun notifyMovement() {
        job?.cancel()
        scheduleIdle()
    }

    private fun scheduleIdle() {
        job = scope.launch {
            delay(idleThresholdMs)
            onIdle()
        }
    }
}
