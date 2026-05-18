package com.olivinestudio614.hartmannav.navigation

import android.content.Context
import android.location.Geocoder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.common.location.Location
import com.mapbox.geojson.Point
import com.mapbox.navigation.base.extensions.applyDefaultNavigationOptions
import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.navigation.base.route.NavigationRouterCallback
import com.mapbox.navigation.base.route.RouterFailure
import com.mapbox.navigation.base.trip.model.RouteLegProgress
import com.mapbox.navigation.base.trip.model.RouteProgress
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.arrival.ArrivalObserver
import com.mapbox.navigation.core.trip.session.LocationMatcherResult
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.core.replay.route.ReplayProgressObserver
import com.mapbox.navigation.core.replay.route.ReplayRouteMapper
import com.mapbox.navigation.core.trip.session.OffRouteObserver
import com.mapbox.navigation.core.trip.session.RouteProgressObserver
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
import java.util.Collections
import java.util.Locale

@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
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

    private val _simulationMode = MutableStateFlow(false)
    val simulationMode: StateFlow<Boolean> = _simulationMode

    private var tts: HartmanTTS? = null
    private var mapboxNavigation: MapboxNavigation? = null
    private var replayProgressObserver: ReplayProgressObserver? = null
    @Volatile private var lastKnownOrigin: Point? = null
    @Volatile private var offRouteCount = 0
    private val announcedThisStep = Collections.synchronizedSet(mutableSetOf<HartmanEvent.Turn.Distance>())
    @Volatile private var lastStepIndex = -1
    @Volatile private var lastSpeedWarnTime = 0L

    private val idleController = IdleTauntController(viewModelScope) {
        speakEvent(HartmanEvent.IdleTaunt)
    }

    fun initTts(context: Context) {
        if (tts == null) tts = HartmanTTS(context)
    }

    fun toggleSimulation() {
        if (_navState.value !is NavigationState.Idle) return
        _simulationMode.value = !_simulationMode.value
    }

    fun setMapboxNavigation(nav: MapboxNavigation?) {
        mapboxNavigation = nav
        if (nav != null) {
            replayProgressObserver = ReplayProgressObserver(nav.mapboxReplayer)
            if (_simulationMode.value) {
                nav.startReplayTripSession()
            } else {
                nav.startTripSession()
            }
        } else {
            replayProgressObserver = null
        }
    }

    fun getReplayProgressObserver(): ReplayProgressObserver? = replayProgressObserver

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

    fun startNavigation() {
        startNavigation(mapboxNavigation ?: return)
    }

    fun stopNavigation() {
        stopNavigation(mapboxNavigation ?: return)
    }

    fun dismissArrival() {
        _navState.value = NavigationState.Idle
    }

    private fun requestRoute(nav: MapboxNavigation, destination: Point) {
        val origin = lastKnownOrigin ?: run {
            _navState.value = NavigationState.Error("Waiting for GPS fix")
            return
        }
        nav.requestRoutes(
            RouteOptions.builder()
                .applyDefaultNavigationOptions()
                .coordinatesList(listOf(origin, destination))
                .build(),
            object : NavigationRouterCallback {
                override fun onRoutesReady(
                    routes: List<NavigationRoute>,
                    routerOrigin: String
                ) {
                    _navState.value = NavigationState.RoutePreview(routes)
                }
                override fun onFailure(reasons: List<RouterFailure>, routeOptions: RouteOptions) {
                    _navState.value = NavigationState.Error("Route request failed")
                }
                override fun onCanceled(routeOptions: RouteOptions, routerOrigin: String) {}
            }
        )
    }

    private fun startNavigation(nav: MapboxNavigation) {
        val state = _navState.value as? NavigationState.RoutePreview ?: return
        nav.setNavigationRoutes(state.routes)
        _navState.value = NavigationState.Navigating
        offRouteCount = 0
        announcedThisStep.clear()
        lastStepIndex = -1
        idleController.start()
        speakEvent(HartmanEvent.TripStart)
        if (_simulationMode.value) {
            val events = ReplayRouteMapper().mapDirectionsRouteGeometry(
                state.routes.first().directionsRoute
            )
            nav.mapboxReplayer.pushEvents(events)
            nav.mapboxReplayer.playbackSpeed(1.5)
            nav.mapboxReplayer.play()
        }
    }

    private fun stopNavigation(nav: MapboxNavigation) {
        if (_simulationMode.value) {
            nav.mapboxReplayer.stop()
            nav.mapboxReplayer.clearEvents()
        }
        nav.setNavigationRoutes(emptyList())
        idleController.stop()
        _navState.value = NavigationState.Idle
    }

    // ── Mapbox Observers ────────────────────────────────────────────────────

    val locationObserver = object : LocationObserver {
        override fun onNewRawLocation(rawLocation: Location) {
            lastKnownOrigin = Point.fromLngLat(rawLocation.longitude, rawLocation.latitude)
            val speedMph = ((rawLocation.speed ?: 0.0) * MS_TO_MPH).toFloat()
            _currentSpeedMph.value = speedMph
            if (_navState.value is NavigationState.Navigating) {
                idleController.notifyMovement()
                checkSpeedWarning(speedMph)
            }
        }
        override fun onNewLocationMatcherResult(locationMatcherResult: LocationMatcherResult) {}
    }

    val routeProgressObserver = RouteProgressObserver { progress ->
        val legProgress = progress.currentLegProgress ?: return@RouteProgressObserver
        val stepProgress = legProgress.currentStepProgress ?: return@RouteProgressObserver
        val stepIndex = stepProgress.stepIndex

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

        val distMiles = progress.distanceRemaining / METERS_PER_MILE
        _distanceRemaining.value = if (distMiles < 0.1f) {
            "${(distMiles * 5280).toInt()} ft"
        } else {
            "%.1f mi".format(distMiles)
        }

        _currentInstruction.value = (stepProgress.step?.maneuver()?.instruction() ?: "").uppercase(Locale.US)
    }

    val offRouteObserver = OffRouteObserver { isOffRoute ->
        if (isOffRoute) {
            offRouteCount++
            speakEvent(HartmanEvent.Recalculating(offRouteCount))
        }
    }

    val arrivalObserver = object : ArrivalObserver {
        override fun onWaypointArrival(routeProgress: RouteProgress) {}
        override fun onNextRouteLegStart(routeLegProgress: RouteLegProgress) {}
        override fun onFinalDestinationArrival(routeProgress: RouteProgress) {
            idleController.stop()
            speakEvent(HartmanEvent.Arrival)
            _navState.value = NavigationState.Arrived
        }
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private fun checkSpeedWarning(speedMph: Float) {
        val limitMph = _speedLimitMph.value
        if (limitMph <= 0f) return
        val now = System.currentTimeMillis()
        if (speedMph > limitMph + 5f && now - lastSpeedWarnTime > 30_000L) {
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

    private companion object {
        const val METERS_PER_MILE = 1609f
        const val MS_TO_MPH = 2.237
    }
}
