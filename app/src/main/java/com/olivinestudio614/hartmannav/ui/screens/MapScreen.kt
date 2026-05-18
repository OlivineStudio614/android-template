package com.olivinestudio614.hartmannav.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
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
    val context = LocalContext.current

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
                Column(modifier = Modifier.align(Alignment.TopCenter)) {
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
