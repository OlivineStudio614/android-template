# HartmanNav Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a turn-by-turn Android navigation app with Mapbox routing and a pre-scripted drill-instructor voice layer replacing standard TTS navigation prompts, wrapped in a military-themed Compose UI.

**Architecture:** MVVM — `NavigationViewModel` registers Mapbox observers (route progress, off-route, arrival, location/speed), translates events via `HartmanEventMapper` and `HartmanPhraseLibrary` into phrases, and speaks them through `HartmanTTS`. UI is three Compose screens (Map, NavigationOverlay, Arrival) driven by `StateFlow` from the ViewModel.

**Tech Stack:** Kotlin 2.2 · Jetpack Compose · Mapbox Maps SDK v11 · Mapbox NavigationCore SDK v3 · Android `TextToSpeech` · Coroutines · JUnit4 · MockK

> **⚠️ Verify Mapbox versions before starting:** Check latest at https://docs.mapbox.com/android/maps/guides/install/ and https://docs.mapbox.com/android/navigation/guides/get-started/install/. Replace `11.8.0` / `3.7.0` with the current stable releases.

---

## File Map

**Create:**
```
app/src/main/java/com/olivinestudio614/hartmannav/
  HartmanNavApplication.kt
  MainActivity.kt
  hartman/
    HartmanEvent.kt
    HartmanPhraseLibrary.kt
    HartmanEventMapper.kt
    HartmanTTS.kt
    IdleTauntController.kt
  navigation/
    NavigationState.kt
    NavigationViewModel.kt
  ui/theme/
    Color.kt
    Type.kt
    Theme.kt
  ui/screens/
    MapScreen.kt
    NavigationOverlay.kt
    ArrivalScreen.kt

app/src/test/java/com/olivinestudio614/hartmannav/
  hartman/
    HartmanPhraseLibraryTest.kt
    HartmanEventMapperTest.kt
    IdleTauntControllerTest.kt
```

**Modify:**
```
gradle/libs.versions.toml
settings.gradle.kts
app/build.gradle.kts
app/src/main/AndroidManifest.xml
app/src/main/res/values/strings.xml
local.properties  (add Mapbox tokens — gitignored)
```

**Delete (replaced by hartmannav package):**
```
app/src/main/java/com/olivinestudio614/template/
app/src/test/java/com/olivinestudio614/template/
app/src/androidTest/java/com/olivinestudio614/template/
```

---

## Task 1: Rename Package and Project

**Files:**
- Delete: `app/src/main/java/com/olivinestudio614/template/` (all files)
- Delete: `app/src/test/java/com/olivinestudio614/template/`
- Delete: `app/src/androidTest/java/com/olivinestudio614/template/`
- Modify: `app/build.gradle.kts`
- Modify: `settings.gradle.kts`
- Modify: `app/src/main/AndroidManifest.xml`
- Modify: `app/src/main/res/values/strings.xml`

- [ ] **Step 1: Delete template source directories**

```bash
rm -rf app/src/main/java/com/olivinestudio614/template
rm -rf app/src/test/java/com/olivinestudio614/template
rm -rf app/src/androidTest/java/com/olivinestudio614/template
mkdir -p app/src/main/java/com/olivinestudio614/hartmannav
mkdir -p app/src/test/java/com/olivinestudio614/hartmannav
mkdir -p app/src/androidTest/java/com/olivinestudio614/hartmannav
```

- [ ] **Step 2: Update `app/build.gradle.kts` — namespace and applicationId**

Replace:
```kotlin
namespace = "com.olivinestudio614.template"
```
With:
```kotlin
namespace = "com.olivinestudio614.hartmannav"
```

Replace:
```kotlin
applicationId = "com.olivinestudio614.template"
```
With:
```kotlin
applicationId = "com.olivinestudio614.hartmannav"
```

- [ ] **Step 3: Update `settings.gradle.kts` — root project name**

Replace:
```kotlin
rootProject.name = "template"
```
With:
```kotlin
rootProject.name = "HartmanNav"
```

- [ ] **Step 4: Update `app/src/main/AndroidManifest.xml`**

Replace the entire file with:
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <application
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.HartmanNav">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:label="@string/app_name"
            android:theme="@style/Theme.HartmanNav">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>
```

- [ ] **Step 5: Update `app/src/main/res/values/strings.xml`**

```xml
<resources>
    <string name="app_name">SGT. NAV</string>
</resources>
```

- [ ] **Step 6: Update `app/src/main/res/values/themes.xml`** — rename theme reference

Replace:
```xml
<style name="Theme.Template"
```
With:
```xml
<style name="Theme.HartmanNav"
```

- [ ] **Step 7: Create a placeholder `MainActivity.kt` so the project compiles**

Create `app/src/main/java/com/olivinestudio614/hartmannav/MainActivity.kt`:
```kotlin
package com.olivinestudio614.hartmannav

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import com.olivinestudio614.hartmannav.ui.theme.HartmanNavTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HartmanNavTheme {
                Text("SGT. NAV — STAND BY")
            }
        }
    }
}
```

- [ ] **Step 8: Create stub theme so placeholder compiles**

Create `app/src/main/java/com/olivinestudio614/hartmannav/ui/theme/Theme.kt`:
```kotlin
package com.olivinestudio614.hartmannav.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun HartmanNavTheme(content: @Composable () -> Unit) {
    MaterialTheme(content = content)
}
```

- [ ] **Step 9: Sync project and verify it compiles**

In Android Studio: **File → Sync Project with Gradle Files**, then **Build → Make Project**.
Expected: BUILD SUCCESSFUL — no errors.

- [ ] **Step 10: Commit**

```bash
git add -A
git commit -m "refactor: rename package from template to hartmannav"
```

---

## Task 2: Mapbox SDK Setup

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `settings.gradle.kts`
- Modify: `app/build.gradle.kts`
- Modify: `local.properties`

- [ ] **Step 1: Create Mapbox account and get tokens**

1. Go to https://account.mapbox.com → sign up / sign in
2. Copy your **Public Token** (starts with `pk.`) — this goes in the app
3. Create a **Secret Token** (starts with `sk.`) with `DOWNLOADS:READ` scope — this authenticates the Maven download

- [ ] **Step 2: Add tokens to `local.properties`** (this file is gitignored)

```properties
MAPBOX_ACCESS_TOKEN=pk.eyJ1IjoieW91ci10b2tlbi1oZXJlIn0.REPLACE_WITH_REAL_TOKEN
MAPBOX_DOWNLOADS_TOKEN=sk.eyJ1IjoieW91ci1zZWNyZXQtaGVyZSJ9.REPLACE_WITH_REAL_TOKEN
```

- [ ] **Step 3: Add Mapbox Maven repo to `settings.gradle.kts`**

Replace the entire file with:
```kotlin
import java.util.Properties

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

val localProps = Properties().also { props ->
    rootDir.resolve("local.properties").takeIf { it.exists() }
        ?.inputStream()?.use { props.load(it) }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven {
            url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
            authentication { create<BasicAuthentication>("basic") }
            credentials {
                username = "mapbox"
                password = localProps.getProperty("MAPBOX_DOWNLOADS_TOKEN") ?: ""
            }
        }
        google()
        mavenCentral()
    }
}

rootProject.name = "HartmanNav"
include(":app")
```

- [ ] **Step 4: Add Mapbox versions and libraries to `gradle/libs.versions.toml`**

Append to `[versions]`:
```toml
mapboxMaps = "11.8.0"
mapboxNavigationCore = "3.7.0"
lifecycleViewmodel = "2.8.7"
kotlinxCoroutines = "1.9.0"
mockk = "1.13.12"
```

Append to `[libraries]`:
```toml
mapbox-maps = { group = "com.mapbox.maps", name = "android", version.ref = "mapboxMaps" }
mapbox-maps-compose = { group = "com.mapbox.maps", name = "extension-compose", version.ref = "mapboxMaps" }
mapbox-navigation = { group = "com.mapbox.navigationcore", name = "android", version.ref = "mapboxNavigationCore" }
lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycleViewmodel" }
kotlinx-coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "kotlinxCoroutines" }
mockk = { group = "io.mockk", name = "mockk", version.ref = "mockk" }
kotlinx-coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "kotlinxCoroutines" }
```

- [ ] **Step 5: Update `app/build.gradle.kts`** — add imports, BuildConfig field, and dependencies

Replace the entire file with:
```kotlin
import java.util.Properties

val localProps = Properties().also { props ->
    rootProject.file("local.properties").takeIf { it.exists() }
        ?.inputStream()?.use { props.load(it) }
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.olivinestudio614.hartmannav"
    compileSdk {
        version = release(36) { minorApiLevel = 1 }
    }

    defaultConfig {
        applicationId = "com.olivinestudio614.hartmannav"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField(
            "String",
            "MAPBOX_ACCESS_TOKEN",
            "\"${localProps.getProperty("MAPBOX_ACCESS_TOKEN") ?: ""}\""
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.mapbox.maps)
    implementation(libs.mapbox.maps.compose)
    implementation(libs.mapbox.navigation)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.kotlinx.coroutines.android)
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
```

- [ ] **Step 6: Sync project and verify it compiles**

**File → Sync Project with Gradle Files**, then **Build → Make Project**.
Expected: BUILD SUCCESSFUL. If you see a 401 Unauthorized error from the Mapbox Maven repo, your `MAPBOX_DOWNLOADS_TOKEN` in `local.properties` is incorrect — regenerate it with `DOWNLOADS:READ` scope.

- [ ] **Step 7: Commit**

```bash
git add gradle/libs.versions.toml settings.gradle.kts app/build.gradle.kts
git commit -m "build: add Mapbox Maps and Navigation SDK dependencies"
```

---

## Task 3: HartmanEvent Sealed Class

**Files:**
- Create: `app/src/main/java/com/olivinestudio614/hartmannav/hartman/HartmanEvent.kt`

- [ ] **Step 1: Create `HartmanEvent.kt`**

```kotlin
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
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/olivinestudio614/hartmannav/hartman/HartmanEvent.kt
git commit -m "feat: add HartmanEvent sealed class"
```

---

## Task 4: HartmanPhraseLibrary + Tests

**Files:**
- Create: `app/src/main/java/com/olivinestudio614/hartmannav/hartman/HartmanPhraseLibrary.kt`
- Create: `app/src/test/java/com/olivinestudio614/hartmannav/hartman/HartmanPhraseLibraryTest.kt`

- [ ] **Step 1: Write the failing test**

Create `app/src/test/java/com/olivinestudio614/hartmannav/hartman/HartmanPhraseLibraryTest.kt`:
```kotlin
package com.olivinestudio614.hartmannav.hartman

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNotEquals
import org.junit.Test

class HartmanPhraseLibraryTest {

    @Test
    fun `all event types return a non-blank phrase`() {
        val events = listOf(
            HartmanEvent.TripStart,
            HartmanEvent.Turn.Left(HartmanEvent.Turn.Distance.FEET_500),
            HartmanEvent.Turn.Left(HartmanEvent.Turn.Distance.FEET_200),
            HartmanEvent.Turn.Left(HartmanEvent.Turn.Distance.NOW),
            HartmanEvent.Turn.Right(HartmanEvent.Turn.Distance.FEET_500),
            HartmanEvent.Turn.Right(HartmanEvent.Turn.Distance.FEET_200),
            HartmanEvent.Turn.Right(HartmanEvent.Turn.Distance.NOW),
            HartmanEvent.Turn.SlightLeft(HartmanEvent.Turn.Distance.FEET_500),
            HartmanEvent.Turn.SlightLeft(HartmanEvent.Turn.Distance.NOW),
            HartmanEvent.Turn.SlightRight(HartmanEvent.Turn.Distance.FEET_500),
            HartmanEvent.Turn.SlightRight(HartmanEvent.Turn.Distance.NOW),
            HartmanEvent.Continue,
            HartmanEvent.Recalculating(1),
            HartmanEvent.Recalculating(2),
            HartmanEvent.Recalculating(3),
            HartmanEvent.Recalculating(5),
            HartmanEvent.SpeedWarning,
            HartmanEvent.IdleTaunt,
            HartmanEvent.Arrival,
        )
        events.forEach { event ->
            val phrase = HartmanPhraseLibrary.phraseFor(event)
            assertFalse("Phrase for $event was blank", phrase.isBlank())
        }
    }

    @Test
    fun `same event called twice does not repeat phrase when pool has multiple entries`() {
        // Reset library state between checks by calling many times
        val seen = mutableSetOf<String>()
        repeat(20) {
            seen += HartmanPhraseLibrary.phraseFor(HartmanEvent.TripStart)
        }
        assert(seen.size > 1) { "Expected multiple different phrases, only got: $seen" }
    }

    @Test
    fun `consecutive calls for same event do not return the same phrase`() {
        val first = HartmanPhraseLibrary.phraseFor(HartmanEvent.Arrival)
        val second = HartmanPhraseLibrary.phraseFor(HartmanEvent.Arrival)
        assertNotEquals("Two consecutive calls returned the same phrase", first, second)
    }
}
```

- [ ] **Step 2: Run test — verify it fails**

Run: `./gradlew :app:test --tests "*.HartmanPhraseLibraryTest" --info`
Expected: FAILED — `HartmanPhraseLibrary` does not exist yet.

- [ ] **Step 3: Create `HartmanPhraseLibrary.kt` with full phrase library**

```kotlin
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
            HartmanEvent.Turn.Distance.FEET_500 -> TURN_SLIGHT_LEFT_500
            HartmanEvent.Turn.Distance.FEET_200 -> TURN_SLIGHT_LEFT_500
            HartmanEvent.Turn.Distance.NOW -> TURN_SLIGHT_LEFT_NOW
        }
        is HartmanEvent.Turn.SlightRight -> when (event.distance) {
            HartmanEvent.Turn.Distance.FEET_500 -> TURN_SLIGHT_RIGHT_500
            HartmanEvent.Turn.Distance.FEET_200 -> TURN_SLIGHT_RIGHT_500
            HartmanEvent.Turn.Distance.NOW -> TURN_SLIGHT_RIGHT_NOW
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
```

- [ ] **Step 4: Run tests — verify they pass**

Run: `./gradlew :app:test --tests "*.HartmanPhraseLibraryTest"`
Expected: 3 tests — BUILD SUCCESSFUL.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/olivinestudio614/hartmannav/hartman/HartmanPhraseLibrary.kt \
        app/src/test/java/com/olivinestudio614/hartmannav/hartman/HartmanPhraseLibraryTest.kt
git commit -m "feat: add HartmanPhraseLibrary with full phrase library and no-repeat logic"
```

---

## Task 5: HartmanEventMapper + Tests

**Files:**
- Create: `app/src/main/java/com/olivinestudio614/hartmannav/hartman/HartmanEventMapper.kt`
- Create: `app/src/test/java/com/olivinestudio614/hartmannav/hartman/HartmanEventMapperTest.kt`

The mapper translates Mapbox step data (maneuver type string, modifier string, distance in meters, and a set of already-announced distance thresholds for this step) into a `HartmanEvent.Turn`. It returns `null` when no announcement is needed.

- [ ] **Step 1: Write the failing test**

Create `app/src/test/java/com/olivinestudio614/hartmannav/hartman/HartmanEventMapperTest.kt`:
```kotlin
package com.olivinestudio614.hartmannav.hartman

import com.olivinestudio614.hartmannav.hartman.HartmanEvent.Turn.Distance
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HartmanEventMapperTest {

    @Test
    fun `left turn at 500ft threshold returns Left FEET_500`() {
        val event = HartmanEventMapper.mapTurnEvent(
            maneuverType = "turn",
            maneuverModifier = "left",
            distanceRemainingMeters = 140f,
            announced = emptySet()
        )
        assertEquals(HartmanEvent.Turn.Left(Distance.FEET_500), event)
    }

    @Test
    fun `left turn already announced at 500ft returns null`() {
        val event = HartmanEventMapper.mapTurnEvent(
            maneuverType = "turn",
            maneuverModifier = "left",
            distanceRemainingMeters = 140f,
            announced = setOf(Distance.FEET_500)
        )
        assertNull(event)
    }

    @Test
    fun `left turn at 200ft threshold returns Left FEET_200`() {
        val event = HartmanEventMapper.mapTurnEvent(
            maneuverType = "turn",
            maneuverModifier = "left",
            distanceRemainingMeters = 55f,
            announced = setOf(Distance.FEET_500)
        )
        assertEquals(HartmanEvent.Turn.Left(Distance.FEET_200), event)
    }

    @Test
    fun `left turn at now threshold returns Left NOW`() {
        val event = HartmanEventMapper.mapTurnEvent(
            maneuverType = "turn",
            maneuverModifier = "left",
            distanceRemainingMeters = 10f,
            announced = setOf(Distance.FEET_500, Distance.FEET_200)
        )
        assertEquals(HartmanEvent.Turn.Left(Distance.NOW), event)
    }

    @Test
    fun `right turn modifier returns Right event`() {
        val event = HartmanEventMapper.mapTurnEvent(
            maneuverType = "turn",
            maneuverModifier = "right",
            distanceRemainingMeters = 140f,
            announced = emptySet()
        )
        assertEquals(HartmanEvent.Turn.Right(Distance.FEET_500), event)
    }

    @Test
    fun `slight left modifier returns SlightLeft event`() {
        val event = HartmanEventMapper.mapTurnEvent(
            maneuverType = "turn",
            maneuverModifier = "slight left",
            distanceRemainingMeters = 140f,
            announced = emptySet()
        )
        assertEquals(HartmanEvent.Turn.SlightLeft(Distance.FEET_500), event)
    }

    @Test
    fun `straight modifier returns null`() {
        val event = HartmanEventMapper.mapTurnEvent(
            maneuverType = "continue",
            maneuverModifier = "straight",
            distanceRemainingMeters = 140f,
            announced = emptySet()
        )
        assertNull(event)
    }

    @Test
    fun `distance above 500ft threshold returns null`() {
        val event = HartmanEventMapper.mapTurnEvent(
            maneuverType = "turn",
            maneuverModifier = "left",
            distanceRemainingMeters = 300f,
            announced = emptySet()
        )
        assertNull(event)
    }
}
```

- [ ] **Step 2: Run test — verify it fails**

Run: `./gradlew :app:test --tests "*.HartmanEventMapperTest"`
Expected: FAILED — `HartmanEventMapper` does not exist yet.

- [ ] **Step 3: Create `HartmanEventMapper.kt`**

```kotlin
package com.olivinestudio614.hartmannav.hartman

import com.olivinestudio614.hartmannav.hartman.HartmanEvent.Turn.Distance

object HartmanEventMapper {

    private const val METERS_500FT = 152f
    private const val METERS_200FT = 61f
    private const val METERS_NOW = 15f

    fun mapTurnEvent(
        maneuverType: String?,
        maneuverModifier: String?,
        distanceRemainingMeters: Float,
        announced: Set<Distance>
    ): HartmanEvent.Turn? {
        val direction = resolveDirection(maneuverModifier) ?: return null

        val threshold = when {
            distanceRemainingMeters <= METERS_NOW && Distance.NOW !in announced -> Distance.NOW
            distanceRemainingMeters <= METERS_200FT && Distance.FEET_200 !in announced -> Distance.FEET_200
            distanceRemainingMeters <= METERS_500FT && Distance.FEET_500 !in announced -> Distance.FEET_500
            else -> return null
        }

        return when (direction) {
            Direction.LEFT -> HartmanEvent.Turn.Left(threshold)
            Direction.RIGHT -> HartmanEvent.Turn.Right(threshold)
            Direction.SLIGHT_LEFT -> HartmanEvent.Turn.SlightLeft(threshold)
            Direction.SLIGHT_RIGHT -> HartmanEvent.Turn.SlightRight(threshold)
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
```

- [ ] **Step 4: Run tests — verify they pass**

Run: `./gradlew :app:test --tests "*.HartmanEventMapperTest"`
Expected: 8 tests — BUILD SUCCESSFUL.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/olivinestudio614/hartmannav/hartman/HartmanEventMapper.kt \
        app/src/test/java/com/olivinestudio614/hartmannav/hartman/HartmanEventMapperTest.kt
git commit -m "feat: add HartmanEventMapper with distance threshold logic"
```

---

## Task 6: HartmanTTS

**Files:**
- Create: `app/src/main/java/com/olivinestudio614/hartmannav/hartman/HartmanTTS.kt`

Android `TextToSpeech` requires a `Context` and fires a callback when initialized. This class manages that lifecycle. No unit tests — TTS requires a device or emulator.

- [ ] **Step 1: Create `HartmanTTS.kt`**

```kotlin
package com.olivinestudio614.hartmannav.hartman

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class HartmanTTS(context: Context) {

    private var tts: TextToSpeech? = null
    private var ready = false
    private val queue = mutableListOf<String>()

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                tts?.setSpeechRate(1.15f)
                ready = true
                queue.forEach { speak(it) }
                queue.clear()
            }
        }
    }

    fun speak(text: String) {
        if (!ready) {
            queue += text
            return
        }
        tts?.speak(text, TextToSpeech.QUEUE_ADD, null, text.hashCode().toString())
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        ready = false
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/olivinestudio614/hartmannav/hartman/HartmanTTS.kt
git commit -m "feat: add HartmanTTS wrapper with initialization queue"
```

---

## Task 7: IdleTauntController + Tests

**Files:**
- Create: `app/src/main/java/com/olivinestudio614/hartmannav/hartman/IdleTauntController.kt`
- Create: `app/src/test/java/com/olivinestudio614/hartmannav/hartman/IdleTauntControllerTest.kt`

Fires an `onIdle` callback after 60 seconds of no movement. Resets whenever `notifyMovement()` is called.

- [ ] **Step 1: Write the failing test**

Create `app/src/test/java/com/olivinestudio614/hartmannav/hartman/IdleTauntControllerTest.kt`:
```kotlin
package com.olivinestudio614.hartmannav.hartman

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
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
```

- [ ] **Step 2: Run test — verify it fails**

Run: `./gradlew :app:test --tests "*.IdleTauntControllerTest"`
Expected: FAILED — `IdleTauntController` does not exist yet.

- [ ] **Step 3: Create `IdleTauntController.kt`**

```kotlin
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
```

- [ ] **Step 4: Run tests — verify they pass**

Run: `./gradlew :app:test --tests "*.IdleTauntControllerTest"`
Expected: 3 tests — BUILD SUCCESSFUL.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/olivinestudio614/hartmannav/hartman/IdleTauntController.kt \
        app/src/test/java/com/olivinestudio614/hartmannav/hartman/IdleTauntControllerTest.kt
git commit -m "feat: add IdleTauntController with coroutine timer and movement reset"
```

---

## Task 8: NavigationState + NavigationViewModel

**Files:**
- Create: `app/src/main/java/com/olivinestudio614/hartmannav/navigation/NavigationState.kt`
- Create: `app/src/main/java/com/olivinestudio614/hartmannav/navigation/NavigationViewModel.kt`

- [ ] **Step 1: Create `NavigationState.kt`**

```kotlin
package com.olivinestudio614.hartmannav.navigation

import com.mapbox.navigation.base.route.NavigationRoute

sealed class NavigationState {
    data object Idle : NavigationState()
    data object Searching : NavigationState()
    data class RoutePreview(val routes: List<NavigationRoute>) : NavigationState()
    data object Navigating : NavigationState()
    data object Arrived : NavigationState()
    data class Error(val message: String) : NavigationState()
}
```

- [ ] **Step 2: Create `NavigationViewModel.kt`**

```kotlin
package com.olivinestudio614.hartmannav.navigation

import android.content.Context
import android.location.Geocoder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.geojson.Point
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.navigation.base.route.RouterCallback
import com.mapbox.navigation.base.route.RouterFailure
import com.mapbox.navigation.base.route.RouterOrigin
import com.mapbox.navigation.base.trip.model.RouteProgress
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.MapboxNavigationApp
import com.mapbox.navigation.core.arrival.ArrivalObserver
import com.mapbox.navigation.core.directions.session.RoutesObserver
import com.mapbox.navigation.core.lifecycle.MapboxNavigationObserver
import com.mapbox.navigation.core.trip.session.LocationMatcherResult
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.navigation.core.trip.session.OffRouteObserver
import com.mapbox.navigation.core.trip.session.RouteProgressObserver
import com.mapbox.navigation.core.trip.session.SpeedLimitObserver
import com.olivinestudio614.hartmannav.BuildConfig
import com.olivinestudio614.hartmannav.hartman.HartmanEvent
import com.olivinestudio614.hartmannav.hartman.HartmanEventMapper
import com.olivinestudio614.hartmannav.hartman.HartmanPhraseLibrary
import com.olivinestudio614.hartmannav.hartman.HartmanTTS
import com.olivinestudio614.hartmannav.hartman.IdleTauntController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class NavigationViewModel : ViewModel() {

    private val _navState = MutableStateFlow<NavigationState>(NavigationState.Idle)
    val navState: StateFlow<NavigationState> = _navState

    private val _currentInstruction = MutableStateFlow("")
    val currentInstruction: StateFlow<String> = _currentInstruction

    private val _currentSpeedMph = MutableStateFlow(0f)
    val currentSpeedMph: StateFlow<Float> = _currentSpeedMph

    private val _speedLimitMph = MutableStateFlow(0f)
    val speedLimitMph: StateFlow<Float> = _speedLimitMph

    private val _distanceRemaining = MutableStateFlow("")
    val distanceRemaining: StateFlow<String> = _distanceRemaining

    private var tts: HartmanTTS? = null
    private var offRouteCount = 0
    private val announcedThisStep = mutableSetOf<HartmanEvent.Turn.Distance>()
    private var lastStepIndex = -1
    private var lastSpeedWarnTime = 0L
    private var speedLimitKph = 0f

    private val idleController = IdleTauntController(viewModelScope) {
        speakEvent(HartmanEvent.IdleTaunt)
    }

    fun initTts(context: Context) {
        if (tts == null) tts = HartmanTTS(context)
    }

    private var mapboxNavigation: MapboxNavigation? = null
    private var lastKnownOrigin: Point? = null

    fun setMapboxNavigation(nav: MapboxNavigation?) {
        mapboxNavigation = nav
    }

    fun searchDestination(context: Context, query: String) {
        _navState.value = NavigationState.Searching
        viewModelScope.launch {
            val point = withContext(Dispatchers.IO) { geocode(context, query) }
            if (point == null) {
                _navState.value = NavigationState.Error("Could not find: $query")
                return@launch
            }
            val nav = mapboxNavigation ?: run {
                _navState.value = NavigationState.Error("Navigation not ready")
                return@launch
            }
            requestRoute(nav, point)
        }
    }

    fun requestRoute(nav: MapboxNavigation, destination: Point) {
        val origin = lastKnownOrigin ?: return
        nav.requestRoutes(
            RouteOptions.builder()
                .applyDefaultNavigationOptions()
                .coordinatesList(listOf(origin, destination))
                .build(),
            object : RouterCallback {
                override fun onRoutesReady(
                    routes: List<NavigationRoute>,
                    routerOrigin: RouterOrigin
                ) {
                    _navState.value = NavigationState.RoutePreview(routes)
                }
                override fun onFailure(reasons: List<RouterFailure>, routeOptions: RouteOptions) {
                    _navState.value = NavigationState.Error("Route request failed")
                }
                override fun onCanceled(routeOptions: RouteOptions, routerOrigin: RouterOrigin) {}
            }
        )
    }

    fun startNavigation() = startNavigation(mapboxNavigation ?: return)
    fun stopNavigation() = stopNavigation(mapboxNavigation ?: return)

    private fun startNavigation(nav: MapboxNavigation) {
        val state = _navState.value as? NavigationState.RoutePreview ?: return
        nav.setNavigationRoutes(state.routes)
        nav.startTripSession()
        _navState.value = NavigationState.Navigating
        offRouteCount = 0
        announcedThisStep.clear()
        lastStepIndex = -1
        idleController.start()
        speakEvent(HartmanEvent.TripStart)
    }

    private fun stopNavigation(nav: MapboxNavigation) {
        nav.stopTripSession()
        nav.setNavigationRoutes(emptyList())
        idleController.stop()
        _navState.value = NavigationState.Idle
    }

    fun dismissArrival() {
        _navState.value = NavigationState.Idle
    }

    // ── Mapbox Observers ────────────────────────────────────────────────────

    val locationObserver = object : LocationObserver {
        override fun onNewRawLocation(rawLocation: android.location.Location) {
            lastKnownOrigin = Point.fromLngLat(rawLocation.longitude, rawLocation.latitude)
            val speedMs = rawLocation.speed
            _currentSpeedMph.value = speedMs * 2.237f
            idleController.notifyMovement()
            checkSpeedWarning()
        }
        override fun onNewLocationMatcherResult(result: LocationMatcherResult) {}
    }

    val routeProgressObserver = RouteProgressObserver { progress ->
        val legProgress = progress.currentLegProgress ?: return@RouteProgressObserver
        val stepProgress = legProgress.currentStepProgress ?: return@RouteProgressObserver
        val stepIndex = legProgress.stepIndex

        if (stepIndex != lastStepIndex) {
            announcedThisStep.clear()
            lastStepIndex = stepIndex
        }

        val maneuver = stepProgress.step?.maneuver()
        val event = HartmanEventMapper.mapTurnEvent(
            maneuverType = maneuver?.type(),
            maneuverModifier = maneuver?.modifier(),
            distanceRemainingMeters = stepProgress.distanceRemaining,
            announced = announcedThisStep
        )
        if (event != null) {
            announcedThisStep += event.distance
            speakEvent(event)
        }

        val distMiles = progress.distanceRemaining / 1609f
        _distanceRemaining.value = if (distMiles < 0.1f) {
            "${(distMiles * 5280).toInt()} ft"
        } else {
            "%.1f mi".format(distMiles)
        }

        val bannerText = stepProgress.step?.maneuver()?.instruction() ?: ""
        _currentInstruction.value = bannerText.uppercase()
    }

    val offRouteObserver = OffRouteObserver { isOffRoute ->
        if (isOffRoute) {
            offRouteCount++
            speakEvent(HartmanEvent.Recalculating(offRouteCount))
        }
    }

    val arrivalObserver = object : ArrivalObserver {
        override fun onWaypointArrival(routeProgress: RouteProgress) {}
        override fun onNextRouteLegStart(routeLegProgress: com.mapbox.navigation.base.trip.model.RouteLegProgress) {}
        override fun onFinalDestinationArrival(routeProgress: RouteProgress) {
            idleController.stop()
            speakEvent(HartmanEvent.Arrival)
            _navState.value = NavigationState.Arrived
        }
    }

    val speedLimitObserver = SpeedLimitObserver { speedLimit ->
        speedLimitKph = speedLimit.speedKmph?.toFloat() ?: 0f
        _speedLimitMph.value = speedLimitKph * 0.621f
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private fun checkSpeedWarning() {
        if (speedLimitKph <= 0f) return
        val currentKph = _currentSpeedMph.value / 0.621f
        val now = System.currentTimeMillis()
        if (currentKph > speedLimitKph + 5f && now - lastSpeedWarnTime > 30_000L) {
            lastSpeedWarnTime = now
            speakEvent(HartmanEvent.SpeedWarning)
        }
    }

    private fun speakEvent(event: HartmanEvent) {
        tts?.speak(HartmanPhraseLibrary.phraseFor(event))
    }

    @Suppress("DEPRECATION")
    private fun geocode(context: Context, address: String): Point? = try {
        Geocoder(context, Locale.getDefault())
            .getFromLocationName(address, 1)
            ?.firstOrNull()
            ?.let { Point.fromLngLat(it.longitude, it.latitude) }
    } catch (e: Exception) { null }

    override fun onCleared() {
        idleController.stop()
        tts?.shutdown()
    }
}
```

- [ ] **Step 3: Sync and verify the project builds**

**Build → Make Project**
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/olivinestudio614/hartmannav/navigation/
git commit -m "feat: add NavigationState and NavigationViewModel with Mapbox observer wiring"
```

---

## Task 9: Military Theme + Application Class

**Files:**
- Modify: `app/src/main/java/com/olivinestudio614/hartmannav/ui/theme/Color.kt`
- Modify: `app/src/main/java/com/olivinestudio614/hartmannav/ui/theme/Type.kt`
- Modify: `app/src/main/java/com/olivinestudio614/hartmannav/ui/theme/Theme.kt`
- Create: `app/src/main/java/com/olivinestudio614/hartmannav/HartmanNavApplication.kt`
- Modify: `app/src/main/AndroidManifest.xml`

- [ ] **Step 1: Create `Color.kt`**

Create `app/src/main/java/com/olivinestudio614/hartmannav/ui/theme/Color.kt`:
```kotlin
package com.olivinestudio614.hartmannav.ui.theme

import androidx.compose.ui.graphics.Color

val OliveDrab = Color(0xFF4A5240)
val ArmyGreen = Color(0xFF3B4A2F)
val ArmyGreenDark = Color(0xFF2A3520)
val AmberAlert = Color(0xFFFFB300)
val AmberAlertDark = Color(0xFFE65100)
val OffWhite = Color(0xFFE8E4D9)
val DangerRed = Color(0xFFCC2200)
val MapOverlayBg = Color(0xE6000000)
```

- [ ] **Step 2: Add Google Fonts dependency to `gradle/libs.versions.toml`**

Append to `[libraries]`:
```toml
androidx-compose-ui-text-google-fonts = { group = "androidx.compose.ui", name = "ui-text-google-fonts" }
```

Add to `app/build.gradle.kts` dependencies block:
```kotlin
implementation(libs.androidx.compose.ui.text.google.fonts)
```

- [ ] **Step 3: Create `Type.kt`**

Create `app/src/main/java/com/olivinestudio614/hartmannav/ui/theme/Type.kt`:
```kotlin
package com.olivinestudio614.hartmannav.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.olivinestudio614.hartmannav.R

private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val SpecialElite = FontFamily(
    Font(GoogleFont("Special Elite"), provider)
)

val MilitaryTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = SpecialElite,
        fontWeight = FontWeight.Normal,
        fontSize = 48.sp,
        color = OffWhite
    ),
    headlineLarge = TextStyle(
        fontFamily = SpecialElite,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        color = OffWhite
    ),
    headlineMedium = TextStyle(
        fontFamily = SpecialElite,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        color = OffWhite
    ),
    titleLarge = TextStyle(
        fontFamily = SpecialElite,
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp,
        color = OffWhite
    ),
    bodyLarge = TextStyle(
        fontFamily = SpecialElite,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        color = OffWhite
    ),
    labelMedium = TextStyle(
        fontFamily = SpecialElite,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        color = OffWhite
    )
)
```

- [ ] **Step 4: Update `Theme.kt`**

```kotlin
package com.olivinestudio614.hartmannav.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val MilitaryColorScheme = darkColorScheme(
    primary = ArmyGreen,
    onPrimary = OffWhite,
    primaryContainer = ArmyGreenDark,
    secondary = AmberAlert,
    onSecondary = ArmyGreenDark,
    background = OliveDrab,
    onBackground = OffWhite,
    surface = ArmyGreenDark,
    onSurface = OffWhite,
    error = DangerRed,
    onError = OffWhite,
)

@Composable
fun HartmanNavTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MilitaryColorScheme,
        typography = MilitaryTypography,
        content = content
    )
}
```

- [ ] **Step 5: Create `HartmanNavApplication.kt`**

```kotlin
package com.olivinestudio614.hartmannav

import android.app.Application
import com.mapbox.navigation.core.MapboxNavigationApp
import com.mapbox.navigation.core.lifecycle.MapboxNavigationObserver
import com.mapbox.navigation.base.options.NavigationOptions

class HartmanNavApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        MapboxNavigationApp.setup(
            NavigationOptions.Builder(this)
                .accessToken(BuildConfig.MAPBOX_ACCESS_TOKEN)
                .build()
        )
    }
}
```

- [ ] **Step 6: Register Application class in `AndroidManifest.xml`**

Add `android:name=".HartmanNavApplication"` to the `<application>` tag:
```xml
<application
    android:name=".HartmanNavApplication"
    android:allowBackup="true"
    ...>
```

- [ ] **Step 7: Build and verify**

**Build → Make Project**
Expected: BUILD SUCCESSFUL. The `R.array.com_google_android_gms_fonts_certs` resource is provided automatically by the `androidx.compose.ui:ui-text-google-fonts` dependency — no manual certs file needed. If you get an unresolved reference error, confirm the `androidx-compose-ui-text-google-fonts` dependency was added to `build.gradle.kts` in Step 2.

- [ ] **Step 8: Commit**

```bash
git add app/src/main/java/com/olivinestudio614/hartmannav/ui/ \
        app/src/main/java/com/olivinestudio614/hartmannav/HartmanNavApplication.kt \
        app/src/main/AndroidManifest.xml \
        gradle/libs.versions.toml app/build.gradle.kts
git commit -m "feat: add military theme, typography, and Mapbox Application init"
```

---

## Task 10: ArrivalScreen

**Files:**
- Create: `app/src/main/java/com/olivinestudio614/hartmannav/ui/screens/ArrivalScreen.kt`

- [ ] **Step 1: Create `ArrivalScreen.kt`**

```kotlin
package com.olivinestudio614.hartmannav.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.olivinestudio614.hartmannav.ui.theme.AmberAlert
import com.olivinestudio614.hartmannav.ui.theme.ArmyGreenDark
import com.olivinestudio614.hartmannav.ui.theme.OliveDrab
import com.olivinestudio614.hartmannav.ui.theme.OffWhite

@Composable
fun ArrivalScreen(
    distanceRemaining: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(OliveDrab),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "OBJECTIVE\nREACHED",
                style = MaterialTheme.typography.displayLarge.copy(
                    color = AmberAlert,
                    textAlign = TextAlign.Center,
                    lineHeight = 56.sp
                )
            )
            Text(
                text = "MISSION COMPLETE",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = OffWhite,
                    textAlign = TextAlign.Center
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ArmyGreenDark,
                    contentColor = AmberAlert
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = "AT EASE",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/olivinestudio614/hartmannav/ui/screens/ArrivalScreen.kt
git commit -m "feat: add ArrivalScreen composable"
```

---

## Task 11: NavigationOverlay

**Files:**
- Create: `app/src/main/java/com/olivinestudio614/hartmannav/ui/screens/NavigationOverlay.kt`

- [ ] **Step 1: Create `NavigationOverlay.kt`**

```kotlin
package com.olivinestudio614.hartmannav.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.olivinestudio614.hartmannav.ui.theme.AmberAlert
import com.olivinestudio614.hartmannav.ui.theme.ArmyGreenDark
import com.olivinestudio614.hartmannav.ui.theme.DangerRed
import com.olivinestudio614.hartmannav.ui.theme.MapOverlayBg
import com.olivinestudio614.hartmannav.ui.theme.OffWhite

@Composable
fun TurnInstructionCard(
    instruction: String,
    distanceRemaining: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MapOverlayBg)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = instruction.ifBlank { "AWAITING ORDERS" },
            style = MaterialTheme.typography.bodyLarge.copy(
                color = OffWhite,
                fontSize = 14.sp
            ),
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = distanceRemaining,
            style = MaterialTheme.typography.titleLarge.copy(
                color = AmberAlert,
                fontSize = 18.sp
            )
        )
    }
}

@Composable
fun SpeedStrip(
    currentSpeedMph: Float,
    speedLimitMph: Float,
    modifier: Modifier = Modifier
) {
    val isOverLimit = speedLimitMph > 0f && currentSpeedMph > speedLimitMph + 2f
    val bgColor = if (isOverLimit) DangerRed else ArmyGreenDark

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(bgColor)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "SPEED: ${currentSpeedMph.toInt()} MPH",
            style = MaterialTheme.typography.labelMedium.copy(color = OffWhite)
        )
        if (speedLimitMph > 0f) {
            Text(
                text = "LIMIT: ${speedLimitMph.toInt()} MPH",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = if (isOverLimit) OffWhite else AmberAlert
                )
            )
        }
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/olivinestudio614/hartmannav/ui/screens/NavigationOverlay.kt
git commit -m "feat: add TurnInstructionCard and SpeedStrip composables"
```

---

## Task 12: MapScreen

**Files:**
- Create: `app/src/main/java/com/olivinestudio614/hartmannav/ui/screens/MapScreen.kt`

- [ ] **Step 1: Create `MapScreen.kt`**

```kotlin
package com.olivinestudio614.hartmannav.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.Style
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.style.GenericStyle
import com.olivinestudio614.hartmannav.navigation.NavigationState
import com.olivinestudio614.hartmannav.navigation.NavigationViewModel
import com.olivinestudio614.hartmannav.ui.theme.AmberAlert
import com.olivinestudio614.hartmannav.ui.theme.ArmyGreenDark
import com.olivinestudio614.hartmannav.ui.theme.OliveDrab
import com.olivinestudio614.hartmannav.ui.theme.OffWhite

@OptIn(MapboxExperimental::class)
@Composable
fun MapScreen(
    viewModel: NavigationViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val navState by viewModel.navState.collectAsState()
    val instruction by viewModel.currentInstruction.collectAsState()
    val speedMph by viewModel.currentSpeedMph.collectAsState()
    val speedLimit by viewModel.speedLimitMph.collectAsState()
    val distanceRemaining by viewModel.distanceRemaining.collectAsState()

    val mapViewportState = rememberMapViewportState()

    LaunchedEffect(navState) {
        if (navState is NavigationState.Navigating) {
            mapViewportState.transitionToFollowPuckState()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        MapboxMap(
            modifier = Modifier.fillMaxSize(),
            mapViewportState = mapViewportState,
            style = { GenericStyle(style = Style.DARK) }
        )

        when (navState) {
            is NavigationState.Idle, is NavigationState.Error -> {
                SearchBar(
                    onSearch = { query -> /* viewModel.searchDestination(context, query) — wired in Task 13 */ },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                )
            }
            is NavigationState.RoutePreview -> {
                StartNavigationButton(
                    onStart = { /* viewModel.startNavigation(nav) — wired in Task 13 */ },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                )
            }
            is NavigationState.Navigating -> {
                Column(
                    modifier = Modifier.align(Alignment.TopCenter)
                ) {
                    TurnInstructionCard(
                        instruction = instruction,
                        distanceRemaining = distanceRemaining
                    )
                    SpeedStrip(
                        currentSpeedMph = speedMph,
                        speedLimitMph = speedLimit
                    )
                }
                StopNavigationButton(
                    onStop = { /* wired in Task 13 */ },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                )
            }
            is NavigationState.Arrived -> {
                ArrivalScreen(
                    distanceRemaining = distanceRemaining,
                    onDismiss = { viewModel.dismissArrival() }
                )
            }
            NavigationState.Searching -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(OliveDrab.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AmberAlert)
                }
            }
        }
    }
}

@Composable
private fun SearchBar(
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = {
                Text(
                    text = "ENTER TARGET COORDINATES",
                    style = MaterialTheme.typography.bodyLarge.copy(color = OffWhite.copy(alpha = 0.5f))
                )
            },
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = OffWhite),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AmberAlert,
                unfocusedBorderColor = ArmyGreenDark,
                focusedContainerColor = ArmyGreenDark,
                unfocusedContainerColor = ArmyGreenDark,
            ),
            shape = RectangleShape,
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { if (query.isNotBlank()) onSearch(query) }),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { if (query.isNotBlank()) onSearch(query) },
            shape = RectangleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = ArmyGreenDark,
                contentColor = AmberAlert
            ),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text("MOVE OUT", style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
private fun StartNavigationButton(onStart: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onStart,
        shape = RectangleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = ArmyGreenDark,
            contentColor = AmberAlert
        ),
        modifier = modifier.fillMaxWidth().height(56.dp)
    ) {
        Text("ENGAGE NAVIGATION", style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
private fun StopNavigationButton(onStop: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onStop,
        shape = RectangleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = ArmyGreenDark,
            contentColor = AmberAlert
        ),
        modifier = modifier
    ) {
        Text("ABORT", style = MaterialTheme.typography.bodyLarge)
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/olivinestudio614/hartmannav/ui/screens/MapScreen.kt
git commit -m "feat: add MapScreen composable with state-driven UI"
```

---

## Task 13: Wire Everything — MainActivity, Manifest, Permissions

**Files:**
- Modify: `app/src/main/java/com/olivinestudio614/hartmannav/MainActivity.kt`
- Modify: `app/src/main/AndroidManifest.xml`

This task connects the ViewModel to the UI (passing Context and `MapboxNavigation` instance), registers all Mapbox observers, and handles location permissions.

- [ ] **Step 1: Add permissions to `AndroidManifest.xml`**

Replace the full `AndroidManifest.xml` with:
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_LOCATION" />
    <uses-permission android:name="android.permission.INTERNET" />

    <application
        android:name=".HartmanNavApplication"
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.HartmanNav">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:label="@string/app_name"
            android:theme="@style/Theme.HartmanNav">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>
```

- [ ] **Step 2: Replace `MainActivity.kt` with full wired version**

```kotlin
package com.olivinestudio614.hartmannav

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.mapbox.navigation.core.MapboxNavigationApp
import com.mapbox.navigation.core.lifecycle.MapboxNavigationObserver
import com.mapbox.navigation.core.MapboxNavigation
import com.olivinestudio614.hartmannav.navigation.NavigationViewModel
import com.olivinestudio614.hartmannav.ui.screens.MapScreen
import com.olivinestudio614.hartmannav.ui.theme.HartmanNavTheme

class MainActivity : ComponentActivity() {

    private val viewModel: NavigationViewModel by viewModels()

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            attachMapboxNavigation()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.initTts(this)

        if (hasLocationPermission()) {
            attachMapboxNavigation()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }

        setContent {
            HartmanNavTheme {
                MapScreen(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    private fun attachMapboxNavigation() {
        MapboxNavigationApp.attach(this)
        MapboxNavigationApp.registerObserver(object : MapboxNavigationObserver {
            override fun onAttached(mapboxNavigation: MapboxNavigation) {
                mapboxNavigation.registerLocationObserver(viewModel.locationObserver)
                mapboxNavigation.registerRouteProgressObserver(viewModel.routeProgressObserver)
                mapboxNavigation.registerOffRouteObserver(viewModel.offRouteObserver)
                mapboxNavigation.registerArrivalObserver(viewModel.arrivalObserver)
                mapboxNavigation.registerSpeedLimitObserver(viewModel.speedLimitObserver)
                viewModel.setMapboxNavigation(mapboxNavigation)
            }
            override fun onDetached(mapboxNavigation: MapboxNavigation) {
                mapboxNavigation.unregisterLocationObserver(viewModel.locationObserver)
                mapboxNavigation.unregisterRouteProgressObserver(viewModel.routeProgressObserver)
                mapboxNavigation.unregisterOffRouteObserver(viewModel.offRouteObserver)
                mapboxNavigation.unregisterArrivalObserver(viewModel.arrivalObserver)
                mapboxNavigation.unregisterSpeedLimitObserver(viewModel.speedLimitObserver)
                viewModel.setMapboxNavigation(null)
            }
        })
    }

    private fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
}
```

- [ ] **Step 3: Verify `NavigationViewModel.kt` already has `setMapboxNavigation`**

The `mapboxNavigation` field and `setMapboxNavigation(nav)` method were added in Task 8. No changes needed here — confirm they are present before proceeding.

- [ ] **Step 4: Wire search and navigation actions into `MapScreen.kt`**

In `MapScreen.kt`, the `onSearch`, `onStart`, and `onStop` lambdas need `Context` and `mapboxNavigation`. Update the `SearchBar` call and buttons to use the actual ViewModel methods:

Replace `/* viewModel.searchDestination(context, query) — wired in Task 13 */` with:
```kotlin
val context = LocalContext.current
// Pass context to searchDestination:
onSearch = { query -> viewModel.searchDestination(context, query) }
```

Replace the `onStart` stub with:
```kotlin
onStart = { viewModel.startNavigation() }
```

Replace the `onStop` stub with:
```kotlin
onStop = { viewModel.stopNavigation() }
```

Full updated `MapScreen` lambda sections (replace the three stubs):
```kotlin
// In SearchBar:
onSearch = { query ->
    val ctx = LocalContext.current
    if (query.isNotBlank()) viewModel.searchDestination(ctx, query)
}

// Start button:
onStart = { viewModel.startNavigation() }

// Stop button:
onStop = { viewModel.stopNavigation() }
```

Add `import androidx.compose.ui.platform.LocalContext` at the top of `MapScreen.kt`.

- [ ] **Step 5: Final build**

**Build → Make Project**
Expected: BUILD SUCCESSFUL.

- [ ] **Step 6: Run on device or emulator**

Deploy to a physical Android device (GPS works best on real hardware).
Test flow:
1. App launches → location permission prompt → approve
2. Map appears in dark military style
3. Type an address in the search bar → tap MOVE OUT
4. Route preview appears → tap ENGAGE NAVIGATION
5. Hartman voice fires "LISTEN UP, MAGGOT..." 
6. Drive (or walk) — verify turn announcements at 500ft, 200ft, and NOW
7. Miss a turn — verify recalculating rant
8. Arrive — verify OBJECTIVE REACHED screen

- [ ] **Step 7: Final commit**

```bash
git add app/src/main/java/com/olivinestudio614/hartmannav/MainActivity.kt \
        app/src/main/AndroidManifest.xml \
        app/src/main/java/com/olivinestudio614/hartmannav/navigation/NavigationViewModel.kt \
        app/src/main/java/com/olivinestudio614/hartmannav/ui/screens/MapScreen.kt
git commit -m "feat: wire MainActivity, permissions, Mapbox observers, and full navigation flow"
```

---

## All Unit Tests

Run the full test suite at any time:
```bash
./gradlew :app:test
```
Expected: 14 tests passing across `HartmanPhraseLibraryTest`, `HartmanEventMapperTest`, `IdleTauntControllerTest`.
