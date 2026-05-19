package com.olivinestudio614.drillnav.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.mapbox.search.result.SearchSuggestion
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.Style
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.style.GenericStyle
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineApiOptions
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineViewOptions
import com.mapbox.maps.plugin.locationcomponent.location
import com.olivinestudio614.drillnav.navigation.NavigationState
import com.olivinestudio614.drillnav.navigation.NavigationViewModel
import com.olivinestudio614.drillnav.ui.theme.AmberAlert
import com.olivinestudio614.drillnav.ui.theme.ArmyGreen
import com.olivinestudio614.drillnav.ui.theme.ArmyGreenDark
import com.olivinestudio614.drillnav.ui.theme.OliveDrab
import com.olivinestudio614.drillnav.ui.theme.OffWhite

@OptIn(MapboxExperimental::class)
@Composable
fun MapScreen(
    viewModel: NavigationViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val navState by viewModel.navState.collectAsState()
    val instruction by viewModel.currentInstruction.collectAsState()
    val speedMph by viewModel.currentSpeedMph.collectAsState()
    val speedLimit by viewModel.speedLimitMph.collectAsState()
    val distanceRemaining by viewModel.distanceRemaining.collectAsState()
    val suggestions by viewModel.suggestions.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState()

    val mapViewportState = rememberMapViewportState()

    // Route line components — created once for the lifetime of this screen
    val routeLineApi = remember { MapboxRouteLineApi(MapboxRouteLineApiOptions.Builder().build()) }
    val routeLineView = remember { MapboxRouteLineView(MapboxRouteLineViewOptions.Builder(context).build()) }

    // Holds references once the map and its style are ready
    var mapboxMapRef by remember { mutableStateOf<com.mapbox.maps.MapboxMap?>(null) }
    var mapStyleRef by remember { mutableStateOf<com.mapbox.maps.Style?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            routeLineApi.cancel()
            routeLineView.cancel()
        }
    }

    // Center on user immediately at launch
    LaunchedEffect(Unit) {
        mapViewportState.transitionToFollowPuckState()
    }

    // React to nav state: draw/clear route line, switch camera mode
    // Keyed on both navState and mapStyleRef so we retry once style loads
    LaunchedEffect(navState, mapStyleRef) {
        val style = mapStyleRef ?: return@LaunchedEffect
        when (val state = navState) {
            is NavigationState.RoutePreview -> {
                routeLineApi.setNavigationRoutes(state.routes) { result ->
                    routeLineView.renderRouteDrawData(style, result)
                }
            }
            is NavigationState.Navigating -> {
                mapViewportState.transitionToFollowPuckState()
            }
            is NavigationState.Idle, is NavigationState.Arrived -> {
                routeLineApi.clearRouteLine { result ->
                    routeLineView.renderClearRouteLineValue(style, result)
                }
            }
            else -> {}
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        MapboxMap(
            modifier = Modifier.fillMaxSize(),
            mapViewportState = mapViewportState,
            style = { GenericStyle(style = Style.DARK) },
            scaleBar = { ScaleBar(modifier = Modifier.statusBarsPadding()) }
        ) {
            MapEffect(Unit) { mapView ->
                mapView.location.apply {
                    enabled = true
                    pulsingEnabled = true
                    setLocationProvider(viewModel.navigationLocationProvider)
                }
                mapboxMapRef = mapView.mapboxMap
                // Initialize route line layers whenever the style (re)loads
                mapView.mapboxMap.subscribeStyleLoaded { _ ->
                    mapView.mapboxMap.style?.let { style ->
                        routeLineView.initializeLayers(style)
                        mapStyleRef = style
                    }
                }
                // Also init now if style is already loaded
                mapView.mapboxMap.style?.let { style ->
                    routeLineView.initializeLayers(style)
                    mapStyleRef = style
                }
            }
        }

        when (navState) {
            is NavigationState.Idle, is NavigationState.Error -> {
                val errorMsg = (navState as? NavigationState.Error)?.message
                SearchBar(
                    errorMessage = errorMsg,
                    suggestions = suggestions,
                    onQueryChange = { viewModel.onQueryChanged(it) },
                    onSuggestionSelected = { viewModel.selectSuggestion(it) },
                    onSearch = { query -> viewModel.searchDestination(context, query) },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                )
            }
            is NavigationState.RoutePreview -> {
                StartNavigationButton(
                    onStart = { viewModel.startNavigation() },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                )
            }
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
                }
                StopNavigationButton(
                    onStop = { viewModel.stopNavigation() },
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

        LocateMeButton(
            onClick = {
                val loc = currentLocation
                if (loc != null) {
                    mapViewportState.easeTo(
                        CameraOptions.Builder().center(loc).zoom(16.0).build()
                    )
                } else {
                    mapViewportState.transitionToFollowPuckState()
                }
            },
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp)
        )
    }
}

@Composable
private fun SearchBar(
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier,
    errorMessage: String? = null,
    suggestions: List<SearchSuggestion> = emptyList(),
    onQueryChange: (String) -> Unit = {},
    onSuggestionSelected: (SearchSuggestion) -> Unit = {}
) {
    var query by remember { mutableStateOf("") }
    Column(modifier = modifier.fillMaxWidth()) {
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (suggestions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 240.dp)
                    .background(ArmyGreenDark)
            ) {
                items(suggestions) { suggestion ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                query = suggestion.name
                                onSuggestionSelected(suggestion)
                            }
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = suggestion.name,
                            style = MaterialTheme.typography.bodyMedium.copy(color = OffWhite)
                        )
                        suggestion.descriptionText?.let { desc ->
                            Text(
                                text = desc,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = OffWhite.copy(alpha = 0.55f)
                                )
                            )
                        }
                    }
                    HorizontalDivider(color = ArmyGreen.copy(alpha = 0.4f), thickness = 0.5.dp)
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = query,
            onValueChange = {
                query = it
                onQueryChange(it)
            },
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
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
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
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Text("ENGAGE NAVIGATION", style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
private fun StopNavigationButton(
    onStop: () -> Unit,
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
        Text("ABORT", style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun LocateMeButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(48.dp),
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = ArmyGreenDark,
            contentColor = AmberAlert
        )
    ) {
        Icon(
            imageVector = Icons.Filled.MyLocation,
            contentDescription = "Locate me"
        )
    }
}

