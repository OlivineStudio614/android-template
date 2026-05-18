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
    private var mapboxNavigation: MapboxNavigation? = null
    private var lastKnownOrigin: Point? = null
    private var offRouteCount = 0
    private val announcedThisStep = mutableSetOf<HartmanEvent.Turn.Distance>()
    private var lastStepIndex = -1
    private var lastSpeedWarnTime = 0L

    private val idleController = IdleTauntController(viewModelScope) {
        speakEvent(HartmanEvent.IdleTaunt)
    }

    fun initTts(context: Context) {
        if (tts == null) tts = HartmanTTS(context)
    }

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

    // ── Mapbox Observers ────────────────────────────────────────────────────

    val locationObserver = object : LocationObserver {
        override fun onNewRawLocation(rawLocation: Location) {
            lastKnownOrigin = Point.fromLngLat(rawLocation.longitude, rawLocation.latitude)
            val speedMph = ((rawLocation.speed ?: 0.0) * 2.237).toFloat()
            _currentSpeedMph.value = speedMph
            idleController.notifyMovement()
            checkSpeedWarning(speedMph)
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
            val distance = when (event) {
                is HartmanEvent.Turn.Left -> event.distance
                is HartmanEvent.Turn.Right -> event.distance
                is HartmanEvent.Turn.SlightLeft -> event.distance
                is HartmanEvent.Turn.SlightRight -> event.distance
            }
            announcedThisStep += distance
            speakEvent(event)
        }

        val distMiles = progress.distanceRemaining / 1609f
        _distanceRemaining.value = if (distMiles < 0.1f) {
            "${(distMiles * 5280).toInt()} ft"
        } else {
            "%.1f mi".format(distMiles)
        }

        _currentInstruction.value = (stepProgress.step?.maneuver()?.instruction() ?: "").uppercase()
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
}
