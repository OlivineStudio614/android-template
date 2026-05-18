# HartmanNav — Design Spec
**Date:** 2026-05-18
**Status:** Approved

---

## Overview

A turn-by-turn Android navigation app that uses a pre-scripted drill instructor voice instead of a standard TTS voice. The app functions like a standard navigation app (Google Maps-style) but all audio instructions are Hartman-style military drill sergeant rants. The UI is military-themed (olive drab, stencil fonts, tactical UI elements).

**Target:** Personal use first, Play Store (free) later. Store version uses a generic "Drill Instructor" persona — not the Hartman name/character — to avoid IP concerns.

---

## Tech Stack

- **Platform:** Android (Kotlin + Jetpack Compose), minSdk 24
- **Maps + Navigation:** Mapbox Navigation SDK (free tier: 25,000 sessions/month)
- **Voice:** Android built-in `TextToSpeech` — no external API, works offline
- **Phrase library:** Pre-scripted Kotlin object, ~80–100 phrases across all event buckets
- **Architecture:** MVVM — `NavigationViewModel` bridges Mapbox events to Hartman/TTS layer

---

## Architecture

### Layers

```
UI Layer (Compose + MilitaryTheme)
        ↓ user actions / state
NavigationViewModel
        ↓ navigation events        ↓ phrase requests
Mapbox Navigation SDK       HartmanPhraseLibrary
                                    ↓
                              HartmanTTS (Android TTS wrapper)
```

### Key Components

| Component | Responsibility |
|---|---|
| `MainActivity` | Entry point, location permission request |
| `MapScreen` | Full-screen Compose map, search bar, nav overlay |
| `NavigationViewModel` | Navigation state, bridges Mapbox events to Hartman layer |
| `HartmanPhraseLibrary` | Kotlin object, phrase buckets per event, random selection |
| `HartmanTTS` | Wraps Android `TextToSpeech`, queues phrases, sets speech rate |
| `MilitaryTheme` | Compose theme — palette, typography, shapes |

---

## Screen Flow

```
Launch
  → Location Permission Request
  → Map Home Screen
    → Enter Destination (search)
      → Route Preview
        → Active Navigation
          → Arrival Screen
```

---

## UI Design

### Map Home Screen
- Full-screen Mapbox map using Mapbox Dark style (or custom military style override)
- Top bar: app name in stencil font (`SGT. NAV`)
- Bottom sheet: destination search bar labeled `ENTER TARGET COORDINATES`
- User location puck: crosshair pin

### Active Navigation Overlay
- **Top card** (tactical briefing style): turn arrow + street name + distance
  - Example: `⬅ TURN LEFT — MAIN ST — IN 0.3 MI`
- **Bottom strip**: current speed vs speed limit; turns red when over limit
- Map auto-follows heading (standard navigation tracking behavior)

### Arrival Screen
- Full overlay with `OBJECTIVE REACHED` in large stencil font
- Subtext: trip distance + elapsed time
- Dismiss button labeled `AT EASE`

### Visual Theme
- **Palette:** Olive drab background, army green accents, amber highlights, white text
- **Font:** Special Elite (Google Fonts) for headings/labels; standard sans for map data
- **Shapes:** Hard edges, no rounded corners — military utilitarian

---

## Phrase System

### Event Types

| Event | Trigger |
|---|---|
| `TripStart` | Navigation begins |
| `DistanceAnnouncement` | 500ft, 200ft, and "now" before each turn |
| `TurnLeft` / `TurnRight` | Left/right turn instruction |
| `TurnSlightLeft` / `TurnSlightRight` | Slight turn instruction |
| `Continue` | Long straight stretch — time-based trigger (every ~3 min of no turn events) |
| `Recalculating_1` | First missed turn |
| `Recalculating_2` | Second missed turn |
| `Recalculating_3Plus` | Third+ missed turn (escalating fury) |
| `SpeedWarning` | User exceeds posted speed limit |
| `IdleTaunt` | No movement for 60+ seconds |
| `Arrival` | Destination reached |

### Phrase Selection
- Each bucket contains 4–5 phrases
- One is selected randomly on each trigger
- No repeat of the same phrase twice in a row (simple last-used tracking)

### Sample Phrases

| Event | Example |
|---|---|
| `TripStart` | `LISTEN UP. I WILL GUIDE YOU TO YOUR DESTINATION. TRY NOT TO DISAPPOINT ME.` |
| `TurnLeft (500ft)` | `IN 500 FEET YOU WILL TURN LEFT. DO NOT MISS IT LIKE THE WORTHLESS MAGGOT YOU ARE.` |
| `TurnRight (now)` | `TURN RIGHT NOW. NOW. ARE YOU DEAF, PRIVATE?` |
| `Recalculating_1` | `YOU MISSED THE TURN. OUTSTANDING. RECALCULATING.` |
| `Recalculating_2` | `AGAIN. YOU DID IT AGAIN. I HAVE SEEN ROCKS NAVIGATE BETTER THAN YOU.` |
| `Recalculating_3Plus` | `I CANNOT BELIEVE WHAT I AM WITNESSING. ROUTE. RECALCULATED. AGAIN.` |
| `SpeedWarning` | `YOU ARE EXCEEDING THE SPEED LIMIT. THIS IS NOT THE INDY 500, THIS IS MY ROAD.` |
| `IdleTaunt` | `WHY. ARE. YOU. STOPPED. MOVE THIS VEHICLE IMMEDIATELY.` |
| `Arrival` | `YOU HAVE REACHED YOUR OBJECTIVE. IT TOOK YOU LONG ENOUGH. DISMISSED.` |

Full phrase library (~80–100 entries) to be written during implementation.

### TTS Settings
- Speech rate: 1.15x (slightly faster, drill instructor energy)
- Pitch: default (no modification needed)
- Queue mode: `QUEUE_ADD` — phrases complete before next begins, no interruption mid-sentence

---

## Permissions Required

- `ACCESS_FINE_LOCATION` — GPS navigation
- `ACCESS_COARSE_LOCATION` — fallback
- `FOREGROUND_SERVICE` — keep navigation running when screen is off
- `INTERNET` — Mapbox tile and routing data

---

## Cost Summary

| Item | Cost |
|---|---|
| Mapbox SDK (personal use) | $0/month |
| Android TTS | $0 |
| Phrase library | $0 |
| Google Play Developer account (when ready) | $25 one-time |
| **Total to build and use personally** | **$0** |

---

## Store Considerations (Future)

- Rename persona to generic "Drill Instructor" — do not use "Hartman" or reference Full Metal Jacket
- App name options: `SGT. NAV`, `Drill Nav`, `Field Commander` 
- Release as free app to avoid commercial IP scrutiny
- Standard Play Store review: 1–3 days

---

## Out of Scope (This Version)

- iOS support
- Offline maps (Mapbox supports this — future enhancement)
- Custom voice cloning (ElevenLabs — future enhancement)
- LLM-generated dynamic phrases
- User-configurable phrase intensity levels
- CarPlay / Android Auto integration
