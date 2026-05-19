package com.olivinestudio614.drillnav.navigation

import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.replay.MapboxReplayer
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalPreviewMapboxNavigationAPI::class)
class NavigationViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `simPlaybackSpeed default value is 1_0f`() {
        val vm = NavigationViewModel()
        assertEquals(1.0f, vm.simPlaybackSpeed.value)
    }

    @Test
    fun `setSimSpeed updates simPlaybackSpeed flow`() {
        val vm = NavigationViewModel()
        vm.setSimSpeed(2.5f)
        assertEquals(2.5f, vm.simPlaybackSpeed.value)
    }

    @Test
    fun `setSimSpeed calls replayer playbackSpeed when nav is set`() {
        val mockReplayer = mockk<MapboxReplayer>(relaxed = true)
        val mockNav = mockk<MapboxNavigation>(relaxed = true) {
            every { mapboxReplayer } returns mockReplayer
        }
        val vm = NavigationViewModel()
        vm.setMapboxNavigation(mockNav)
        vm.setSimSpeed(3.0f)
        verify { mockReplayer.playbackSpeed(3.0) }
    }
}
