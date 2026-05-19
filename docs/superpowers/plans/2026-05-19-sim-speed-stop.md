# Sim Speed Control & Stop Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a real-time speed slider to simulation mode and make the stop button kill sim mode in one tap.

**Architecture:** `simPlaybackSpeed` StateFlow lives in `NavigationViewModel` alongside `simulationMode`. `setSimSpeed()` updates it and calls the Mapbox replayer live. `stopNavigation()` gains two lines to reset both sim flags when sim is active. `MapScreen` collects the new state and renders a `SimSpeedSlider` below the turn card during sim navigation, plus renames the ABORT button to ABORT SIM.

**Tech Stack:** Kotlin, Jetpack Compose Material3, Mapbox Navigation SDK, MockK, kotlinx-coroutines-test

---

## File Map

| File | Action | Responsibility |
|------|--------|---------------|
| `app/src/main/java/com/olivinestudio614/drillnav/navigation/NavigationViewModel.kt` | Modify | Add `simPlaybackSpeed` state, `setSimSpeed()`, reset logic in `stopNavigation()`, remove constant |
| `app/src/main/java/com/olivinestudio614/drillnav/ui/screens/MapScreen.kt` | Modify | Collect `simPlaybackSpeed`, add `SimSpeedSlider` composable, update `StopNavigationButton` label |
| `app/src/test/java/com/olivinestudio614/drillnav/navigation/NavigationViewModelTest.kt` | Create | Unit tests for speed state and sim reset behavior |

---

## Task 1: ViewModel — add `simPlaybackSpeed` state and `setSimSpeed()`

**Files:**
- Modify: `app/src/main/java/com/olivinestudio614/drillnav/navigation/NavigationViewModel.kt`
- Create: `app/src/test/java/com/olivinestudio614/drillnav/navigation/NavigationViewModelTest.kt`

- [ ] **Step 1: Write the failing tests**

Create `app/src/test/java/com/olivinestudio614/drillnav/navigation/NavigationViewModelTest.kt`:

```kotlin
package com.olivinestudio614.drillnav.navigation

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

@OptIn(ExperimentalCoroutinesApi::class)
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
```

- [ ] **Step 2: Run tests to confirm they fail**

```bash
./gradlew :app:test --tests "com.olivinestudio614.drillnav.navigation.NavigationViewModelTest" 2>&1 | tail -20
```

Expected: FAIL — `simPlaybackSpeed` property does not exist yet.

- [ ] **Step 3: Add `simPlaybackSpeed` state and `setSimSpeed()` to ViewModel**

In `NavigationViewModel.kt`, add these two members after the `_simulationMode` block (after line 58):

```kotlin
private val _simPlaybackSpeed = MutableStateFlow(1.0f)
val simPlaybackSpeed: StateFlow<Float> = _simPlaybackSpeed
```

Add this function after `toggleSimulation()` (after line 90):

```kotlin
fun setSimSpeed(speed: Float) {
    _simPlaybackSpeed.value = speed
    mapboxNavigation?.mapboxReplayer?.playbackSpeed(speed.toDouble())
}
```

In `startNavigation()`, replace the hardcoded speed line (currently `nav.mapboxReplayer.playbackSpeed(SIMULATION_PLAYBACK_SPEED)`):

```kotlin
nav.mapboxReplayer.playbackSpeed(_simPlaybackSpeed.value.toDouble())
```

Remove `const val SIMULATION_PLAYBACK_SPEED = 2.0` from the companion object at line 292.

- [ ] **Step 4: Run tests to confirm they pass**

```bash
./gradlew :app:test --tests "com.olivinestudio614.drillnav.navigation.NavigationViewModelTest" 2>&1 | tail -20
```

Expected: 3 tests PASS.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/olivinestudio614/drillnav/navigation/NavigationViewModel.kt \
        app/src/test/java/com/olivinestudio614/drillnav/navigation/NavigationViewModelTest.kt
git commit -m "feat: add simPlaybackSpeed state and setSimSpeed to NavigationViewModel"
```

---

## Task 2: ViewModel — reset sim state in `stopNavigation()`

**Files:**
- Modify: `app/src/main/java/com/olivinestudio614/drillnav/navigation/NavigationViewModel.kt`
- Modify: `app/src/test/java/com/olivinestudio614/drillnav/navigation/NavigationViewModelTest.kt`

- [ ] **Step 1: Add the failing tests**

Append these two tests to `NavigationViewModelTest.kt` inside the class body:

```kotlin
@Test
fun `stopNavigation resets simulationMode to false when sim was on`() {
    val mockReplayer = mockk<MapboxReplayer>(relaxed = true)
    val mockNav = mockk<MapboxNavigation>(relaxed = true) {
        every { mapboxReplayer } returns mockReplayer
    }
    val vm = NavigationViewModel()
    vm.setMapboxNavigation(mockNav)
    vm.toggleSimulation()           // sim ON (nav state is Idle)
    vm.stopNavigation()
    assertEquals(false, vm.simulationMode.value)
}

@Test
fun `stopNavigation resets simPlaybackSpeed to 1_0f when sim was on`() {
    val mockReplayer = mockk<MapboxReplayer>(relaxed = true)
    val mockNav = mockk<MapboxNavigation>(relaxed = true) {
        every { mapboxReplayer } returns mockReplayer
    }
    val vm = NavigationViewModel()
    vm.setMapboxNavigation(mockNav)
    vm.toggleSimulation()           // sim ON
    vm.setSimSpeed(3.5f)
    vm.stopNavigation()
    assertEquals(1.0f, vm.simPlaybackSpeed.value)
}
```

- [ ] **Step 2: Run tests to confirm they fail**

```bash
./gradlew :app:test --tests "com.olivinestudio614.drillnav.navigation.NavigationViewModelTest" 2>&1 | tail -20
```

Expected: 2 new tests FAIL — `stopNavigation` does not yet reset sim state.

- [ ] **Step 3: Update `stopNavigation()` to reset sim flags**

In `NavigationViewModel.kt`, find the private `stopNavigation(nav: MapboxNavigation)` function (around line 179). Replace the body:

```kotlin
private fun stopNavigation(nav: MapboxNavigation) {
    if (_simulationMode.value) {
        nav.mapboxReplayer.stop()
        nav.mapboxReplayer.clearEvents()
        _simulationMode.value = false
        _simPlaybackSpeed.value = 1.0f
    }
    nav.setNavigationRoutes(emptyList())
    idleController.stop()
    _navState.value = NavigationState.Idle
}
```

- [ ] **Step 4: Run all ViewModel tests to confirm they pass**

```bash
./gradlew :app:test --tests "com.olivinestudio614.drillnav.navigation.NavigationViewModelTest" 2>&1 | tail -20
```

Expected: 5 tests PASS.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/olivinestudio614/drillnav/navigation/NavigationViewModel.kt \
        app/src/test/java/com/olivinestudio614/drillnav/navigation/NavigationViewModelTest.kt
git commit -m "feat: reset sim mode and speed in stopNavigation"
```

---

## Task 3: MapScreen — add `SimSpeedSlider` and update ABORT button

All MapScreen changes land in one task to keep the file compilable at each step.

**Files:**
- Modify: `app/src/main/java/com/olivinestudio614/drillnav/ui/screens/MapScreen.kt`

- [ ] **Step 1: Update `StopNavigationButton` to accept `isSimActive`**

In `MapScreen.kt`, replace the existing `StopNavigationButton` composable (around line 275):

```kotlin
@Composable
private fun StopNavigationButton(
    onStop: () -> Unit,
    isSimActive: Boolean = false,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onStop,
        shape = RectangleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = ArmyGreenDark,
            contentColor = AmberAlert
        ),
        modifier = modifier
    ) {
        Text(
            if (isSimActive) "ABORT SIM" else "ABORT",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
```

- [ ] **Step 2: Add `SimSpeedSlider` composable**

Add this private composable at the bottom of `MapScreen.kt`, before the final closing brace of the file:

```kotlin
@Composable
private fun SimSpeedSlider(
    speed: Float,
    onSpeedChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(ArmyGreenDark)
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "SIM",
            style = MaterialTheme.typography.labelSmall.copy(color = AmberAlert),
            modifier = Modifier.width(28.dp)
        )
        Slider(
            value = speed,
            onValueChange = onSpeedChange,
            valueRange = 0.5f..5.0f,
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(
                thumbColor = AmberAlert,
                activeTrackColor = AmberAlert,
                inactiveTrackColor = ArmyGreenDark
            )
        )
        Text(
            text = "${"%.1f".format(speed)}×",
            style = MaterialTheme.typography.labelSmall.copy(color = AmberAlert),
            modifier = Modifier.width(36.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
}
```

- [ ] **Step 3: Collect `simPlaybackSpeed` state in `MapScreen`**

In `MapScreen.kt`, add this line after `val simulationMode` (currently line 46):

```kotlin
val simSpeed by viewModel.simPlaybackSpeed.collectAsState()
```

- [ ] **Step 4: Wire slider and updated button into the `Navigating` block**

In `MapScreen.kt`, replace the entire `is NavigationState.Navigating` branch (around line 142):

```kotlin
is NavigationState.Navigating -> {
    Column(modifier = Modifier.align(Alignment.TopCenter).statusBarsPadding()) {
        TurnInstructionCard(
            instruction = instruction,
            distanceRemaining = distanceRemaining
        )
        SpeedStrip(
            currentSpeedMph = speedMph,
            speedLimitMph = speedLimit
        )
        if (simulationMode) {
            SimSpeedSlider(
                speed = simSpeed,
                onSpeedChange = { viewModel.setSimSpeed(it) }
            )
        }
    }
    StopNavigationButton(
        onStop = { viewModel.stopNavigation() },
        isSimActive = simulationMode,
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(16.dp)
    )
}
```

- [ ] **Step 5: Build clean**

```bash
./gradlew :app:assembleDebug 2>&1 | grep -E "error:" | head -10
```

Expected: no errors.

- [ ] **Step 6: Run all tests**

```bash
./gradlew :app:test 2>&1 | tail -20
```

Expected: all tests PASS.

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/olivinestudio614/drillnav/ui/screens/MapScreen.kt
git commit -m "feat: add SimSpeedSlider and ABORT SIM button to MapScreen"
```
