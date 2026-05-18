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
