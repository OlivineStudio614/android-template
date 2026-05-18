package com.olivinestudio614.hartmannav.hartman

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class IdleTauntControllerTest {

    @Test
    fun `onIdle fires after 60 seconds of no movement`() = runTest {
        var idleCount = 0
        val controller = IdleTauntController(
            scope = this,
            onIdle = { idleCount++ }
        )
        controller.start()
        advanceTimeBy(61_000)
        assertEquals(1, idleCount)
        controller.stop()
    }

    @Test
    fun `notifyMovement resets timer and prevents idle callback`() = runTest {
        var idleCount = 0
        val controller = IdleTauntController(
            scope = this,
            onIdle = { idleCount++ }
        )
        controller.start()
        advanceTimeBy(40_000)
        controller.notifyMovement()
        advanceTimeBy(40_000)
        assertEquals(0, idleCount)
        controller.stop()
    }

    @Test
    fun `stop cancels pending idle`() = runTest {
        var idleCount = 0
        val controller = IdleTauntController(
            scope = this,
            onIdle = { idleCount++ }
        )
        controller.start()
        advanceTimeBy(50_000)
        controller.stop()
        advanceTimeBy(20_000)
        assertEquals(0, idleCount)
    }
}
