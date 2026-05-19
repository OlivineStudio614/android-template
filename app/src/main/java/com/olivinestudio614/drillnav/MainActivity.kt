package com.olivinestudio614.drillnav

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.navigation.core.lifecycle.MapboxNavigationObserver
import com.olivinestudio614.drillnav.navigation.NavigationViewModel
import com.olivinestudio614.drillnav.ui.screens.MapScreen
import com.olivinestudio614.drillnav.ui.theme.DrillNavTheme

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
            DrillNavTheme {
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
                viewModel.setMapboxNavigation(mapboxNavigation)
                mapboxNavigation.registerLocationObserver(viewModel.locationObserver)
                mapboxNavigation.registerRouteProgressObserver(viewModel.routeProgressObserver)
                mapboxNavigation.registerOffRouteObserver(viewModel.offRouteObserver)
                mapboxNavigation.registerArrivalObserver(viewModel.arrivalObserver)
            }
            override fun onDetached(mapboxNavigation: MapboxNavigation) {
                mapboxNavigation.unregisterLocationObserver(viewModel.locationObserver)
                mapboxNavigation.unregisterRouteProgressObserver(viewModel.routeProgressObserver)
                mapboxNavigation.unregisterOffRouteObserver(viewModel.offRouteObserver)
                mapboxNavigation.unregisterArrivalObserver(viewModel.arrivalObserver)
                viewModel.setMapboxNavigation(null)
            }
        })
    }

    private fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
}
