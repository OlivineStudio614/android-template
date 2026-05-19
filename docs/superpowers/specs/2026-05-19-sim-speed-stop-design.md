# Sim Speed Control & Stop — Design Spec
**Date:** 2026-05-19

## Problem

Two issues with simulation mode:

1. Playback speed is hardcoded at `2.0` — no way to adjust it during a sim run, causing the app to spam turn commands.
2. No intuitive way to stop the sim mid-run. The ABORT button stops navigation but leaves sim mode ON, requiring a second tap on SIM ON/OFF.

## Goals

- Let the user adjust sim playback speed in real-time during navigation.
- Let the user stop the sim in one tap, returning to the idle search state with sim mode OFF.

## Out of Scope

- Pause/resume (only stop is needed)
- Persisting speed across sessions
- Speed adjustment before starting navigation (slider only shown during active sim navigation)

---

## Architecture

**`NavigationViewModel`**

- Add `private val _simPlaybackSpeed = MutableStateFlow(1.0f)` and expose `val simPlaybackSpeed: StateFlow<Float>`.
- Add `fun setSimSpeed(speed: Float)` — updates `_simPlaybackSpeed` and calls `nav.mapboxReplayer.playbackSpeed(speed.toDouble())`. Safe to call anytime; no-op if replayer is inactive.
- In `stopNavigation()`, add: `if (_simulationMode.value) { _simulationMode.value = false; _simPlaybackSpeed.value = 1.0f }` — resets both flags so the next sim session starts fresh.
- Remove `SIMULATION_PLAYBACK_SPEED` constant (no longer needed; speed is user-controlled from first play).

**No new classes required.**

---

## UI

### During `NavigationState.Navigating` with sim ON

A `SimSpeedSlider` composable appears below `SpeedStrip`, above the map:

- `Slider` range: `0.5f` to `5.0f`
- Label shows current value formatted as `"1.5×"`
- Styled with amber thumb / olive track to match existing theme
- Calls `onSpeedChange(speed)` → `viewModel.setSimSpeed(speed)`

The ABORT button label switches to `"ABORT SIM"` when `simulationMode == true`. Same position, same style, same callback (`viewModel.stopNavigation()`).

### During `NavigationState.Idle` with sim ON

No slider shown — the replayer isn't active yet. Speed is already reset to `1.0f`.

---

## Data Flow

```
User drags slider
  → onSpeedChange(speed)
  → viewModel.setSimSpeed(speed)
  → _simPlaybackSpeed.value = speed
  → nav.mapboxReplayer.playbackSpeed(speed.toDouble())

User taps ABORT SIM
  → viewModel.stopNavigation()
  → nav.mapboxReplayer.stop() + clearEvents()
  → nav.setNavigationRoutes(emptyList())
  → _simulationMode.value = false
  → _simPlaybackSpeed.value = 1.0f
  → _navState = Idle
  → UI returns to SearchBar
```

---

## Files Changed

| File | Change |
|------|--------|
| `NavigationViewModel.kt` | Add `simPlaybackSpeed` state, `setSimSpeed()`, update `stopNavigation()`, remove constant |
| `MapScreen.kt` | Collect `simPlaybackSpeed`, add `SimSpeedSlider` in `Navigating` block, pass `simulationMode` to `StopNavigationButton` for label |

---

## Error Handling

- Mapbox replayer handles out-of-range speeds gracefully; slider range (0.5–5.0) stays well within safe bounds.
- `setSimSpeed()` guards with `mapboxNavigation ?: return` — no crash if called before nav is ready.
